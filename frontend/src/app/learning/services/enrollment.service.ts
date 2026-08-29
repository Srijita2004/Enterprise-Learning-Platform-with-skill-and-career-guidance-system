import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  Enrollment,
  EnrollmentCreateRequest,
  EnrollmentStatus,
  PaymentStatus
} from '../models/enrollment.model';

export interface CourseCompletion {
  completionId: string;
  enrollmentId: string;
  learnerId: string;
  courseId: string;
  courseCode: string;
  courseTitle: string;
  enrollmentStatus: 'COMPLETED';
  certificateEligible: boolean;
  completedAt?: string | null;
}

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.learningApiUrl;

  createEnrollment(
    courseId: string,
    request: EnrollmentCreateRequest
  ): Observable<Enrollment> {
    return this.http.post<Enrollment>(
      `${this.apiUrl}/courses/${courseId}/enrollments`,
      request
    );
  }

  getEnrollmentByLearnerAndCourse(
    learnerId: string,
    courseId: string
  ): Observable<Enrollment> {
    return this.http.get<Enrollment>(
      `${this.apiUrl}/learners/${learnerId}/courses/${courseId}/enrollment`
    );
  }

  getLearnerEnrollments(learnerId: string): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(
      `${this.apiUrl}/learners/${learnerId}/enrollments`
    );
  }

  getAllEnrollments(
    status?: EnrollmentStatus,
    paymentStatus?: PaymentStatus
  ): Observable<Enrollment[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    if (paymentStatus) {
      params = params.set('paymentStatus', paymentStatus);
    }
    return this.http.get<Enrollment[]>(`${this.apiUrl}/enrollments`, { params });
  }

  getPendingPayments(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/payments/pending`);
  }

  submitPayment(
    enrollmentId: string,
    paymentReference: string
  ): Observable<Enrollment> {
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/submit`,
      { paymentReference }
    );
  }

  approvePayment(enrollmentId: string): Observable<Enrollment> {
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/approve`,
      {}
    );
  }

  rejectPayment(
    enrollmentId: string,
    reason: string
  ): Observable<Enrollment> {
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/reject`,
      { reason }
    );
  }

  /** Existing compatibility method. */
  confirmPayment(enrollmentId: string): Observable<Enrollment> {
    return this.approvePayment(enrollmentId);
  }

  completeCourse(enrollmentId: string): Observable<CourseCompletion> {
    return this.http.post<CourseCompletion>(
      `${this.apiUrl}/enrollments/${enrollmentId}/completion`,
      {}
    );
  }
}
