import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { Course } from '../../../learning/models/course.model';
import {
  CourseSaveRequest,
  CourseService
} from '../../../learning/services/course.service';
import { ToastService } from '../../../shared/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';

const COURSE_TYPES = ['ONLINE', 'WORKSHOP', 'WEBINAR', 'BOOTCAMP'];
const COURSE_LEVELS = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

const EMPTY_FORM: CourseSaveRequest = {
  courseCode: '',
  title: '',
  description: '',
  category: '',
  courseType: 'ONLINE',
  courseLevel: 'BEGINNER',
  pricingType: 'FREE',
  price: 0,
  currencyCode: 'INR',
  instructorName: '',
  durationHours: 1,
  maxCapacity: 50,
  passingScore: 60,
  certificateEnabled: true,
  startDate: null,
  endDate: null
};

@Component({
  selector: 'app-course-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './course-management.html',
  styleUrls: ['./course-management.css']
})
export class CourseManagement implements OnInit {

  private readonly courseService = inject(CourseService);
  private readonly toastService = inject(ToastService);
  private readonly confirmService = inject(ConfirmService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly courseTypes = COURSE_TYPES;
  readonly courseLevels = COURSE_LEVELS;

  courses: Course[] = [];
  isLoading = true;
  errorMessage = '';

  isFormOpen = false;
  isEditing = false;
  editingCourseId: string | null = null;
  form: CourseSaveRequest = { ...EMPTY_FORM };
  formErrors: string[] = [];
  isSaving = false;

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.courseService.getAllCourses().subscribe({
      next: (courses) => {
        this.courses = courses || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.courses = [];
        this.isLoading = false;
        this.errorMessage =
          'Courses could not be loaded. Please check that the backend is running.';
        this.cdr.detectChanges();
      }
    });
  }

  openCreateForm(): void {
    this.isEditing = false;
    this.editingCourseId = null;
    this.form = { ...EMPTY_FORM };
    this.formErrors = [];
    this.isFormOpen = true;
  }

  openEditForm(course: Course): void {
    this.isEditing = true;
    this.editingCourseId = course.courseId;
    this.form = {
      courseCode: course.courseCode,
      title: course.title,
      description: course.description ?? '',
      category: course.category ?? '',
      courseType: course.courseType ?? 'ONLINE',
      courseLevel: course.courseLevel ?? 'BEGINNER',
      pricingType: course.pricingType ?? 'FREE',
      price: course.price ?? 0,
      currencyCode: course.currencyCode ?? 'INR',
      instructorName: course.instructorName ?? '',
      durationHours: course.durationHours ?? 1,
      maxCapacity: course.maxCapacity ?? 50,
      passingScore: course.passingScore ?? 60,
      certificateEnabled: course.certificateEnabled ?? true,
      startDate: course.startDate ?? null,
      endDate: course.endDate ?? null
    };
    this.formErrors = [];
    this.isFormOpen = true;
  }

  closeForm(): void {
    this.isFormOpen = false;
  }

  private validate(): boolean {
    const errors: string[] = [];

    if (!this.isEditing && !this.form.courseCode?.trim()) {
      errors.push('Course code is required.');
    }
    if (!this.form.title?.trim()) {
      errors.push('Course title is required.');
    }
    if (!this.form.category?.trim()) {
      errors.push('Category is required.');
    }
    if (!this.form.instructorName?.trim()) {
      errors.push('Instructor name is required.');
    }
    if (!this.form.currencyCode || this.form.currencyCode.trim().length !== 3) {
      errors.push('Currency code must be exactly 3 letters.');
    }
    if (this.form.pricingType === 'PAID' && (!this.form.price || this.form.price <= 0)) {
      errors.push('Paid courses must have a price greater than 0.');
    }
    if (this.form.pricingType === 'FREE') {
      this.form.price = 0;
    }
    if (!this.form.durationHours || this.form.durationHours < 1) {
      errors.push('Duration must be at least 1 hour.');
    }
    if (!this.form.maxCapacity || this.form.maxCapacity < 1) {
      errors.push('Maximum capacity must be at least 1.');
    }
    if (this.form.passingScore === null || this.form.passingScore === undefined ||
        this.form.passingScore < 0 || this.form.passingScore > 100) {
      errors.push('Passing score must be between 0 and 100.');
    }

    this.formErrors = errors;
    return errors.length === 0;
  }

  saveCourse(): void {
    if (!this.validate()) {
      return;
    }

    this.isSaving = true;

    const request$ = this.isEditing && this.editingCourseId
      ? this.courseService.updateCourse(this.editingCourseId, this.form)
      : this.courseService.createCourse(this.form);

    request$.subscribe({
      next: () => {
        this.isSaving = false;
        this.isFormOpen = false;
        this.toastService.showSuccess(
          this.isEditing ? 'Course updated successfully.' : 'Course created successfully.'
        );
        this.loadCourses();
      },
      error: (error: unknown) => {
        this.isSaving = false;
        this.toastService.showError(
          this.extractErrorMessage(error) ??
            'Could not save the course. Please check the details and try again.'
        );
      }
    });
  }

  async deleteCourse(course: Course): Promise<void> {
    const confirmed = await this.confirmService.ask(
      'Delete Course',
      `Delete "${course.title}"? This cannot be undone.`
    );

    if (!confirmed) {
      return;
    }

    this.courseService.deleteCourse(course.courseId).subscribe({
      next: () => {
        this.toastService.showSuccess('Course deleted.');
        this.loadCourses();
      },
      error: (error: unknown) => {
        this.toastService.showError(
          this.extractErrorMessage(error) ?? 'Could not delete this course.'
        );
      }
    });
  }

  publishCourse(course: Course): void {
    this.courseService.publishCourse(course.courseId).subscribe({
      next: () => {
        this.toastService.showSuccess('Course published.');
        this.loadCourses();
      },
      error: (error: unknown) => {
        this.toastService.showError(
          this.extractErrorMessage(error) ?? 'Could not publish this course.'
        );
      }
    });
  }

  archiveCourse(course: Course): void {
    this.courseService.archiveCourse(course.courseId).subscribe({
      next: () => {
        this.toastService.showSuccess('Course archived.');
        this.loadCourses();
      },
      error: (error: unknown) => {
        this.toastService.showError(
          this.extractErrorMessage(error) ?? 'Could not archive this course.'
        );
      }
    });
  }

  trackCourseById(index: number, course: Course): string {
    return course.courseId;
  }

  private extractErrorMessage(error: unknown): string | null {
    const err = error as { error?: { message?: string } };
    return err?.error?.message ?? null;
  }
}
