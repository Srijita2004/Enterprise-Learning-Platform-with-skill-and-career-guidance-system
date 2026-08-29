import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

import {
  ChangeDetectorRef,
  Component,
  OnInit,
  OnDestroy,
  inject
} from '@angular/core';

import {
  ActivatedRoute,
  RouterLink
} from '@angular/router';

import {
  catchError,
  forkJoin,
  map,
  of,
  switchMap
} from 'rxjs';

import { Course } from '../../models/course.model';
import { Enrollment } from '../../models/enrollment.model';

import {
  ContentProgress,
  ContentProgressUpdateRequest,
  CourseContent,
  CourseModule,
  CourseProgressSummary,
  LearningContentView,
  LearningModuleView
} from '../../models/learning-content.model';

import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { CourseCertificateService } from '../../services/course-certificate.service';
import { LearningContentService } from '../../services/learning-content.service';

import { AuthService } from '../../../core/auth/auth.service';
import { AiAssistantService } from '../../../core/services/ai-assistant.service';

@Component({
  selector: 'app-course-learning',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './course-learning.html',
  styleUrls: ['./course-learning.css']
})
export class CourseLearning implements OnInit, OnDestroy {

  private readonly route =
    inject(ActivatedRoute);

  private readonly courseService =
    inject(CourseService);

  private readonly enrollmentService =
    inject(EnrollmentService);

  private readonly learningContentService =
    inject(LearningContentService);

  private readonly courseCertificateService =
    inject(CourseCertificateService);

  private readonly aiAssistantService =
    inject(AiAssistantService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  private readonly authService = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  course: Course | null = null;
  enrollment: Enrollment | null = null;
  moduleGroups: LearningModuleView[] = [];
  progressSummary: CourseProgressSummary | null = null;

  flatContents: { content: LearningContentView; module: CourseModule }[] = [];
  selectedContent: LearningContentView | null = null;
  selectedModule: CourseModule | null = null;

  // Inline Quiz state
  inlineQuizQuestions: { id: number; question: string; options: string[]; correctOption: number }[] = [];
  selectedQuizAnswers: { [qId: number]: number } = {};
  quizSubmitted = false;
  quizScorePercentage = 0;
  quizPassed = false;

  isLoading = true;
  updatingContentId: string | null = null;
  isFinalizingCourse = false;
  errorMessage = '';
  actionMessage = '';

  ngOnInit(): void {
    const courseId =
      this.route.snapshot.paramMap.get('courseId');

    if (!courseId) {
      this.isLoading = false;
      this.errorMessage =
        'Course ID was not provided.';
      this.changeDetector.detectChanges();
      return;
    }

    this.loadCourseAndEnrollment(courseId);
  }

  ngOnDestroy(): void {
    this.aiAssistantService.clearCourseContext();
  }

  loadCourseAndEnrollment(
    courseId: string
  ): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    forkJoin({
      course:
        this.courseService.getCourseById(courseId),

      enrollment:
        this.enrollmentService
          .getEnrollmentByLearnerAndCourse(
            this.learnerId,
            courseId
          )
    }).subscribe({
      next: ({ course, enrollment }) => {
        this.course = course;
        this.enrollment = enrollment;

        if (!enrollment.accessAllowed) {
          this.isLoading = false;

          this.errorMessage =
            'You do not currently have access to this course. Complete enrollment or payment before starting learning.';

          this.changeDetector.detectChanges();
          return;
        }

        this.loadLearningData(
          course.courseId,
          enrollment.enrollmentId
        );
      },

      error: (error: HttpErrorResponse) => {
        console.error(
          'Failed to load course enrollment:',
          error
        );

        this.isLoading = false;

        if (error.status === 404) {
          this.errorMessage =
            'No enrollment was found for this course.';
        } else {
          this.errorMessage =
            'Course learning information could not be loaded.';
        }

        this.changeDetector.detectChanges();
      }
    });
  }

  loadLearningData(
    courseId: string,
    enrollmentId: string
  ): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      modules:
        this.learningContentService
          .getPublishedModules(courseId),

      progress:
        this.learningContentService
          .getProgressByEnrollment(enrollmentId)
          .pipe(
            catchError((error) => {
              console.error(
                'Progress list could not be loaded:',
                error
              );

              return of([] as ContentProgress[]);
            })
          ),

