import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
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
  LearningPathCourse,
  LearningPathCreateRequest,
  LearningPathUpdateRequest
} from '../../../learning/models/learning-path.model';
import { CourseService } from '../../../learning/services/course.service';
import { LearningPathService } from '../../../learning/services/learning-path.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { ToastService } from '../../../shared/services/toast.service';

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
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);

  paths: LearningPath[] = [];
  courses: Course[] = [];
  learners: DirectoryUser[] = [];
  pathCourses: Record<string, LearningPathCourse[]> = {};

  isLoading = true;
  isSaving = false;
  processingPathId: string | null = null;
  errorMessage = '';

  isFormOpen = false;
  editingPathId: string | null = null;
  form: PathForm = { ...EMPTY_FORM };

  selectedLearnerByPath: Record<string, string> = {};
  selectedCourseByPath: Record<string, string> = {};
  courseOrderByPath: Record<string, number> = {};

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      paths: this.learningPathService.getAllLearningPaths(),
      courses: this.courseService.getAllCourses(),
      learners: this.userDirectoryService.getLearners()
    }).subscribe({
      next: ({ paths, courses, learners }) => {
        this.paths = paths;
        this.courses = courses.filter((course) => course.status === 'PUBLISHED');
        this.learners = learners.filter((learner) => learner.active);
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = this.extractError(
          error,
          'Learning path management data could not be loaded.'
        );
      }
    });
  }

  openCreateForm(): void {
    this.editingPathId = null;
    this.form = { ...EMPTY_FORM };
    this.isFormOpen = true;
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
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.isSaving = false;
        this.toastService.showError(
          this.extractError(error, 'Learning path could not be saved.')
        );
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
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Learning path could not be deleted.')
        );
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

    this.learningPathService.assignLearningPath(path.pathId, {
      learnerId,
      assignmentSource: currentUser?.role === 'ADMIN'
        ? 'ADMIN_ASSIGNED'
        : 'MANAGER_ASSIGNED',
      assignedByUserId: currentUser?.userId ?? null
    }).subscribe({
      next: () => {
        this.processingPathId = null;
        this.selectedLearnerByPath[path.pathId] = '';
        this.toastService.showSuccess('Learning path assigned to the learner.');
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Learning path could not be assigned.')
        );
      }
    });
  }

  loadPathCourses(path: LearningPath): void {
    this.processingPathId = path.pathId;
    this.learningPathService.getLearningPathCourses(path.pathId).subscribe({
      next: (courses) => {
        this.pathCourses[path.pathId] = courses;
        this.courseOrderByPath[path.pathId] = courses.length + 1;
        this.processingPathId = null;
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Path courses could not be loaded.')
        );
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
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Course could not be added to the path.')
        );
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
          this.loadData();
        },
        error: (error: HttpErrorResponse) => {
          this.processingPathId = null;
          this.toastService.showError(
            this.extractError(error, 'Course could not be removed from the path.')
          );
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
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.processingPathId = null;
        this.toastService.showError(
          this.extractError(error, 'Learning path status could not be changed.')
        );
      }
    });
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message || error.error?.details || fallback;
  }
}
