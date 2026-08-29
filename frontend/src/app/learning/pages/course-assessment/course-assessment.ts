import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import {
  ChangeDetectorRef,
  Component,
  OnInit,
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
import { CourseContent, CourseModule } from '../../models/learning-content.model';

import {
  AssessmentAttempt,
  AssessmentGradeRequest
} from '../../models/assessment-model';

import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { AssessmentAttemptService } from '../../services/assessment-attempt.service';
import { LearningContentService } from '../../services/learning-content.service';

interface AssessmentQuestion {
  id: number;
  question: string;
  options: string[];
  correctOption: number;
}

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-course-assessment',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './course-assessment.html',
  styleUrls: ['./course-assessment.css']
})
export class CourseAssessment implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly courseService =
    inject(CourseService);

  private readonly enrollmentService =
    inject(EnrollmentService);

  private readonly assessmentService =
    inject(AssessmentAttemptService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  private readonly authService = inject(AuthService);
  private readonly contentService = inject(LearningContentService);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  questions: AssessmentQuestion[] = [
    {
      id: 1,
      question:
        'Which annotation is commonly used on the main Spring Boot application class?',
      options: [
        '@SpringBootApplication',
        '@SpringEntity',
        '@EnableJava',
        '@RestService'
      ],
      correctOption: 0
    },
    {
      id: 2,
      question:
        'What is the main purpose of dependency injection?',
      options: [
        'To increase database size',
        'To provide required objects automatically',
        'To compile JavaScript',
        'To create HTML pages'
      ],
      correctOption: 1
    },
    {
      id: 3,
      question:
        'What is the role of Eureka in a microservices architecture?',
      options: [
        'Database management',
        'Service discovery',
        'Frontend styling',
        'PDF generation'
      ],
      correctOption: 1
    },
    {
      id: 4,
      question:
        'Which HTTP method is normally used to retrieve data?',
      options: [
        'POST',
        'DELETE',
        'GET',
        'PATCH'
      ],
      correctOption: 2
    },
    {
      id: 5,
      question:
        'Which file commonly stores Spring Boot application configuration?',
      options: [
        'index.html',
        'application.properties',
        'package.json',
        'angular.json'
      ],
      correctOption: 1
    }
  ];

  course: Course | null = null;

  enrollment: Enrollment | null = null;

  attemptHistory: AssessmentAttempt[] = [];

  latestAttempt: AssessmentAttempt | null = null;

  currentAttempt: AssessmentAttempt | null = null;

  answers: Record<number, number> = {};

  isLoading = true;

  isActionRunning = false;

  quizVisible = false;

  errorMessage = '';

  actionMessage = '';

  ngOnInit(): void {
    const courseId =
      this.route.snapshot.paramMap.get('courseId');

    const contentId =
      this.route.snapshot.paramMap.get('contentId');

    if (!courseId || !contentId) {
      this.isLoading = false;

      this.errorMessage =
        'Course ID or assessment content ID was not provided.';

      this.changeDetector.detectChanges();
      return;
    }

    this.loadAssessmentPage(
      courseId,
      contentId
    );
  }

  loadAssessmentPage(
    courseId: string,
    contentId: string
  ): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    forkJoin({
      course:
        this.courseService
          .getCourseById(courseId),

      enrollment:
        this.enrollmentService
          .getEnrollmentByLearnerAndCourse(
            this.learnerId,
            courseId
          ),

      contents:
        this.contentService
          .getPublishedModules(courseId)
          .pipe(
            switchMap((modules: CourseModule[]) => {
              if (!modules || modules.length === 0) return of<CourseContent[]>([]);
              const calls = modules.map((m: CourseModule) =>
                this.contentService
                  .getPublishedContentsForLearner(courseId, m.moduleId, this.learnerId)
                  .pipe(catchError(() => of<CourseContent[]>([])))
              );
              return forkJoin(calls).pipe(map((arr: CourseContent[][]) => arr.flat()));
            }),
            catchError(() => of<CourseContent[]>([]))
          )
    }).subscribe({
      next: ({ course, enrollment, contents }) => {
        this.course = course;
        this.enrollment = enrollment;

        const currentContent = (contents as CourseContent[]).find((c: CourseContent) => c.contentId === contentId);
        if (currentContent?.textContent) {
          try {
            const customQuestions = JSON.parse(currentContent.textContent);
            if (Array.isArray(customQuestions) && customQuestions.length > 0) {
              this.questions = customQuestions;
            }
          } catch {
            // Keep default benchmark questions
          }
        }

        if (!enrollment.accessAllowed) {
          this.isLoading = false;

          this.errorMessage =
            'You do not have access to this assessment. Complete enrollment or payment first.';

          this.changeDetector.detectChanges();
          return;
        }

        this.loadAttemptHistory(
          enrollment.enrollmentId,
          contentId
        );
      },

      error: (error: HttpErrorResponse) => {
        console.error(
          'Assessment page loading failed:',
          error
        );

        this.isLoading = false;

        if (error.status === 404) {
          this.errorMessage =
            'No valid enrollment was found for this course.';
        } else {
          this.errorMessage =
            'Assessment information could not be loaded.';
        }

        this.changeDetector.detectChanges();
      }
    });
  }

  loadAttemptHistory(
    enrollmentId: string,
    contentId: string
  ): void {
    this.assessmentService
      .getAttemptsByEnrollmentAndContent(
        enrollmentId,
        contentId
      )
      .pipe(
        catchError((error) => {
          console.error(
            'Attempt history could not be loaded:',
            error
          );

          return of(
            [] as AssessmentAttempt[]
          );
        })
      )
      .subscribe({
        next: (attempts) => {
          this.attemptHistory =
            [...attempts].sort(
              (first, second) =>
                second.attemptNumber -
                first.attemptNumber
            );

          this.latestAttempt =
            this.attemptHistory[0] ?? null;

          this.currentAttempt =
            this.latestAttempt?.status ===
            'IN_PROGRESS'
              ? this.latestAttempt
              : null;

          this.isLoading = false;

          this.changeDetector.detectChanges();
        },

        error: (error: unknown) => {
          console.error(
            'Assessment attempts failed:',
            error
          );

          this.isLoading = false;

          this.errorMessage =
            'Assessment attempts could not be loaded.';

          this.changeDetector.detectChanges();
        }
      });
  }

  startOrContinueAssessment(): void {
    if (
      !this.enrollment ||
      this.isActionRunning
    ) {
      return;
    }

    if (
      this.latestAttempt?.status ===
      'IN_PROGRESS'
    ) {
      this.currentAttempt =
        this.latestAttempt;

      this.answers = {};
      this.quizVisible = true;
      this.actionMessage = '';
      this.errorMessage = '';

      this.changeDetector.detectChanges();
      return;
    }

    const contentId =
      this.route.snapshot.paramMap.get(
        'contentId'
      );

    if (!contentId) {
      this.errorMessage =
        'Assessment content ID is missing.';

      this.changeDetector.detectChanges();
      return;
    }

    this.isActionRunning = true;
    this.actionMessage = '';
    this.errorMessage = '';

    this.changeDetector.detectChanges();

    this.assessmentService
      .startAttempt(
        this.enrollment.enrollmentId,
        contentId
      )
      .subscribe({
        next: (attempt) => {
          this.currentAttempt = attempt;
          this.latestAttempt = attempt;

          this.attemptHistory = [
            attempt,
            ...this.attemptHistory.filter(
              (existingAttempt) =>
                existingAttempt.attemptId !==
                attempt.attemptId
            )
          ];

          this.answers = {};
          this.quizVisible = true;
          this.isActionRunning = false;

          this.actionMessage =
            `Assessment attempt ${attempt.attemptNumber} started.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Assessment attempt start failed:',
            error
          );

          this.isActionRunning = false;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Assessment attempt could not be started.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  submitAssessment(): void {
    if (
      !this.currentAttempt ||
      this.isActionRunning
    ) {
      return;
    }

    if (!this.allQuestionsAnswered()) {
      this.errorMessage =
        'Please answer all questions before submitting.';

      this.changeDetector.detectChanges();
      return;
    }

    const correctAnswers =
      this.calculateCorrectAnswers();

    const maximumScore = 100;

    const score =
      Math.round(
        correctAnswers *
        maximumScore /
        this.questions.length
      );

    const gradeRequest:
      AssessmentGradeRequest = {

      score,

      maximumScore,

      feedback:
        this.buildFeedback(
          score,
          this.currentAttempt.passingScore
        )
    };

    this.isActionRunning = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.assessmentService
      .submitAttempt(
        this.currentAttempt.attemptId
      )
      .pipe(
        switchMap((submittedAttempt) =>
          this.assessmentService
            .gradeAttempt(
              submittedAttempt.attemptId,
              gradeRequest
            )
        )
      )
      .subscribe({
        next: (gradedAttempt) => {
          this.currentAttempt =
            gradedAttempt;

          this.latestAttempt =
            gradedAttempt;

          this.attemptHistory = [
            gradedAttempt,
            ...this.attemptHistory.filter(
              (attempt) =>
                attempt.attemptId !==
                gradedAttempt.attemptId
            )
          ];

          this.quizVisible = false;
          this.isActionRunning = false;

          this.actionMessage =
            gradedAttempt.status === 'PASSED'
              ? `Assessment passed with ${gradedAttempt.scorePercentage}%.`
              : `Assessment completed with ${gradedAttempt.scorePercentage}%. You may try again.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Assessment submission failed:',
            error
          );

          this.isActionRunning = false;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Assessment could not be submitted and graded.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  allQuestionsAnswered(): boolean {
    return this.questions.every(
      (question) =>
        this.answers[question.id] !==
        undefined
    );
  }

  calculateCorrectAnswers(): number {
    return this.questions.filter(
      (question) =>
        Number(
          this.answers[question.id]
        ) === question.correctOption
    ).length;
  }

  getStartButtonLabel(): string {
    if (this.isActionRunning) {
      return 'Please Wait...';
    }

    if (
      this.latestAttempt?.status ===
      'IN_PROGRESS'
    ) {
      return 'Continue Current Attempt';
    }

    if (
      this.latestAttempt?.status ===
      'PASSED'
    ) {
      return 'Retake Assessment';
    }

    if (
      this.latestAttempt?.status ===
      'FAILED'
    ) {
      return 'Try Again';
    }

    return 'Start Assessment';
  }

  getScorePercentage(
    attempt: AssessmentAttempt
  ): number {
    return attempt.scorePercentage ?? 0;
  }

  getResultText(
    attempt: AssessmentAttempt
  ): string {
    switch (attempt.status) {
      case 'PASSED':
        return 'Assessment Passed';

      case 'FAILED':
        return 'Assessment Not Passed';

      case 'SUBMITTED':
        return 'Assessment Submitted';

      case 'IN_PROGRESS':
        return 'Attempt In Progress';

      default:
        return 'Not Attempted';
    }
  }

  trackQuestionById(
    index: number,
    question: AssessmentQuestion
  ): number {
    return question.id;
  }

  trackAttemptById(
    index: number,
    attempt: AssessmentAttempt
  ): string {
    return attempt.attemptId;
  }

  private buildFeedback(
    score: number,
    passingScore: number
  ): string {
    if (score >= passingScore) {
      return 'Good work. You have successfully passed the assessment.';
    }

    return 'Review the Spring Boot concepts and attempt the assessment again.';
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