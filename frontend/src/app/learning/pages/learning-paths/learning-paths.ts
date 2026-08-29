import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { RouterLink } from '@angular/router';

import {
  catchError,
  forkJoin,
  of
} from 'rxjs';

import {
  LearningPath,
  LearningPathAssignment,
  LearningPathCourse
} from '../../models/learning-path.model';

import {
  LearningPathService
} from '../../services/learning-path.service';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-learning-paths',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './learning-paths.html',
  styleUrls: ['./learning-paths.css']
})
export class LearningPaths implements OnInit {

  private readonly learningPathService =
    inject(LearningPathService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  private readonly authService = inject(AuthService);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  learningPaths: LearningPath[] = [];

  assignments: LearningPathAssignment[] = [];

  pathCourses:
    Record<string, LearningPathCourse[]> = {};

  expandedPathId: string | null = null;

  loadingCoursesPathId: string | null = null;

  processingPathId: string | null = null;

  processingAssignmentId: string | null =
    null;

  isLoading = true;

  errorMessage = '';

  actionMessage = '';

  ngOnInit(): void {
    this.loadLearningPathPage();
  }

  loadLearningPathPage(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    forkJoin({
      paths:
        this.learningPathService
          .getAllLearningPaths()
          .pipe(
            catchError((error) => {
              console.error(
                'Learning paths could not be loaded:',
                error
              );

              return of([] as LearningPath[]);
            })
          ),

      assignments:
        this.learningPathService
          .getAssignmentsByLearner(
            this.learnerId
          )
          .pipe(
            catchError((error) => {
              console.error(
                'Learner assignments could not be loaded:',
                error
              );

              return of(
                [] as LearningPathAssignment[]
              );
            })
          )
    }).subscribe({
      next: ({ paths, assignments }) => {
        this.learningPaths =
          [...paths].sort(
            (first, second) => {
              const firstPublished =
                first.status === 'PUBLISHED'
                  ? 0
                  : 1;

              const secondPublished =
                second.status === 'PUBLISHED'
                  ? 0
                  : 1;

              if (
                firstPublished !==
                secondPublished
              ) {
                return (
                  firstPublished -
                  secondPublished
                );
              }

              return first.title.localeCompare(
                second.title
              );
            }
          );

        this.assignments =
          [...assignments].sort(
            (first, second) =>
              new Date(
                second.assignedAt ??
                second.createdAt ??
                0
              ).getTime() -
              new Date(
                first.assignedAt ??
                first.createdAt ??
                0
              ).getTime()
          );

        this.isLoading = false;

        this.changeDetector.detectChanges();
      },

      error: (error: unknown) => {
        console.error(
          'Learning path page loading failed:',
          error
        );

        this.isLoading = false;

        this.errorMessage =
          'Learning paths could not be loaded.';

        this.changeDetector.detectChanges();
      }
    });
  }

  assignLearningPath(
    path: LearningPath
  ): void {
    if (
      path.status !== 'PUBLISHED' ||
      this.processingPathId !== null ||
      this.getAssignmentForPath(
        path.pathId
      )
    ) {
      return;
    }

    this.processingPathId =
      path.pathId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.learningPathService
      .assignLearningPath(
        path.pathId,
        {
          learnerId: this.learnerId,
          assignmentSource:
            'SELF_ASSIGNED'
        }
      )
      .subscribe({
        next: (assignment) => {
          this.assignments = [
            assignment,
            ...this.assignments
          ];

          this.processingPathId = null;

          this.actionMessage =
            `${path.title} was assigned successfully.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Learning path assignment failed:',
            error
          );

          this.processingPathId = null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Learning path could not be assigned.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  startLearningPath(
    assignment: LearningPathAssignment
  ): void {
    if (
      assignment.status !== 'ASSIGNED' ||
      this.processingAssignmentId !== null
    ) {
      return;
    }

    this.processingAssignmentId =
      assignment.assignmentId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.learningPathService
      .startLearningPath(
        assignment.assignmentId
      )
      .subscribe({
        next: (updatedAssignment) => {
          this.replaceAssignment(
            updatedAssignment
          );

          this.processingAssignmentId =
            null;

          this.actionMessage =
            `${updatedAssignment.pathTitle} was started successfully.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Learning path start failed:',
            error
          );

          this.processingAssignmentId =
            null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Learning path could not be started.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  refreshProgress(
    assignment: LearningPathAssignment
  ): void {
    if (
      this.processingAssignmentId !== null
    ) {
      return;
    }

    this.processingAssignmentId =
      assignment.assignmentId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.learningPathService
      .refreshProgress(
        assignment.assignmentId
      )
      .subscribe({
        next: (updatedAssignment) => {
          this.replaceAssignment(
            updatedAssignment
          );

          this.processingAssignmentId =
            null;

          this.actionMessage =
            `${updatedAssignment.pathTitle} progress was refreshed.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Learning path progress refresh failed:',
            error
          );

          this.processingAssignmentId =
            null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Learning path progress could not be refreshed.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  toggleCourseSequence(
    path: LearningPath
  ): void {
    if (
      this.expandedPathId ===
      path.pathId
    ) {
      this.expandedPathId = null;

      this.changeDetector.detectChanges();
      return;
    }

    if (
      this.pathCourses[path.pathId]
    ) {
      this.expandedPathId =
        path.pathId;

      this.changeDetector.detectChanges();
      return;
    }

    this.loadingCoursesPathId =
      path.pathId;

    this.errorMessage = '';

    this.changeDetector.detectChanges();

    this.learningPathService
      .getLearningPathCourses(
        path.pathId
      )
      .subscribe({
        next: (courses) => {
          this.pathCourses[path.pathId] =
            [...courses].sort(
              (first, second) =>
                first.courseOrder -
                second.courseOrder
            );

          this.loadingCoursesPathId =
            null;

          this.expandedPathId =
            path.pathId;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Learning path courses loading failed:',
            error
          );

          this.loadingCoursesPathId =
            null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Learning path course sequence could not be loaded.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  getAssignmentForPath(
    pathId: string
  ): LearningPathAssignment | null {
    return (
      this.assignments.find(
        (assignment) =>
          assignment.pathId === pathId &&
          assignment.status !== 'CANCELLED' &&
          assignment.status !== 'EXPIRED'
      ) ?? null
    );
  }

  getCoursesForPath(
    pathId: string
  ): LearningPathCourse[] {
    return this.pathCourses[pathId] ?? [];
  }

  getProgressPercentage(
    assignment: LearningPathAssignment
  ): number {
    const progress =
      Number(
        assignment.progressPercentage ?? 0
      );

    return Math.max(
      0,
      Math.min(100, progress)
    );
  }

  replaceAssignment(
    updatedAssignment:
      LearningPathAssignment
  ): void {
    this.assignments =
      this.assignments.map(
        (assignment) =>
          assignment.assignmentId ===
          updatedAssignment.assignmentId
            ? updatedAssignment
            : assignment
      );
  }

  formatEnumLabel(
    value?: string | null
  ): string {
    if (!value) {
      return 'Not specified';
    }

    return value
      .toLowerCase()
      .split('_')
      .map(
        (word) =>
          word.charAt(0).toUpperCase() +
          word.slice(1)
      )
      .join(' ');
  }

  trackPathById(
    index: number,
    path: LearningPath
  ): string {
    return path.pathId;
  }

  trackAssignmentById(
    index: number,
    assignment: LearningPathAssignment
  ): string {
    return assignment.assignmentId;
  }

  trackPathCourseById(
    index: number,
    course: LearningPathCourse
  ): string {
    return course.pathCourseId;
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