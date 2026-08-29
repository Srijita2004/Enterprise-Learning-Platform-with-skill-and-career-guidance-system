import { CommonModule } from '@angular/common';
import { Component, ElementRef, ViewChild, inject, signal, effect } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AiAssistantService, AiChatMessage, AiConversationSummary } from '../../../core/services/ai-assistant.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-assistant.html',
  styleUrls: ['./ai-assistant.css']
})
export class AiAssistantComponent {
  readonly aiService = inject(AiAssistantService);
  readonly authService = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);

  @ViewChild('chatScrollContainer') private chatContainer?: ElementRef<HTMLDivElement>;

  userInput = signal<string>('');
  showHistoryView = signal<boolean>(false);

  get welcomeGreeting(): { title: string; subtitle: string } {
    const user = this.authService.currentUser();
    const rawName = user?.name?.trim();
    if (rawName) {
      return {
        title: `Hi, ${rawName}! 👋`,
        subtitle: 'Welcome to EIWI. How can I help you today?'
      };
    }
    return {
      title: 'Hi! 👋',
      subtitle: 'Welcome to EIWI. How can I help you today?'
    };
  }

  constructor() {
    effect(() => {
      const msgs = this.aiService.messages();
      if (msgs.length > 0 || this.aiService.isLoading()) {
        setTimeout(() => this.scrollToBottom(), 60);
      }
    });
  }

  toggleOpen(): void {
    this.aiService.toggleOpen();
    if (this.aiService.isOpen()) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  close(): void {
    this.aiService.close();
  }

  toggleHistoryView(): void {
    this.showHistoryView.update(v => !v);
    if (this.showHistoryView()) {
      this.aiService.loadConversations().subscribe();
    }
  }

  startNewChat(): void {
    this.aiService.startNewChat();
    this.showHistoryView.set(false);
  }

  selectConversation(conv: AiConversationSummary): void {
    this.aiService.loadConversation(conv.conversationId).subscribe(() => {
      this.showHistoryView.set(false);
      setTimeout(() => this.scrollToBottom(), 100);
    });
  }

  deleteConversation(event: MouseEvent, convId: string): void {
    event.stopPropagation();
    this.aiService.deleteConversation(convId).subscribe();
  }

  sendMessage(quickAction?: string): void {
    const text = this.userInput().trim();
    if (!text && !quickAction) return;

    const messageToSend = text || (quickAction ? `Please ${quickAction.toLowerCase()} based on current material.` : '');
    this.userInput.set('');

    this.aiService.sendMessage(messageToSend, quickAction).subscribe();
  }

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  triggerQuickAction(action: string): void {
    const ctx = this.aiService.activeContext();
    let prompt = action;
    if ((action === 'Explain this lesson' || action === 'Explain this topic') && ctx?.contentTitle) {
      prompt = `Explain the lesson "${ctx.contentTitle}".`;
    } else if (action === 'Summarize lesson' && ctx?.contentTitle) {
      prompt = `Summarize the lesson "${ctx.contentTitle}".`;
    } else if (action.includes('hint') && ctx?.contentTitle) {
      prompt = `Give me a hint for "${ctx.contentTitle}".`;
    }
    this.aiService.sendMessage(prompt, action).subscribe();
  }

  renderMarkdown(content: string): SafeHtml {
    if (!content) return '';

    let html = content
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    html = html.replace(/```([a-zA-Z0-9_-]*)\n([\s\S]*?)```/g, (_match, lang, code) => {
      const trimmed = code ? code.trim() : '';
      return `<div class="ai-code-block"><div class="code-header"><span class="code-lang">${lang || 'code'}</span></div><pre><code>${trimmed}</code></pre></div>`;
    });

    html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>');
    html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
    html = html.replace(/^### (.*$)/gim, '<h4 class="ai-md-h4">$1</h4>');
    html = html.replace(/^## (.*$)/gim, '<h3 class="ai-md-h3">$1</h3>');
    html = html.replace(/^\s*-\s+(.*$)/gim, '<li class="ai-md-li">$1</li>');
    html = html.replace(/^\s*\d+\.\s+(.*$)/gim, '<li class="ai-md-oli">$1</li>');
    html = html.replace(/\n\n/g, '<br/><br/>').replace(/\n/g, '<br/>');

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private scrollToBottom(): void {
    if (this.chatContainer && this.chatContainer.nativeElement) {
      const el = this.chatContainer.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}