      summary:
        this.learningContentService
          .getProgressSummary(enrollmentId)
          .pipe(
            catchError((error) => {
              console.error(
                'Progress summary could not be loaded:',
                error
              );

              return of(
                null as CourseProgressSummary | null
              );
            })
          )
    }).subscribe({
      next: ({
        modules,
        progress,
        summary
      }) => {
        this.progressSummary = summary;

        if (modules.length === 0) {
          this.moduleGroups = [];
          this.isLoading = false;

          this.changeDetector.detectChanges();
          return;
        }

        const progressMap =
          new Map<string, ContentProgress>();

        progress.forEach((item) => {
          progressMap.set(
            item.contentId,
            item
          );
        });

        const contentRequests =
          modules.map((courseModule) =>
            this.learningContentService
              .getPublishedContentsForLearner(
                courseId,
                courseModule.moduleId,
                this.learnerId
              )
              .pipe(
                catchError((error) => {
                  console.error(
                    `Contents could not be loaded for module ${courseModule.moduleId}:`,
                    error
                  );

                  return of(
                    [] as CourseContent[]
                  );
                }),

                map((contents) => ({
                  module: courseModule,

                  contents: contents.map(
                    (content): LearningContentView => ({
                      ...content,

                      progress:
                        progressMap.get(
                          content.contentId
                        ) ?? null
                    })
                  )
                }))
              )
          );

        forkJoin(contentRequests)
          .subscribe({
            next: (moduleGroups) => {
              this.moduleGroups = moduleGroups;

              // Build flat contents list in exact sequence
              this.flatContents = [];
              moduleGroups.forEach(g => {
                g.contents.forEach(c => {
                  this.flatContents.push({ content: c, module: g.module });
                });
              });

              // Auto-select first in-progress or not-started content, or first content
              if (!this.selectedContent && this.flatContents.length > 0) {
                const inProgress = this.flatContents.find(item => {
                  const p = item.content.progress?.progressPercentage || 0;
                  return p > 0 && p < 100;
                });
                const notStarted = this.flatContents.find(item => {
                  const p = item.content.progress?.progressPercentage || 0;
                  return p === 0;
                });
                const target = inProgress || notStarted || this.flatContents[0];
                this.selectContent(target.content, target.module);
              } else if (this.selectedContent) {
                const refreshed = this.flatContents.find(i => i.content.contentId === this.selectedContent?.contentId);
                if (refreshed) {
                  this.selectedContent = refreshed.content;
                  this.selectedModule = refreshed.module;
                }
              }

              if (summary) {
                this.finalizeCourseIfEligible(summary);
              }

              this.isLoading = false;
              this.changeDetector.detectChanges();
            },

            error: (error: unknown) => {
              console.error(
                'Learning contents could not be loaded:',
                error
              );

              this.moduleGroups = [];
              this.flatContents = [];
              this.isLoading = false;

              this.errorMessage =
                'Learning contents could not be loaded.';

              this.changeDetector.detectChanges();
            }
          });
      },

      error: (error: unknown) => {
        console.error(
          'Learning modules could not be loaded:',
          error
        );

        this.isLoading = false;

        this.errorMessage =
          'Learning modules could not be loaded.';

        this.changeDetector.detectChanges();
      }
    });
  }

  selectContent(content: LearningContentView, module: CourseModule): void {
    this.selectedContent = content;
    this.selectedModule = module;
    this.actionMessage = '';

    // Synchronize active learning context with EIWI
    if (this.course) {
      this.aiAssistantService.setCourseContext({
        courseId: this.course.courseId,
        courseTitle: this.course.title,
        moduleId: module.moduleId,
        moduleTitle: module.title,
        contentId: content.contentId,
        contentTitle: content.title
      });
    }

    // If not started, advance to 25%
    if (!content.progress || content.progress.progressPercentage === 0) {
      this.updateContentProgress(content, 25);
    }

    // If quiz, parse questions
    if (this.isAssessmentContent(content)) {
      this.inlineQuizQuestions = [];
      if (content.textContent) {
        try {
          const parsed = JSON.parse(content.textContent);
          if (Array.isArray(parsed) && parsed.length > 0) {
            this.inlineQuizQuestions = parsed;
          }
        } catch {
          // fallback
        }
      }
      if (this.inlineQuizQuestions.length === 0) {
        this.inlineQuizQuestions = [
          {
            id: 1,
            question: `What is the core takeaway of ${content.title}?`,
            options: [
              'Architectural decoupling, resilience, and clean design patterns',
              'Direct tightly-coupled dependencies',
              'Disabling unit and integration tests',
              'Single point of failure deployments'
            ],
            correctOption: 0
          },
          {
            id: 2,
            question: 'Which method best supports enterprise high throughput?',
            options: [
              'Asynchronous non-blocking pipelines with event streams',
              'Synchronous blocking infinite loops',
              'Ignoring database index execution plans',
              'Unbounded memory consumption'
            ],
            correctOption: 0
          }
        ];
      }
      this.selectedQuizAnswers = {};
      this.quizSubmitted = this.isContentCompleted(content);
      this.quizScorePercentage = this.isContentCompleted(content) ? 100 : 0;
      this.quizPassed = this.isContentCompleted(content);
    }
    this.changeDetector.detectChanges();
  }

  isContentCompleted(content: LearningContentView): boolean {
    return content.progress?.status === 'COMPLETED' || (content.progress?.progressPercentage || 0) >= 100;
  }

  isContentCurrent(content: LearningContentView): boolean {
    return this.selectedContent?.contentId === content.contentId;
  }

  isContentLocked(content: LearningContentView): boolean {
    const idx = this.flatContents.findIndex(i => i.content.contentId === content.contentId);
    if (idx <= 0) return false;
    if (content.previewAvailable) return false;
    const prev = this.flatContents[idx - 1];
    if (prev && prev.content.mandatory && !this.isContentCompleted(prev.content) && !this.isContentCurrent(prev.content)) {
      return true;
    }
    return false;
  }

  get currentFlatIndex(): number {
    if (!this.selectedContent) return -1;
    return this.flatContents.findIndex(i => i.content.contentId === this.selectedContent?.contentId);
  }

  hasNextContent(): boolean {
    const idx = this.currentFlatIndex;
    return idx >= 0 && idx < this.flatContents.length - 1;
  }

  hasPreviousContent(): boolean {
    const idx = this.currentFlatIndex;
    return idx > 0;
  }

  goToNextContent(): void {
    const idx = this.currentFlatIndex;
    if (idx >= 0 && idx < this.flatContents.length - 1) {
      const next = this.flatContents[idx + 1];
      this.selectContent(next.content, next.module);
    }
  }

  goToPreviousContent(): void {
    const idx = this.currentFlatIndex;
    if (idx > 0) {
      const prev = this.flatContents[idx - 1];
      this.selectContent(prev.content, prev.module);
    }
  }

  completeAndGoNext(): void {
    if (!this.selectedContent) return;
    this.completeContent(this.selectedContent);
    if (this.hasNextContent()) {
      setTimeout(() => {
        this.goToNextContent();
      }, 400);
    }
  }

  selectQuizOption(questionId: number, optionIndex: number): void {
    this.selectedQuizAnswers[questionId] = optionIndex;
  }

  submitInlineQuiz(): void {
    if (this.inlineQuizQuestions.length === 0 || !this.selectedContent) return;

    let correct = 0;
    this.inlineQuizQuestions.forEach(q => {
      if (this.selectedQuizAnswers[q.id] === q.correctOption) {
        correct++;
      }
    });

    this.quizScorePercentage = Math.round((correct / this.inlineQuizQuestions.length) * 100);
    this.quizSubmitted = true;
    this.quizPassed = this.quizScorePercentage >= 70;

    if (this.quizPassed) {
      this.completeContent(this.selectedContent);
      this.actionMessage = `Quiz Passed with ${this.quizScorePercentage}%! Lesson marked as completed.`;
    } else {
      this.actionMessage = `Quiz score ${this.quizScorePercentage}%. Passing score is 70%. Please review the questions and try again.`;
    }
    this.changeDetector.detectChanges();
  }

  resetInlineQuiz(): void {
    this.selectedQuizAnswers = {};
    this.quizSubmitted = false;
    this.quizScorePercentage = 0;
    this.quizPassed = false;
    this.actionMessage = '';
    this.changeDetector.detectChanges();
  }

  updateContentProgress(
    content: LearningContentView,
    targetPercentage: number
  ): void {
    if (
      !this.enrollment ||
      this.updatingContentId !== null
    ) {
      return;
    }

    const safePercentage =
      Math.max(
        0,
        Math.min(100, targetPercentage)
      );

    const durationSeconds =
      Math.max(
        0,
        (content.durationMinutes ?? 0) * 60
      );

    const lastPositionSeconds =
      safePercentage === 100
        ? durationSeconds
        : Math.round(
          durationSeconds *
          safePercentage /
          100
        );

    const request:
      ContentProgressUpdateRequest = {

      progressPercentage:
        safePercentage,

      lastPositionSeconds,

      additionalTimeSpentSeconds: 60
    };

    this.updatingContentId =
      content.contentId;

    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.learningContentService
      .updateContentProgress(
        this.enrollment.enrollmentId,
        content.contentId,
        request
      )
      .subscribe({
        next: (updatedProgress) => {
          this.replaceContentProgress(
            updatedProgress
          );

          this.updatingContentId = null;

          this.actionMessage =
            safePercentage === 100
              ? `${content.title} completed successfully.`
              : `${content.title} progress updated to ${safePercentage}%.`;

          this.reloadProgressSummary();

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Content progress update failed:',
            error
          );

          this.updatingContentId = null;

          this.actionMessage =
            this.getBackendErrorMessage(
              error,
              'Content progress could not be updated.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  continueContent(
    content: LearningContentView
  ): void {
    const currentProgress =
      this.getProgressPercentage(content);

    let nextProgress: number;

    if (currentProgress === 0) {
      nextProgress = 25;
    } else if (currentProgress < 50) {
      nextProgress = 50;
    } else if (currentProgress < 75) {
      nextProgress = 75;
    } else {
      nextProgress = 100;
    }

    this.updateContentProgress(
      content,
      nextProgress
    );
  }

  completeContent(
    content: LearningContentView
  ): void {
    this.updateContentProgress(
      content,
      100
    );
  }

  reloadProgressSummary(): void {
    if (!this.enrollment) {
      return;
    }

    this.learningContentService
      .getProgressSummary(
        this.enrollment.enrollmentId
      )
      .subscribe({
        next: (summary) => {
          this.progressSummary = summary;

          this.finalizeCourseIfEligible(summary);

          this.changeDetector.detectChanges();
        },

        error: (error: unknown) => {
          console.error(
            'Progress summary refresh failed:',
            error
          );
        }
      });
  }

  private finalizeCourseIfEligible(
    summary: CourseProgressSummary
  ): void {
    if (
      !this.enrollment ||
      this.isFinalizingCourse ||
      this.enrollment.status === 'COMPLETED' ||
      summary.totalPublishedContents <= 0 ||
      summary.completedContents !== summary.totalPublishedContents ||
      !summary.allMandatoryContentsCompleted
    ) {
      return;
    }

    this.isFinalizingCourse = true;
    this.actionMessage = 'Finalizing course completion...';

    const enrollmentId = this.enrollment.enrollmentId;
    const recipientName =
      this.authService.currentUser()?.name?.trim() || 'Enterprise Learner';

    this.enrollmentService
      .completeCourse(enrollmentId)
      .pipe(
        switchMap((completion) => {
          if (this.enrollment) {
            this.enrollment = {
              ...this.enrollment,
              status: 'COMPLETED',
              completedAt: completion.completedAt ?? null,
              accessAllowed: true
            };
          }

          if (!completion.certificateEligible) {
            return of(null);
          }

          return this.courseCertificateService
            .issueCertificate(
              completion.completionId,
              {
                recipientName
              }
            );
        })
      )
      .subscribe({
        next: (certificate) => {
          this.isFinalizingCourse = false;
          this.actionMessage = certificate
            ? 'Course completed and certificate generated successfully.'
            : 'Course completed successfully.';
          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error('Course finalization failed:', error);
          this.isFinalizingCourse = false;
          this.actionMessage = this.getBackendErrorMessage(
            error,
            'Course completion could not be finalized.'
          );
          this.changeDetector.detectChanges();
        }
      });
  }

  replaceContentProgress(
    updatedProgress: ContentProgress
  ): void {
    this.moduleGroups =
      this.moduleGroups.map((group) => ({
        ...group,

        contents: group.contents.map(
          (content) =>
            content.contentId ===
            updatedProgress.contentId
              ? {
                ...content,
                progress: updatedProgress
              }
              : content
        )
      }));
  }

  getProgressPercentage(
    content: LearningContentView
  ): number {
    return content.progress
      ?.progressPercentage ?? 0;
  }

  getProgressStatus(
    content: LearningContentView
  ): string {
    return content.progress
      ?.status ?? 'NOT_STARTED';
  }

  getActionLabel(
    content: LearningContentView
  ): string {
    if (
      this.updatingContentId ===
      content.contentId
    ) {
      return 'Updating...';
    }

    const progress =
      this.getProgressPercentage(content);

    if (progress === 0) {
      return 'Start Content';
    }

    return 'Continue Learning';
  }

  getAssessmentActionLabel(
    content: LearningContentView
  ): string {
    return this.getProgressPercentage(content) === 100
      ? 'Retake Quiz'
      : 'Take Quiz';
  }

  isAssessmentContent(
    content: LearningContentView
  ): boolean {
    const contentType =
      content.contentType
        ?.toUpperCase();

    return contentType === 'QUIZ'
      || contentType === 'ASSIGNMENT'
      || contentType === 'ASSESSMENT';
  }

  isYouTubeVideo(content: LearningContentView): boolean {
    const ct = (content.contentType || '').toUpperCase();
    const url = (content.contentUrl || '').toLowerCase();
    return ct === 'VIDEO' || url.includes('youtube.com') || url.includes('youtu.be');
  }

  isReadingContent(content: LearningContentView): boolean {
    const ct = (content.contentType || '').toUpperCase();
    return ct === 'DOCUMENT' || ct === 'EXTERNAL_LINK' || ct === 'ARTICLE' || ct === 'PDF' || ct === 'TEXT';
  }

  getSafeYouTubeEmbed(url?: string | null): SafeResourceUrl | null {
    if (!url) return null;
    const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
    const embedUrl = match ? `https://www.youtube.com/embed/${match[1]}?rel=0&autoplay=0` : url;
    return this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
  }

  getYouTubeDirectWatchUrl(url?: string | null): string {
    if (!url) return '';
    const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
    return match ? `https://www.youtube.com/watch?v=${match[1]}` : url;
  }

  openContent(
    content: LearningContentView
  ): void {
    if (!content.contentUrl) {
      return;
    }

    const targetUrl = this.isYouTubeVideo(content)
      ? this.getYouTubeDirectWatchUrl(content.contentUrl)
      : content.contentUrl;

    window.open(
      targetUrl,
      '_blank',
      'noopener,noreferrer'
    );
  }

  getResourceBadgeClass(contentType?: string): string {
    const ct = (contentType || '').toUpperCase();
    if (ct === 'VIDEO') return 'badge-youtube';
    if (ct === 'QUIZ' || ct === 'ASSESSMENT') return 'badge-quiz';
    return 'badge-reading';
  }

  getResourceIcon(contentType?: string): string {
    return '';
  }

  getResourceLabel(contentType?: string): string {
    const ct = (contentType || '').toUpperCase();
    if (ct === 'VIDEO') return 'YouTube Video';
    if (ct === 'QUIZ' || ct === 'ASSESSMENT') return 'Online Quiz';
    if (ct === 'DOCUMENT' || ct === 'EXTERNAL_LINK' || ct === 'ARTICLE') return 'External Reading';
    return contentType || 'Learning Resource';
  }

  getContentIcon(
    contentType: string
  ): string {
    return this.getResourceIcon(contentType);
  }

  formatTime(
    totalSeconds?: number | null
  ): string {
    if (!totalSeconds) {
      return '0 min';
    }

    const totalMinutes =
      Math.floor(totalSeconds / 60);

    const hours =
      Math.floor(totalMinutes / 60);

    const minutes =
      totalMinutes % 60;

    if (hours === 0) {
      return `${minutes} min`;
    }

    return `${hours}h ${minutes}m`;
  }

  trackModuleById(
    index: number,
    group: LearningModuleView
  ): string {
    return group.module.moduleId;
  }

  trackContentById(
    index: number,
    content: LearningContentView
  ): string {
    return content.contentId;
  }

  private getBackendErrorMessage(
    error: HttpErrorResponse,
    fallbackMessage: string
  ): string {
    const backendMessage =
      error.error?.message ||
      error.error?.error ||
      error.error?.details;

    if (
      typeof backendMessage === 'string' &&
      backendMessage.trim().length > 0
    ) {
      return backendMessage;
    }

    return fallbackMessage;
  }
}