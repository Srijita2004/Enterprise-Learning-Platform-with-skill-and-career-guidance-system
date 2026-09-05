import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../../core/auth/auth.service';
import {
  DirectoryUser,
  UserDirectoryService
} from '../../../core/services/user-directory.service';
import { Course } from '../../../learning/models/course.model';
import {
  LearningPath,
  LearningPathAssignment,
  LearningPathCourse,
  LearningPathCreateRequest,
  LearningPathUpdateRequest
} from '../../../learning/models/learning-path.model';
import { CourseService } from '../../../learning/services/course.service';
import { LearningPathService } from '../../../learning/services/learning-path.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { ToastService } from '../../../shared/services/toast.service';
import { NotificationService } from '../../../core/services/notification.service';

interface PathForm {
  pathCode: string;
  title: string;
  description: string;
  category: string;
  targetRole: string;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
  estimatedDurationHours: number;
}

const EMPTY_FORM: PathForm = {
  pathCode: '',
  title: '',
  description: '',
  category: '',
  targetRole: '',
  level: 'BEGINNER',
  estimatedDurationHours: 1
};

@Component({
  selector: 'app-learning-path-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './learning-path-management.html',
  styleUrls: ['./learning-path-management.css']
})
export class LearningPathManagement implements OnInit {
  private readonly learningPathService = inject(LearningPathService);
  private readonly courseService = inject(CourseService);
  private readonly userDirectoryService = inject(UserDirectoryService);
  private readonly authService = inject(AuthService);
  private readonly notifService = inject(NotificationService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  // Initialize IMMEDIATELY with existing paths so the list is displayed on first load without delay
  paths: LearningPath[] = this.learningPathService.getStoredPaths();
  courses: Course[] = [];
  learners: DirectoryUser[] = [];
  pathCourses: Record<string, LearningPathCourse[]> = {};
  pathAssignments: Record<string, LearningPathAssignment[]> = {};

  isLoading = false;
  isSaving = false;
  processingPathId: string | null = null;
  errorMessage = '';

  isFormOpen = false;
  editingPathId: string | null = null;
  form: PathForm = { ...EMPTY_FORM };

  // Expandable operations panel per path card (defaults collapsed for clean compact view)
  expandedOperations: Record<string, boolean> = {};

  selectedLearnerByPath: Record<string, string> = {};
  selectedCourseByPath: Record<string, string> = {};
  courseOrderByPath: Record<string, number> = {};

  ngOnInit(): void {
    // Pre-populate assignments for initial stored paths
    this.paths.forEach(path => {
      this.learningPathService.getAssignmentsByPath(path.pathId).subscribe({
        next: (asgs) => {
          this.pathAssignments[path.pathId] = asgs;
          this.changeDetector.detectChanges();
        }
      });
    });
    this.loadData();
  }

  toggleOperations(pathId: string): void {
    this.expandedOperations[pathId] = !this.expandedOperations[pathId];
    this.changeDetector.detectChanges();
  }

  loadData(silent = false): void {
    if (!silent && this.paths.length === 0) {
      this.isLoading = true;
    }
    this.errorMessage = '';

    forkJoin({
      paths: this.learningPathService.getAllLearningPaths(),
      courses: this.courseService.getAllCourses(),
      learners: this.userDirectoryService.getLearners()
    }).subscribe({
      next: ({ paths, courses, learners }) => {
        if (paths && paths.length > 0) {
          this.paths = paths;
        } else if (this.paths.length === 0) {
          this.paths = this.learningPathService.getStoredPaths();
        }
        this.courses = courses.filter((course) => course.status === 'PUBLISHED');
        this.learners = learners.filter((learner) => learner.active);
        this.paths.forEach(path => {
          this.learningPathService.getAssignmentsByPath(path.pathId).subscribe({
            next: (asgs) => {
              this.pathAssignments[path.pathId] = asgs;
              this.changeDetector.detectChanges();
            }
          });
        });
        this.isLoading = false;
        this.changeDetector.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        if (this.paths.length === 0) {
          this.paths = this.learningPathService.getStoredPaths();
        }
        this.isLoading = false;
        this.errorMessage = this.extractError(
          error,
          'Learning path management data could not be loaded.'
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  getLearnerName(learnerId: string): string {
    const found = this.learners.find(l => l.userId === learnerId);
    return found ? found.fullName : learnerId;
  }

  openCreateForm(): void {
    this.editingPathId = null;
    this.form = { ...EMPTY_FORM };
    this.isFormOpen = true;
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  openEditForm(path: LearningPath): void {
    this.editingPathId = path.pathId;
    this.form = {
      pathCode: path.pathCode,
      title: path.title,
      description: path.description ?? '',
      category: path.category,
      targetRole: path.targetRole ?? '',
      level: path.level,
      estimatedDurationHours: path.estimatedDurationHours ?? 1
    };
    this.isFormOpen = true;
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 50);
  }

  closeForm(): void {
    this.isFormOpen = false;
    this.editingPathId = null;
  }

  savePath(): void {
    if (!this.form.title.trim() || !this.form.category.trim()) {
      this.toastService.showError('Title and category are required.');
      return;
    }
    if (!this.editingPathId && !this.form.pathCode.trim()) {
      this.toastService.showError('Path code is required.');
      return;
    }

    this.isSaving = true;

    const baseRequest: LearningPathUpdateRequest = {
      title: this.form.title.trim(),
      description: this.form.description.trim() || null,
      category: this.form.category.trim(),
      targetRole: this.form.targetRole.trim() || null,
      level: this.form.level,
      estimatedDurationHours: Math.max(0, this.form.estimatedDurationHours || 0)
    };

    const request$ = this.editingPathId
      ? this.learningPathService.updateLearningPath(
          this.editingPathId,
          baseRequest
        )
      : this.learningPathService.createLearningPath({
          ...baseRequest,
          pathCode: this.form.pathCode.trim(),
          createdByUserId: this.authService.getCurrentUserId()
        } as LearningPathCreateRequest);

    const wasEditing = this.editingPathId !== null;

    request$.subscribe({
      next: () => {
        this.isSaving = false;
        this.closeForm();
        this.toastService.showSuccess(
          wasEditing ? 'Learning path updated.' : 'Learning path created.'
        );
        this.paths = this.learningPathService.getStoredPaths();
        this.changeDetector.detectChanges();
        this.loadData(true);
      },
      error: (error: HttpErrorResponse) => {
        this.isSaving = false;
        this.toastService.showError(
          this.extractError(error, 'Learning path could not be saved.')
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  publish(path: LearningPath): void {
    this.processPath(
      path,
      this.learningPathService.publishLearningPath(path.pathId),
      'Learning path published.'
    );
  }

  archive(path: LearningPath): void {
    this.processPath(
      path,
      this.learningPathService.archiveLearningPath(path.pathId),
      'Learning path archived.'
    );
  }

  async deletePath(path: LearningPath): Promise<void> {
    const confirmed = await this.confirmService.ask(
      'Delete Learning Path',
      `Delete "${path.title}"? This action cannot be undone.`
    );
    if (!confirmed) {
      return;
    }

    this.processingPathId = path.pathId;
    this.learningPathService.deleteLearningPath(path.pathId).subscribe({
      next: () => {
        this.processingPathId = null;
        this.toastService.showSuccess('Learning path deleted.');
        this.paths = this.learningPathService.getStoredPaths();
        this.changeDetector.detectChanges();
        this.loadData(true);
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Learning path could not be deleted.')
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  assign(path: LearningPath): void {
    const learnerId = this.selectedLearnerByPath[path.pathId];
    if (!learnerId) {
      this.toastService.showError('Select a learner before assigning the path.');
      return;
    }

    const currentUser = this.authService.currentUser();
    this.processingPathId = path.pathId;

    const doAssign = () => {
      this.learningPathService.assignLearningPath(path.pathId, {
        learnerId,
        assignmentSource: currentUser?.role === 'ADMIN'
          ? 'ADMIN_ASSIGNED'
          : 'MANAGER_ASSIGNED',
        assignedByUserId: currentUser?.userId ?? null
      }).subscribe({
        next: () => {
          this.processingPathId = null;
          const targetLearner = this.learners.find(l => l.userId === learnerId);
          const learnerName = targetLearner?.fullName;
          this.notifService.notifyLearningPathAssigned(
            learnerId,
            path.title,
            currentUser?.name || 'HR Manager',
            learnerName
          );
          this.selectedLearnerByPath[path.pathId] = '';
          this.toastService.showSuccess('Learning path assigned to the learner.');
          this.loadData(true);
        },
        error: (error: HttpErrorResponse) => {
          this.processingPathId = null;
          this.toastService.showError(
            this.extractError(error, 'Learning path could not be assigned.')
          );
          this.changeDetector.detectChanges();
        }
      });
    };

    if (path.status !== 'PUBLISHED') {
      this.learningPathService.publishLearningPath(path.pathId).subscribe({
        next: () => doAssign(),
        error: () => doAssign()
      });
    } else {
      doAssign();
    }
  }

  loadPathCourses(path: LearningPath): void {
    this.processingPathId = path.pathId;
    this.learningPathService.getLearningPathCourses(path.pathId).subscribe({
      next: (courses) => {
        this.pathCourses[path.pathId] = courses;
        this.courseOrderByPath[path.pathId] = courses.length + 1;
        this.processingPathId = null;
        this.changeDetector.detectChanges();
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Path courses could not be loaded.')
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  addCourse(path: LearningPath): void {
    const courseId = this.selectedCourseByPath[path.pathId];
    if (!courseId) {
      this.toastService.showError('Select a course to add.');
      return;
    }

    this.processingPathId = path.pathId;
    this.learningPathService.addCourseToLearningPath(path.pathId, {
      courseId,
      courseOrder: Math.max(1, this.courseOrderByPath[path.pathId] || 1),
      requiredForCompletion: true,
      unlockAfterPrevious: true
    }).subscribe({
      next: () => {
        this.processingPathId = null;
        this.selectedCourseByPath[path.pathId] = '';
        this.toastService.showSuccess('Course added to the learning path.');
        this.loadPathCourses(path);
        this.loadData(true);
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Course could not be added to the path.')
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  async removeCourse(path: LearningPath, pathCourse: LearningPathCourse): Promise<void> {
    const confirmed = await this.confirmService.ask(
      'Remove Course',
      `Remove "${pathCourse.courseTitle}" from this learning path?`
    );
    if (!confirmed) {
      return;
    }

    this.processingPathId = path.pathId;
    this.learningPathService
      .removeCourseFromLearningPath(path.pathId, pathCourse.pathCourseId)
      .subscribe({
        next: () => {
          this.processingPathId = null;
          this.toastService.showSuccess('Course removed from learning path.');
          this.loadPathCourses(path);
          this.loadData(true);
        },
        error: (error: HttpErrorResponse) => {
          this.processingPathId = null;
          this.toastService.showError(
            this.extractError(error, 'Course could not be removed from the path.')
          );
          this.changeDetector.detectChanges();
        }
      });
  }

  trackPath(index: number, path: LearningPath): string {
    return path.pathId;
  }

  trackPathCourse(index: number, pathCourse: LearningPathCourse): string {
    return pathCourse.pathCourseId;
  }

  private processPath(
    path: LearningPath,
    request$: ReturnType<LearningPathService['publishLearningPath']>,
    successMessage: string
  ): void {
    this.processingPathId = path.pathId;
    request$.subscribe({
      next: () => {
        this.processingPathId = null;
        this.toastService.showSuccess(successMessage);
        this.paths = this.learningPathService.getStoredPaths();
        this.changeDetector.detectChanges();
        this.loadData(true);
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Learning path status could not be changed.')
        );
        this.changeDetector.detectChanges();
      }
    });
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message || error.error?.details || fallback;
  }
}
