import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';

import { Course } from '../../../learning/models/course.model';
import {
  CourseContent,
  CourseModule
} from '../../../learning/models/learning-content.model';
import { CourseService } from '../../../learning/services/course.service';
import { LearningContentService } from '../../../learning/services/learning-content.service';
import { ToastService } from '../../../shared/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';

const CONTENT_TYPES = [
  'VIDEO', 'DOCUMENT', 'ARTICLE', 'EXTERNAL_LINK', 'QUIZ', 'ASSIGNMENT', 'LIVE_SESSION'
];

interface ModuleFormState {
  title: string;
  description: string;
  moduleOrder: number;
}

export interface QuizQuestionState {
  id: number;
  question: string;
  options: string[];
  correctOption: number;
}

export type ModuleResourceType = 'YOUTUBE_VIDEO' | 'ONLINE_QUIZ' | 'EXTERNAL_READING';

interface ContentFormState {
  title: string;
  description: string;
  resourceType: ModuleResourceType;
  contentType: string;
  contentUrl: string;
  textContent: string;
  durationMinutes: number | null;
  contentOrder: number;
  mandatory: boolean;
  previewAvailable: boolean;
  quizPassingScore: number;
  quizQuestions: QuizQuestionState[];
}

const EMPTY_MODULE_FORM: ModuleFormState = {
  title: '',
  description: '',
  moduleOrder: 1
};

const DEFAULT_QUIZ_QUESTIONS: QuizQuestionState[] = [
  {
    id: 1,
    question: 'Which of the following best describes the core principle of this module?',
    options: [
      'Stateless architectural scalability and loose coupling',
      'Direct coupling between client and database',
      'Monolithic tight bindings',
      'Manual deployment without testing'
    ],
    correctOption: 0
  },
  {
    id: 2,
    question: 'What is the primary verification step before production release?',
    options: [
      'Skipping code reviews',
      'Automated test suite execution and CI validation',
      'Deploying directly to production',
      'Disabling logging and monitoring'
    ],
    correctOption: 1
  }
];

const EMPTY_CONTENT_FORM: ContentFormState = {
  title: '',
  description: '',
  resourceType: 'YOUTUBE_VIDEO',
  contentType: 'VIDEO',
  contentUrl: '',
  textContent: '',
  durationMinutes: 15,
  contentOrder: 1,
  mandatory: true,
  previewAvailable: false,
  quizPassingScore: 70,
  quizQuestions: [...DEFAULT_QUIZ_QUESTIONS.map(q => ({ ...q, options: [...q.options] }))]
};

export interface ModuleItem {
  module: CourseModule;
  contents: CourseContent[];
  isExpanded: boolean;
  isLoading: boolean;
}

@Component({
  selector: 'app-content-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './content-management.html',
  styleUrls: ['./content-management.css']
})
export class ContentManagement implements OnInit {

  private readonly courseService = inject(CourseService);
  private readonly contentService = inject(LearningContentService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);
  private readonly route = inject(ActivatedRoute);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly contentTypes = CONTENT_TYPES;

  courses: Course[] = [];
  selectedCourseId = '';
  selectedCourse: Course | null = null;
  isLoadingCourses = false;

  moduleItems: ModuleItem[] = [];
  isLoadingCurriculum = false;

  selectedModuleId = '';
  contents: CourseContent[] = [];

  // Module form
  isModuleFormOpen = false;
  isEditingModule = false;
  editingModuleId: string | null = null;
  moduleForm: ModuleFormState = { ...EMPTY_MODULE_FORM };
  moduleFormErrors: string[] = [];

  // Content form
  isContentFormOpen = false;
  isEditingContent = false;
  editingContentId: string | null = null;
  contentForm: ContentFormState = { ...EMPTY_CONTENT_FORM };
  contentFormErrors: string[] = [];

  get totalLessonsCount(): number {
    return this.moduleItems.reduce((acc, m) => acc + (m.contents?.length || 0), 0);
  }

  get totalCourseDuration(): number {
    return this.moduleItems.reduce((acc, m) =>
      acc + (m.contents?.reduce((cAcc, c) => cAcc + (c.durationMinutes || 0), 0) || 0), 0
    );
  }

