import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Enrollment } from '../../../learning/models/enrollment.model';
import { EnrollmentService } from '../../../learning/services/enrollment.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'app-payment-verification',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payment-verification.html',
  styleUrls: ['./payment-verification.css']
})
export class PaymentVerification implements OnInit {
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly toastService = inject(ToastService);

  enrollments: Enrollment[] = [];
  isLoading = true;
  errorMessage = '';
  processingEnrollmentId: string | null = null;
  statusFilter = 'ALL';
  searchText = '';
  rejectionReasons: Record<string, string> = {};

  ngOnInit(): void {
    this.loadEnrollments();
  }

  loadEnrollments(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.enrollmentService.getAllEnrollments().subscribe({
      next: (enrollments) => {
        this.enrollments = enrollments;
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = this.extractError(
          error,
          'Enrollment and payment data could not be loaded.'
        );
      }
    });
  }

  get filteredEnrollments(): Enrollment[] {
    const search = this.searchText.trim().toLowerCase();

    return this.enrollments.filter((enrollment) => {
      const statusMatches =
        this.statusFilter === 'ALL' ||
        enrollment.paymentStatus === this.statusFilter;

      const searchMatches =
        !search ||
        (enrollment.learnerName ?? '').toLowerCase().includes(search) ||
        enrollment.courseTitle.toLowerCase().includes(search) ||
        enrollment.courseCode.toLowerCase().includes(search) ||
        (enrollment.paymentReference ?? '').toLowerCase().includes(search);

      return statusMatches && searchMatches;
    });
  }

  get pendingCount(): number {
    return this.enrollments.filter(
      (item) => item.paymentStatus === 'PENDING'
    ).length;
  }

  get paidCount(): number {
    return this.enrollments.filter(
      (item) => item.paymentStatus === 'PAID'
    ).length;
  }

  get rejectedCount(): number {
    return this.enrollments.filter(
      (item) => item.paymentStatus === 'REJECTED'
    ).length;
  }

  approve(enrollment: Enrollment): void {
    if (this.processingEnrollmentId) {
      return;
    }

    this.processingEnrollmentId = enrollment.enrollmentId;

    this.enrollmentService.approvePayment(enrollment.enrollmentId).subscribe({
      next: (updated) => {
        this.replaceEnrollment(updated);
        this.processingEnrollmentId = null;
        this.toastService.showSuccess(
          `Payment approved for ${updated.learnerName ?? 'learner'}.`
        );
      },
      error: (error: HttpErrorResponse) => {
        this.processingEnrollmentId = null;
        this.toastService.showError(
          this.extractError(error, 'Payment could not be approved.')
        );
      }
    });
  }

  reject(enrollment: Enrollment): void {
    if (this.processingEnrollmentId) {
      return;
    }

    const reason = (this.rejectionReasons[enrollment.enrollmentId] ?? '').trim();
    if (!reason) {
      this.toastService.showError('Enter a rejection reason before rejecting payment.');
      return;
    }

    this.processingEnrollmentId = enrollment.enrollmentId;

    this.enrollmentService
      .rejectPayment(enrollment.enrollmentId, reason)
      .subscribe({
        next: (updated) => {
          this.replaceEnrollment(updated);
          this.processingEnrollmentId = null;
          this.rejectionReasons[enrollment.enrollmentId] = '';
          this.toastService.showSuccess(
            `Payment rejected for ${updated.learnerName ?? 'learner'}.`
          );
        },
        error: (error: HttpErrorResponse) => {
          this.processingEnrollmentId = null;
          this.toastService.showError(
            this.extractError(error, 'Payment could not be rejected.')
          );
        }
      });
  }

  formatAmount(enrollment: Enrollment): string {
    if (
      enrollment.coursePricingType === 'FREE' ||
      !enrollment.priceAtEnrollment
    ) {
      return 'Free';
    }

    return `${enrollment.currencyCode ?? 'INR'} ${enrollment.priceAtEnrollment}`;
  }

  trackByEnrollmentId(index: number, enrollment: Enrollment): string {
    return enrollment.enrollmentId;
  }

  private replaceEnrollment(updated: Enrollment): void {
    this.enrollments = this.enrollments.map((item) =>
      item.enrollmentId === updated.enrollmentId ? updated : item
    );
  }

  private extractError(error: HttpErrorResponse, fallback: string): string {
    return error.error?.message || error.error?.details || fallback;
  }
}
