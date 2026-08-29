import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
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

import { Course } from '../../models/course.model';
import { Enrollment } from '../../models/enrollment.model';
import { CourseModule } from '../../models/learning-content.model';
import { CourseService } from '../../services/course.service';
import { EnrollmentService } from '../../services/enrollment.service';
import { LearningContentService } from '../../services/learning-content.service';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-course-details',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './course-details.html',
  styleUrls: ['./course-details.css']
})
export class CourseDetails implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly courseService =
    inject(CourseService);

  private readonly enrollmentService =
    inject(EnrollmentService);

  private readonly learningContentService =
    inject(LearningContentService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  private readonly authService = inject(AuthService);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  course: Course | null = null;
  modules: CourseModule[] = [];

  enrollment: Enrollment | null = null;

  isLoading = true;
  isLoadingModules = false;

  isEnrollmentLoading = false;

  isEnrollmentActionRunning = false;

  errorMessage = '';

  enrollmentMessage = '';

  enrollmentError = '';

  paymentReference = '';

  isPaymentSubmitting = false;

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

    this.loadCourse(courseId);
  }

  loadCourse(courseId: string): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.course = null;

    this.changeDetector.detectChanges();

    this.courseService
      .getCourseById(courseId)
      .subscribe({
        next: (course: Course) => {
          this.course = course;
          this.isLoading = false;
          this.errorMessage = '';

          this.changeDetector.detectChanges();

          this.loadExistingEnrollment(
            course.courseId
          );
          this.loadCourseModules(
            course.courseId
          );
        },

        error: (error: unknown) => {
          console.error(
            'Failed to load course details:',
            error
          );

          this.course = null;
          this.isLoading = false;

          this.errorMessage =
            'Course details could not be loaded. Please check that the learning management service is running.';

          this.changeDetector.detectChanges();
        }
      });
  }

  loadCourseModules(courseId: string): void {
    this.isLoadingModules = true;
    this.learningContentService.getPublishedModules(courseId).subscribe({
      next: (modules) => {
        this.modules = modules || [];
        this.isLoadingModules = false;
        this.changeDetector.detectChanges();
      },
      error: () => {
        this.modules = [];
        this.isLoadingModules = false;
        this.changeDetector.detectChanges();
      }
    });
  }

  loadExistingEnrollment(
    courseId: string
  ): void {
    this.isEnrollmentLoading = true;
    this.enrollmentError = '';

    this.changeDetector.detectChanges();

    this.enrollmentService
      .getEnrollmentByLearnerAndCourse(
        this.learnerId,
        courseId
      )
      .subscribe({
        next: (enrollment: Enrollment) => {
          this.enrollment = enrollment;
          this.isEnrollmentLoading = false;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          this.isEnrollmentLoading = false;

          if (error.status === 404) {
            this.enrollment = null;
            this.enrollmentError = '';
          } else {
            console.error(
              'Failed to check enrollment:',
              error
            );

            this.enrollmentError =
              'Existing enrollment information could not be checked.';
          }

          this.changeDetector.detectChanges();
        }
      });
  }

  enrollNow(): void {
    if (
      !this.course ||
      this.isEnrollmentActionRunning
    ) {
      return;
    }

    if (
      this.course.pricingType === 'PAID' &&
      !this.paymentReference.trim()
    ) {
      this.enrollmentError =
        'Enter the payment transaction reference before submitting the paid enrollment.';
      return;
    }

    this.isEnrollmentActionRunning = true;
    this.enrollmentMessage = '';
    this.enrollmentError = '';

    this.changeDetector.detectChanges();

    this.enrollmentService
      .createEnrollment(
        this.course.courseId,
        {
          learnerId: this.learnerId,
          enrollmentSource: 'SELF_ENROLLED',
          paymentReference:
            this.course.pricingType === 'PAID'
              ? this.paymentReference.trim()
              : null
        }
      )
      .subscribe({
        next: (enrollment: Enrollment) => {
          this.enrollment = enrollment;

          this.isEnrollmentActionRunning = false;

          if (
            enrollment.paymentStatus === 'PENDING'
          ) {
            this.enrollmentMessage =
              'Enrollment created. Payment verification is pending. Admin or HR must verify the payment before course access is activated.';
          } else {
            this.enrollmentMessage =
              'Enrollment completed successfully. Course access is now available.';
          }

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Failed to create enrollment:',
            error
          );

          this.isEnrollmentActionRunning = false;

          this.enrollmentError =
            this.getBackendErrorMessage(
              error,
              'Enrollment could not be completed.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }


  submitPaymentForVerification(): void {
    if (
      !this.enrollment ||
      !this.paymentReference.trim() ||
      this.isPaymentSubmitting
    ) {
      this.enrollmentError =
        'Enter a valid payment transaction reference.';
      return;
    }

    this.isPaymentSubmitting = true;
    this.enrollmentError = '';
    this.enrollmentMessage = '';

    this.enrollmentService
      .submitPayment(
        this.enrollment.enrollmentId,
        this.paymentReference.trim()
      )
      .subscribe({
        next: (enrollment) => {
          this.enrollment = enrollment;
          this.isPaymentSubmitting = false;
          this.enrollmentMessage =
            'Payment reference submitted. Course access will be activated after Admin or HR approval.';
          this.changeDetector.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          this.isPaymentSubmitting = false;
          this.enrollmentError = this.getBackendErrorMessage(
            error,
            'Payment reference could not be submitted.'
          );
          this.changeDetector.detectChanges();
        }
      });
  }

  canEnroll(): boolean {
    return Boolean(
      this.course &&
      this.course.status === 'PUBLISHED' &&
      !this.enrollment &&
      !this.isEnrollmentLoading
    );
  }


  getCoursePrice(
    course: Course
  ): string {
    if (
      course.pricingType === 'FREE' ||
      course.price === null ||
      course.price === undefined ||
      course.price === 0
    ) {
      return 'Free';
    }

    return `${
      course.currencyCode ?? 'INR'
    } ${course.price}`;
  }

  getAvailableSeats(
    course: Course
  ): string {
    if (
      course.availableSeats === null ||
      course.availableSeats === undefined
    ) {
      return 'Not specified';
    }

    return `${course.availableSeats} seats`;
  }

  getEnrollmentPrice(
    enrollment: Enrollment
  ): string {
    if (
      enrollment.coursePricingType === 'FREE' ||
      enrollment.priceAtEnrollment === null ||
      enrollment.priceAtEnrollment === undefined ||
      enrollment.priceAtEnrollment === 0
    ) {
      return 'Free';
    }

    return `${
      enrollment.currencyCode ?? 'INR'
    } ${enrollment.priceAtEnrollment}`;
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