  ngOnInit(): void {
    this.loadCoursesAndCheckParam();
  }

  loadCoursesAndCheckParam(): void {
    this.isLoadingCourses = true;
    this.cdr.markForCheck();
    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        this.courses = courses || [];
        this.isLoadingCourses = false;

        const queryId = this.route.snapshot.queryParamMap.get('courseId');
        if (queryId && this.courses.some(c => c.courseId === queryId)) {
          this.selectedCourseId = queryId;
        } else if (this.courses.length > 0 && !this.selectedCourseId) {
          this.selectedCourseId = this.courses[0].courseId;
        }
        this.onCourseChange();
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingCourses = false;
        this.toastService.showError('Could not load courses.');
        this.cdr.detectChanges();
      }
    });
  }

  onCourseChange(): void {
    this.selectedCourse = this.courses.find(c => c.courseId === this.selectedCourseId) || null;
    this.selectedModuleId = '';
    this.contents = [];
    if (!this.selectedCourseId) {
      this.moduleItems = [];
      this.cdr.detectChanges();
      return;
    }
    this.loadFullCurriculum();
  }

  loadFullCurriculum(): void {
    if (!this.selectedCourseId) return;
    this.isLoadingCurriculum = true;
    this.cdr.markForCheck();

    this.contentService.getAllModules(this.selectedCourseId).subscribe({
      next: (modules) => {
        const sortedModules = (modules || []).sort((a, b) => a.moduleOrder - b.moduleOrder);
        this.moduleItems = sortedModules.map(m => ({
          module: m,
          contents: [],
          isExpanded: true,
          isLoading: true
        }));

        if (this.moduleItems.length === 0) {
          this.isLoadingCurriculum = false;
          this.cdr.detectChanges();
          return;
        }

        // Load contents for each module
        let loadedCount = 0;
        this.moduleItems.forEach(item => {
          this.contentService.getAllContents(this.selectedCourseId, item.module.moduleId).subscribe({
            next: (contents) => {
              item.contents = (contents || []).sort((a, b) => a.contentOrder - b.contentOrder);
              item.isLoading = false;
              loadedCount++;
              if (loadedCount >= this.moduleItems.length) {
                this.isLoadingCurriculum = false;
                this.cdr.detectChanges();
              }
            },
            error: () => {
              item.contents = [];
              item.isLoading = false;
              loadedCount++;
              if (loadedCount >= this.moduleItems.length) {
                this.isLoadingCurriculum = false;
                this.cdr.detectChanges();
              }
            }
          });
        });
      },
      error: () => {
        this.moduleItems = [];
        this.isLoadingCurriculum = false;
        this.toastService.showError('Could not load course curriculum.');
        this.cdr.detectChanges();
      }
    });
  }

  toggleModuleExpand(item: ModuleItem): void {
    item.isExpanded = !item.isExpanded;
  }

  // ---- Reordering Handlers ----

  moveModuleUp(index: number): void {
    if (index <= 0) return;
    const current = this.moduleItems[index];
    const prev = this.moduleItems[index - 1];

    const currentOrder = current.module.moduleOrder;
    const prevOrder = prev.module.moduleOrder;

    current.module.moduleOrder = prevOrder;
    prev.module.moduleOrder = currentOrder;

    this.contentService.updateModule(this.selectedCourseId, current.module.moduleId, {
      title: current.module.title,
      description: current.module.description,
      moduleOrder: current.module.moduleOrder
    }).subscribe(() => {
      this.contentService.updateModule(this.selectedCourseId, prev.module.moduleId, {
        title: prev.module.title,
        description: prev.module.description,
        moduleOrder: prev.module.moduleOrder
      }).subscribe(() => {
        this.loadFullCurriculum();
        this.toastService.showSuccess('Module moved up.');
      });
    });
  }

  moveModuleDown(index: number): void {
    if (index >= this.moduleItems.length - 1) return;
    const current = this.moduleItems[index];
    const next = this.moduleItems[index + 1];

    const currentOrder = current.module.moduleOrder;
    const nextOrder = next.module.moduleOrder;

    current.module.moduleOrder = nextOrder;
    next.module.moduleOrder = currentOrder;

    this.contentService.updateModule(this.selectedCourseId, current.module.moduleId, {
      title: current.module.title,
      description: current.module.description,
      moduleOrder: current.module.moduleOrder
    }).subscribe(() => {
      this.contentService.updateModule(this.selectedCourseId, next.module.moduleId, {
        title: next.module.title,
        description: next.module.description,
        moduleOrder: next.module.moduleOrder
      }).subscribe(() => {
        this.loadFullCurriculum();
        this.toastService.showSuccess('Module moved down.');
      });
    });
  }

  moveContentUp(moduleIndex: number, contentIndex: number): void {
    const item = this.moduleItems[moduleIndex];
    if (!item || contentIndex <= 0) return;

    const current = item.contents[contentIndex];
    const prev = item.contents[contentIndex - 1];

    const currentOrder = current.contentOrder;
    const prevOrder = prev.contentOrder;

    current.contentOrder = prevOrder;
    prev.contentOrder = currentOrder;

    this.contentService.updateContent(this.selectedCourseId, item.module.moduleId, current.contentId, {
      title: current.title,
      description: current.description,
      contentType: current.contentType,
      contentUrl: current.contentUrl,
      textContent: current.textContent,
      durationMinutes: current.durationMinutes,
      contentOrder: current.contentOrder,
      mandatory: current.mandatory,
      previewAvailable: current.previewAvailable
    }).subscribe(() => {
      this.contentService.updateContent(this.selectedCourseId, item.module.moduleId, prev.contentId, {
        title: prev.title,
        description: prev.description,
        contentType: prev.contentType,
        contentUrl: prev.contentUrl,
        textContent: prev.textContent,
        durationMinutes: prev.durationMinutes,
        contentOrder: prev.contentOrder,
        mandatory: prev.mandatory,
        previewAvailable: prev.previewAvailable
      }).subscribe(() => {
        this.loadFullCurriculum();
        this.toastService.showSuccess('Lesson order updated.');
      });
    });
  }

  moveContentDown(moduleIndex: number, contentIndex: number): void {
    const item = this.moduleItems[moduleIndex];
    if (!item || contentIndex >= item.contents.length - 1) return;

    const current = item.contents[contentIndex];
    const next = item.contents[contentIndex + 1];

    const currentOrder = current.contentOrder;
    const nextOrder = next.contentOrder;

    current.contentOrder = nextOrder;
    next.contentOrder = currentOrder;

    this.contentService.updateContent(this.selectedCourseId, item.module.moduleId, current.contentId, {
      title: current.title,
      description: current.description,
      contentType: current.contentType,
      contentUrl: current.contentUrl,
      textContent: current.textContent,
      durationMinutes: current.durationMinutes,
      contentOrder: current.contentOrder,
      mandatory: current.mandatory,
      previewAvailable: current.previewAvailable
    }).subscribe(() => {
      this.contentService.updateContent(this.selectedCourseId, item.module.moduleId, next.contentId, {
        title: next.title,
        description: next.description,
        contentType: next.contentType,
        contentUrl: next.contentUrl,
        textContent: next.textContent,
        durationMinutes: next.durationMinutes,
        contentOrder: next.contentOrder,
        mandatory: next.mandatory,
        previewAvailable: next.previewAvailable
      }).subscribe(() => {
        this.loadFullCurriculum();
        this.toastService.showSuccess('Lesson order updated.');
      });
    });
  }

  // ---- Module CRUD ----

  openCreateModuleForm(): void {
    this.isEditingModule = false;
    this.editingModuleId = null;
    this.moduleForm = {
      ...EMPTY_MODULE_FORM,
      moduleOrder: this.moduleItems.length + 1
    };
    this.moduleFormErrors = [];
    this.isModuleFormOpen = true;
  }

  openEditModuleForm(module: CourseModule): void {
    this.isEditingModule = true;
    this.editingModuleId = module.moduleId;
    this.moduleForm = {
      title: module.title,
      description: module.description ?? '',
      moduleOrder: module.moduleOrder
    };
    this.moduleFormErrors = [];
    this.isModuleFormOpen = true;
  }

  closeModuleForm(): void {
    this.isModuleFormOpen = false;
  }

  saveModule(): void {
    const errors: string[] = [];
    if (!this.moduleForm.title.trim()) {
      errors.push('Module title is required.');
    }
    if (!this.moduleForm.moduleOrder || this.moduleForm.moduleOrder < 1) {
      errors.push('Module order must be at least 1.');
    }
    this.moduleFormErrors = errors;
    if (errors.length > 0) {
      return;
    }

    const request$ = this.isEditingModule && this.editingModuleId
      ? this.contentService.updateModule(this.selectedCourseId, this.editingModuleId, this.moduleForm)
      : this.contentService.createModule(this.selectedCourseId, this.moduleForm);

    request$.subscribe({
      next: () => {
        this.isModuleFormOpen = false;
        this.toastService.showSuccess(
          this.isEditingModule ? 'Module updated.' : 'Module created.'
        );
        this.loadFullCurriculum();
      },
      error: (error: unknown) => {
        this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not save module.');
      }
    });
  }

  deleteModule(module: CourseModule): void {
    this.confirmService.ask(
      'Delete Module',
      `Delete module "${module.title}"? All its lessons and learning items will also be removed.`
    ).then((confirmed) => {
      if (!confirmed) return;

      this.contentService.deleteModule(this.selectedCourseId, module.moduleId).subscribe({
        next: () => {
          this.toastService.showSuccess('Module deleted.');
          this.loadFullCurriculum();
        },
        error: (error: unknown) => {
          this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not delete module.');
        }
      });
    });
  }

  togglePublishModule(module: CourseModule): void {
    const request$ = module.published
      ? this.contentService.unpublishModule(this.selectedCourseId, module.moduleId)
      : this.contentService.publishModule(this.selectedCourseId, module.moduleId);

    request$.subscribe({
      next: () => this.loadFullCurriculum(),
      error: (error: unknown) => {
        this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not update module status.');
      }
    });
  }

  openCreateContentInModule(module: CourseModule): void {
    this.selectedModuleId = module.moduleId;
    const targetModuleItem = this.moduleItems.find(m => m.module.moduleId === module.moduleId);
    this.isEditingContent = false;
    this.editingContentId = null;
    this.contentForm = {
      ...EMPTY_CONTENT_FORM,
      contentOrder: (targetModuleItem?.contents?.length || 0) + 1,
      quizQuestions: [...DEFAULT_QUIZ_QUESTIONS.map(q => ({ ...q, options: [...q.options] }))]
    };
    this.contentFormErrors = [];
    this.isContentFormOpen = true;
  }

  openEditContentInModule(module: CourseModule, content: CourseContent): void {
    this.selectedModuleId = module.moduleId;
    this.openEditContentForm(content);
  }

  deleteContentInModule(module: CourseModule, content: CourseContent): void {
    this.confirmService.ask(
      'Delete Learning Item',
      `Delete "${content.title}"? This cannot be undone.`
    ).then((confirmed) => {
      if (!confirmed) return;

      this.contentService
        .deleteContent(this.selectedCourseId, module.moduleId, content.contentId)
        .subscribe({
          next: () => {
            this.toastService.showSuccess('Learning item deleted.');
            this.loadFullCurriculum();
          },
          error: (error: unknown) => {
            this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not delete content.');
          }
        });
    });
  }

  togglePublishContentInModule(module: CourseModule, content: CourseContent): void {
    const request$ = content.published
      ? this.contentService.unpublishContent(this.selectedCourseId, module.moduleId, content.contentId)
      : this.contentService.publishContent(this.selectedCourseId, module.moduleId, content.contentId);

    request$.subscribe({
      next: () => this.loadFullCurriculum(),
      error: (error: unknown) => {
        this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not update content status.');
      }
    });
  }

  // ---- Content CRUD & Resource Types ----

  selectResourceType(type: ModuleResourceType): void {
    this.contentForm.resourceType = type;
    if (type === 'YOUTUBE_VIDEO') {
      this.contentForm.contentType = 'VIDEO';
      if (!this.contentForm.contentUrl) {
        this.contentForm.contentUrl = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
      }
    } else if (type === 'ONLINE_QUIZ') {
      this.contentForm.contentType = 'QUIZ';
      if (!this.contentForm.quizQuestions || this.contentForm.quizQuestions.length === 0) {
        this.contentForm.quizQuestions = [...DEFAULT_QUIZ_QUESTIONS.map(q => ({ ...q, options: [...q.options] }))];
      }
    } else if (type === 'EXTERNAL_READING') {
      this.contentForm.contentType = 'DOCUMENT';
      if (!this.contentForm.contentUrl) {
        this.contentForm.contentUrl = 'https://docs.spring.io/spring-boot/docs/current/reference/html/';
      }
    }
  }

  getYouTubeEmbedUrl(url: string): string {
    if (!url) return '';
    const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
    return match ? `https://www.youtube.com/embed/${match[1]}` : url;
  }

  addQuizQuestion(): void {
    const nextId = (this.contentForm.quizQuestions.length > 0)
      ? Math.max(...this.contentForm.quizQuestions.map(q => q.id)) + 1
      : 1;
    this.contentForm.quizQuestions.push({
      id: nextId,
      question: 'New question description...',
      options: ['Option A', 'Option B', 'Option C', 'Option D'],
      correctOption: 0
    });
  }

  removeQuizQuestion(index: number): void {
    if (this.contentForm.quizQuestions.length > 1) {
      this.contentForm.quizQuestions.splice(index, 1);
    } else {
      this.toastService.showError('A quiz must have at least one question.');
    }
  }

  addQuizOption(qIndex: number): void {
    const q = this.contentForm.quizQuestions[qIndex];
    if (q && q.options.length < 6) {
      q.options.push(`Option ${String.fromCharCode(65 + q.options.length)}`);
    }
  }

  removeQuizOption(qIndex: number, optIndex: number): void {
    const q = this.contentForm.quizQuestions[qIndex];
    if (q && q.options.length > 2) {
      q.options.splice(optIndex, 1);
      if (q.correctOption >= q.options.length) {
        q.correctOption = 0;
      }
    }
  }

  getResourceBadgeClass(contentType: string): string {
    const ct = (contentType || '').toUpperCase();
    if (ct === 'VIDEO') return 'badge-youtube';
    if (ct === 'QUIZ') return 'badge-quiz';
    return 'badge-reading';
  }

  getResourceIcon(contentType: string): string {
    return '';
  }

  getResourceLabel(contentType: string): string {
    const ct = (contentType || '').toUpperCase();
    if (ct === 'VIDEO') return 'YouTube Video';
    if (ct === 'QUIZ') return 'Online Quiz';
    if (ct === 'DOCUMENT' || ct === 'EXTERNAL_LINK' || ct === 'ARTICLE') return 'External Reading';
    return contentType;
  }

  openCreateContentForm(): void {
    this.isEditingContent = false;
    this.editingContentId = null;
    this.contentForm = {
      ...EMPTY_CONTENT_FORM,
      contentOrder: this.contents.length + 1,
      quizQuestions: [...DEFAULT_QUIZ_QUESTIONS.map(q => ({ ...q, options: [...q.options] }))]
    };
    this.contentFormErrors = [];
    this.isContentFormOpen = true;
  }

  openEditContentForm(content: CourseContent): void {
    this.isEditingContent = true;
    this.editingContentId = content.contentId;

    let resType: ModuleResourceType = 'EXTERNAL_READING';
    if (content.contentType === 'VIDEO') {
      resType = 'YOUTUBE_VIDEO';
    } else if (content.contentType === 'QUIZ') {
      resType = 'ONLINE_QUIZ';
    }

    let parsedQuestions: QuizQuestionState[] = [...DEFAULT_QUIZ_QUESTIONS.map(q => ({ ...q, options: [...q.options] }))];
    if (content.contentType === 'QUIZ' && content.textContent) {
      try {
        const parsed = JSON.parse(content.textContent);
        if (Array.isArray(parsed) && parsed.length > 0) {
          parsedQuestions = parsed;
        }
      } catch {
        // fallback to default
      }
    }

    this.contentForm = {
      title: content.title,
      description: content.description ?? '',
      resourceType: resType,
      contentType: content.contentType,
      contentUrl: content.contentUrl ?? '',
      textContent: content.textContent ?? '',
      durationMinutes: content.durationMinutes ?? 15,
      contentOrder: content.contentOrder,
      mandatory: content.mandatory,
      previewAvailable: content.previewAvailable,
      quizPassingScore: 70,
      quizQuestions: parsedQuestions
    };
    this.contentFormErrors = [];
    this.isContentFormOpen = true;
  }

  closeContentForm(): void {
    this.isContentFormOpen = false;
  }

  saveContent(): void {
    const errors: string[] = [];
    if (!this.contentForm.title.trim()) {
      errors.push('Learning resource title is required.');
    }
    if (!this.contentForm.contentType) {
      errors.push('Resource type is required.');
    }
    if (!this.contentForm.contentOrder || this.contentForm.contentOrder < 1) {
      errors.push('Content order must be at least 1.');
    }
    if (this.contentForm.durationMinutes !== null && this.contentForm.durationMinutes < 1) {
      errors.push('Duration must be at least 1 minute if provided.');
    }

    if (this.contentForm.resourceType === 'YOUTUBE_VIDEO') {
      if (!this.contentForm.contentUrl.trim()) {
        errors.push('A valid YouTube video URL is required.');
      }
    } else if (this.contentForm.resourceType === 'EXTERNAL_READING') {
      if (!this.contentForm.contentUrl.trim()) {
        errors.push('A valid study/reading resource URL or document link is required.');
      }
    } else if (this.contentForm.resourceType === 'ONLINE_QUIZ') {
      if (!this.contentForm.quizQuestions || this.contentForm.quizQuestions.length === 0) {
        errors.push('Online quiz must contain at least one question.');
      }
    }

    this.contentFormErrors = errors;
    if (errors.length > 0) {
      return;
    }

    let finalContentUrl = this.contentForm.contentUrl?.trim() || null;
    let finalPayloadText = this.contentForm.textContent || null;

    if (this.contentForm.resourceType === 'YOUTUBE_VIDEO') {
      finalContentUrl = this.getYouTubeEmbedUrl(this.contentForm.contentUrl.trim());
      if (!finalPayloadText) {
        finalPayloadText = `YouTube video lecture: ${this.contentForm.title}`;
      }
    } else if (this.contentForm.resourceType === 'ONLINE_QUIZ') {
      finalPayloadText = JSON.stringify(this.contentForm.quizQuestions);
    }

    const payload = {
      title: this.contentForm.title.trim(),
      description: this.contentForm.description?.trim() || null,
      contentType: this.contentForm.contentType,
      contentUrl: finalContentUrl,
      textContent: finalPayloadText,
      durationMinutes: this.contentForm.durationMinutes,
      contentOrder: this.contentForm.contentOrder,
      mandatory: this.contentForm.mandatory,
      previewAvailable: this.contentForm.previewAvailable
    };

    const request$ = this.isEditingContent && this.editingContentId
      ? this.contentService.updateContent(
          this.selectedCourseId, this.selectedModuleId, this.editingContentId, payload
        )
      : this.contentService.createContent(
          this.selectedCourseId, this.selectedModuleId, payload
        );

    request$.subscribe({
      next: () => {
        this.isContentFormOpen = false;
        this.toastService.showSuccess(
          this.isEditingContent ? 'Learning item updated.' : 'Learning item added to module.'
        );
        this.loadFullCurriculum();
      },
      error: (error: unknown) => {
        this.toastService.showError(this.extractErrorMessage(error) ?? 'Could not save learning item.');
      }
    });
  }

  trackById(index: number, item: { moduleId?: string; contentId?: string }): string {
    return item.moduleId ?? item.contentId ?? String(index);
  }

  private extractErrorMessage(error: unknown): string | null {
    const err = error as { error?: { message?: string } };
    return err?.error?.message ?? null;
  }
}
