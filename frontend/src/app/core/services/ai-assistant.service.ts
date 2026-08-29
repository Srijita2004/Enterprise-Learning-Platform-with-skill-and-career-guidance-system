import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AiChatMessage {
  messageId: string;
  conversationId: string;
  senderType: 'USER' | 'ASSISTANT' | 'SYSTEM';
  content: string;
  contextSummary?: string;
  createdAt: string;
}

export interface AiConversationSummary {
  conversationId: string;
  title: string;
  courseId?: string;
  courseTitle?: string;
  contentId?: string;
  contentTitle?: string;
  lastMessagePreview?: string;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface AiConversationDetail {
  conversationId: string;
  title: string;
  courseId?: string;
  courseTitle?: string;
  moduleId?: string;
  contentId?: string;
  contentTitle?: string;
  messages: AiChatMessage[];
  createdAt: string;
  updatedAt: string;
}

export interface AiChatRequest {
  conversationId?: string;
  courseId?: string;
  courseTitle?: string;
  moduleId?: string;
  contentId?: string;
  contentTitle?: string;
  message: string;
  quickAction?: string;
}

export interface AiChatResponse {
  conversationId: string;
  userMessageId: string;
  assistantMessageId: string;
  reply: string;
  senderType: 'ASSISTANT';
  courseTitle?: string;
  lessonTitle?: string;
  contextGrounded: boolean;
  providerEngine: string;
  suggestedFollowUps: string[];
  timestamp: string;
}

export interface ActiveCourseContext {
  courseId: string;
  courseTitle: string;
  moduleId?: string;
  moduleTitle?: string;
  contentId?: string;
  contentTitle?: string;
}

@Injectable({ providedIn: 'root' })
export class AiAssistantService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.learningApiUrl}/ai-assistant`;

  // UI state signals
  readonly isOpen = signal<boolean>(false);
  readonly isLoading = signal<boolean>(false);
  readonly activeConversationId = signal<string | null>(null);
  readonly conversations = signal<AiConversationSummary[]>([]);
  readonly messages = signal<AiChatMessage[]>([]);
  readonly activeContext = signal<ActiveCourseContext | null>(null);
  readonly error = signal<string | null>(null);
  readonly suggestedFollowUps = signal<string[]>([]);
  readonly activeEngine = signal<string>('EIWI');

  toggleOpen(): void {
    this.isOpen.update(v => !v);
    if (this.isOpen() && this.conversations().length === 0) {
      this.loadConversations().subscribe();
    }
  }

  open(): void {
    this.isOpen.set(true);
    if (this.conversations().length === 0) {
      this.loadConversations().subscribe();
    }
  }

  close(): void {
    this.isOpen.set(false);
  }

  setCourseContext(context: ActiveCourseContext | null): void {
    this.activeContext.set(context);
  }

  clearCourseContext(): void {
    this.activeContext.set(null);
  }

  startNewChat(): void {
    this.activeConversationId.set(null);
    this.messages.set([]);
    this.error.set(null);
    this.suggestedFollowUps.set([
      'Explain this topic',
      'Summarize this lesson',
      'Give me an example',
      'Help me understand this concept',
      'Give me a hint'
    ]);
  }

  loadConversations(): Observable<AiConversationSummary[]> {
    return this.http.get<AiConversationSummary[]>(`${this.baseUrl}/conversations`).pipe(
      tap(convs => this.conversations.set(convs || [])),
      catchError(() => of([]))
    );
  }

  loadConversation(conversationId: string): Observable<AiConversationDetail | null> {
    this.isLoading.set(true);
    this.error.set(null);
    return this.http.get<AiConversationDetail>(`${this.baseUrl}/conversations/${conversationId}`).pipe(
      tap(detail => {
        this.activeConversationId.set(detail.conversationId);
        this.messages.set(detail.messages || []);
        this.isLoading.set(false);
      }),
      catchError(err => {
        this.error.set('Could not load conversation history.');
        this.isLoading.set(false);
        return of(null);
      })
    );
  }

  sendMessage(messageText: string, quickAction?: string): Observable<AiChatResponse | null> {
    const text = messageText.trim();
    if (!text) return of(null);

    const ctx = this.activeContext();
    const payload: AiChatRequest = {
      conversationId: this.activeConversationId() || undefined,
      courseId: ctx?.courseId,
      courseTitle: ctx?.courseTitle,
      moduleId: ctx?.moduleId,
      contentId: ctx?.contentId,
      contentTitle: ctx?.contentTitle,
      message: text,
      quickAction: quickAction
    };

    // Optimistically add user message to UI
    const tempUserMsg: AiChatMessage = {
      messageId: 'temp-' + Date.now(),
      conversationId: this.activeConversationId() || '',
      senderType: 'USER',
      content: text,
      contextSummary: ctx?.contentTitle ? `Lesson: ${ctx.contentTitle}` : undefined,
      createdAt: new Date().toISOString()
    };
    this.messages.update(msgs => [...msgs, tempUserMsg]);
    this.isLoading.set(true);
    this.error.set(null);

    return this.http.post<AiChatResponse>(`${this.baseUrl}/chat`, payload).pipe(
      tap(resp => {
        this.isLoading.set(false);
        this.activeConversationId.set(resp.conversationId);
        this.activeEngine.set(resp.providerEngine || 'EIWI');
        this.suggestedFollowUps.set(resp.suggestedFollowUps || []);

        const assistantMsg: AiChatMessage = {
          messageId: resp.assistantMessageId,
          conversationId: resp.conversationId,
          senderType: 'ASSISTANT',
          content: resp.reply,
          contextSummary: resp.lessonTitle ? `Context: ${resp.lessonTitle}` : undefined,
          createdAt: resp.timestamp || new Date().toISOString()
        };

        this.messages.update(msgs => {
          // Replace temp user message with valid state if needed, add assistant message
          return [...msgs, assistantMsg];
        });

        // Refresh conversation history list in background
        this.loadConversations().subscribe();
      }),
      catchError(err => {
        this.isLoading.set(false);
        let errorMsg = 'Failed to generate AI response. Please try again.';
        if (err.status === 401) {
          errorMsg = 'Session expired. Please log in again.';
        } else if (err.status === 403) {
          errorMsg = 'You do not have authorization for this conversation.';
        }
        this.error.set(errorMsg);
        return of(null);
      })
    );
  }

  deleteConversation(conversationId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/conversations/${conversationId}`).pipe(
      tap(() => {
        this.conversations.update(list => list.filter(c => c.conversationId !== conversationId));
        if (this.activeConversationId() === conversationId) {
          this.startNewChat();
        }
      })
    );
  }
}
