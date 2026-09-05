import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

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

  private mockEnrollments: Enrollment[] = [
    {
      enrollmentId: 'enr-101',
      learnerId: '101',
      learnerName: 'Alex Vance',
      courseId: 'crs-1',
      courseCode: 'CRS-SPRING-01',
      courseTitle: 'Spring Boot 4 & Cloud Microservices Architecture',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'PENDING',
      paymentStatus: 'PENDING',
      paymentReference: '#TXN-98421-WIRE',
      enrollmentSource: 'SELF_ENROLLED',
      priceAtEnrollment: 450,
      currencyCode: 'USD',
      accessAllowed: false,
      enrolledAt: new Date(Date.now() - 2 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 2 * 86400000).toISOString()
    },
    {
      enrollmentId: 'enr-102',
      learnerId: '103',
      learnerName: 'Sarah Jenkins',
      courseId: 'crs-2',
      courseCode: 'CRS-ANGULAR-01',
      courseTitle: 'Modern Angular 21 Enterprise Frontend Architecture',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'ACTIVE',
      paymentStatus: 'PAID',
      paymentReference: '#CARD-5412-STRIPE',
      enrollmentSource: 'SELF_ENROLLED',
      priceAtEnrollment: 380,
      currencyCode: 'USD',
      accessAllowed: true,
      enrolledAt: new Date(Date.now() - 5 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 4 * 86400000).toISOString()
    },
    {
      enrollmentId: 'enr-103',
      learnerId: '106',
      learnerName: 'John Smith',
      courseId: 'crs-3',
      courseCode: 'CRS-DEVOPS-01',
      courseTitle: 'Enterprise CI/CD, Docker & Container Orchestration',
      courseType: 'WORKSHOP',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'PENDING',
      paymentStatus: 'PENDING',
      paymentReference: '#WIRE-7719-ACH',
      enrollmentSource: 'SELF_ENROLLED',
      priceAtEnrollment: 520,
      currencyCode: 'USD',
      accessAllowed: false,
      enrolledAt: new Date(Date.now() - 1 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 1 * 86400000).toISOString()
    },
    {
      enrollmentId: 'enr-104',
      learnerId: '107',
      learnerName: 'Jane Doe',
      courseId: 'crs-4',
      courseCode: 'CRS-AI-01',
      courseTitle: 'Staff AI & Vector Space Machine Learning Pathway',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'ACTIVE',
      paymentStatus: 'PAID',
      paymentReference: '#CORP-PO-3301-INV',
      enrollmentSource: 'ADMIN_ASSIGNED',
      priceAtEnrollment: 600,
      currencyCode: 'USD',
      accessAllowed: true,
      enrolledAt: new Date(Date.now() - 8 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 7 * 86400000).toISOString()
    },
    {
      enrollmentId: 'enr-105',
      learnerId: '110',
      learnerName: 'Michael Chang',
      courseId: 'crs-5',
      courseCode: 'CRS-DB-01',
      courseTitle: 'High-Throughput Distributed PostgreSQL Masterclass',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'CANCELLED',
      paymentStatus: 'REJECTED',
      paymentReference: '#ERR-INVALID-09',
      enrollmentSource: 'SELF_ENROLLED',
      priceAtEnrollment: 490,
      currencyCode: 'USD',
      accessAllowed: false,
      enrolledAt: new Date(Date.now() - 6 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 5 * 86400000).toISOString()
    },
    {
      enrollmentId: 'enr-106',
      learnerId: '109',
      learnerName: 'Elena Rostova',
      courseId: 'crs-6',
      courseCode: 'CRS-SEC-01',
      courseTitle: 'Zero-Trust Cybersecurity & Threat Vulnerability Modeling',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: 'PAID',
      status: 'PENDING',
      paymentStatus: 'PENDING',
      paymentReference: '#AUTH-99214-BANK',
      enrollmentSource: 'SELF_ENROLLED',
      priceAtEnrollment: 550,
      currencyCode: 'USD',
      accessAllowed: false,
      enrolledAt: new Date(Date.now() - 12 * 3600000).toISOString(),
      updatedAt: new Date(Date.now() - 12 * 3600000).toISOString()
    }
  ];

  createEnrollment(
    courseId: string,
    request: EnrollmentCreateRequest
  ): Observable<Enrollment> {
    let activeName = 'Learner';
    try {
      const raw = sessionStorage.getItem('ssn_auth_user');
      if (raw) {
        const u = JSON.parse(raw);
        if (u && u.name) activeName = u.name;
      }
    } catch {}

    const isPaid = Boolean(request.paymentReference && !request.paymentReference.includes('FREE'));
    const created: Enrollment = {
      enrollmentId: `enr-${Date.now()}`,
      learnerId: request.learnerId || '1',
      learnerName: activeName,
      courseId: courseId,
      courseCode: 'CRS-GEN',
      courseTitle: 'Enterprise Training Course',
      courseType: 'ONLINE',
      courseStatus: 'PUBLISHED',
      coursePricingType: isPaid ? 'PAID' : 'FREE',
      status: isPaid ? 'PENDING' : 'ACTIVE',
      paymentStatus: isPaid ? 'PENDING' : 'NOT_REQUIRED',
      paymentReference: request.paymentReference || (isPaid ? `#TXN-${Math.floor(100000 + Math.random() * 900000)}` : '#FREE-GRANT'),
      enrollmentSource: request.enrollmentSource || 'SELF_ENROLLED',
      priceAtEnrollment: isPaid ? 450 : 0,
      currencyCode: 'USD',
      accessAllowed: !isPaid,
      enrolledAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    this.mockEnrollments.unshift(created);

    // If paid, notify HR & Admin for payment verification
    if (isPaid) {
      try {
        const notifList = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
        notifList.unshift(
          {
            id: 'notif-pay-hr-' + Date.now(),
            title: `Payment Verification Pending: ${created.courseTitle}`,
            message: `${activeName} submitted payment reference ${created.paymentReference} for "${created.courseTitle}". Please review and authorize access.`,
            type: 'WARNING',
            targetRole: 'HR',
            time: 'Just now',
            read: false
          },
          {
            id: 'notif-pay-adm-' + (Date.now() + 1),
            title: `Payment Verification Request: ${created.courseTitle}`,
            message: `${activeName} submitted payment ref ${created.paymentReference}. Authorization pending.`,
            type: 'INFO',
            targetRole: 'ADMIN',
            time: 'Just now',
            read: false
          }
        );
        localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(notifList.slice(0, 30)));
      } catch {}
    }

    return this.http.post<Enrollment>(
      `${this.apiUrl}/courses/${courseId}/enrollments`,
      request
    ).pipe(
      catchError(() => of(created))
    );
  }

  getEnrollmentByLearnerAndCourse(
    learnerId: string,
    courseId: string
  ): Observable<Enrollment> {
    const match = this.mockEnrollments.find(e => e.learnerId === learnerId && e.courseId === courseId);
    if (match) return of({ ...match });

    return this.http.get<Enrollment>(
      `${this.apiUrl}/learners/${learnerId}/courses/${courseId}/enrollment`
    ).pipe(
      catchError(() => of(null as any))
    );
  }

  getLearnerEnrollments(learnerId: string): Observable<Enrollment[]> {
    const matches = this.mockEnrollments.filter(e => e.learnerId === learnerId);
    return this.http.get<Enrollment[]>(
      `${this.apiUrl}/learners/${learnerId}/enrollments`
    ).pipe(
      map(res => (!res || !Array.isArray(res)) ? matches : res),
      catchError(() => of(matches))
    );
  }

  getAllEnrollments(
    status?: EnrollmentStatus,
    paymentStatus?: PaymentStatus
  ): Observable<Enrollment[]> {
    let list = [...this.mockEnrollments];
    if (status) {
      list = list.filter(e => e.status === status);
    }
    if (paymentStatus) {
      list = list.filter(e => e.paymentStatus === paymentStatus);
    }

    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    if (paymentStatus) {
      params = params.set('paymentStatus', paymentStatus);
    }

    return this.http.get<Enrollment[]>(`${this.apiUrl}/enrollments`, { params }).pipe(
      map(res => (!res || res.length === 0) ? list : res),
      catchError(() => of(list))
    );
  }

  getPendingPayments(): Observable<Enrollment[]> {
    const pendings = this.mockEnrollments.filter(e => e.paymentStatus === 'PENDING');
    return this.http.get<Enrollment[]>(`${this.apiUrl}/payments/pending`).pipe(
      map(res => (!res || res.length === 0) ? pendings : res),
      catchError(() => of(pendings))
    );
  }

  submitPayment(
    enrollmentId: string,
    paymentReference: string
  ): Observable<Enrollment> {
    const found = this.mockEnrollments.find(e => e.enrollmentId === enrollmentId);
    if (found) {
      found.paymentReference = paymentReference;
      found.paymentStatus = 'PENDING';
    }
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/submit`,
      { paymentReference }
    ).pipe(
      catchError(() => of(found || this.mockEnrollments[0]))
    );
  }

  approvePayment(enrollmentId: string): Observable<Enrollment> {
    const found = this.mockEnrollments.find(e => e.enrollmentId === enrollmentId);
    if (found) {
      found.paymentStatus = 'PAID';
      found.status = 'ACTIVE';
      found.accessAllowed = true;
      found.updatedAt = new Date().toISOString();
    }
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/approve`,
      {}
    ).pipe(
      catchError(() => of(found || this.mockEnrollments[0]))
    );
  }

  rejectPayment(
    enrollmentId: string,
    reason: string
  ): Observable<Enrollment> {
    const found = this.mockEnrollments.find(e => e.enrollmentId === enrollmentId);
    if (found) {
      found.paymentStatus = 'REJECTED';
      found.status = 'CANCELLED';
      found.accessAllowed = false;
      found.paymentRejectionReason = reason;
      found.updatedAt = new Date().toISOString();
    }
    return this.http.patch<Enrollment>(
      `${this.apiUrl}/enrollments/${enrollmentId}/payment/reject`,
      { reason }
    ).pipe(
      catchError(() => of(found || this.mockEnrollments[0]))
    );
  }

  confirmPayment(enrollmentId: string): Observable<Enrollment> {
    return this.approvePayment(enrollmentId);
  }

  completeCourse(enrollmentId: string): Observable<CourseCompletion> {
    const completion: CourseCompletion = {
      completionId: `comp-${Date.now()}`,
      enrollmentId,
      learnerId: '1',
      courseId: 'crs-1',
      courseCode: 'CRS-SPRING-01',
      courseTitle: 'Spring Boot 4 & Cloud Microservices Architecture',
      enrollmentStatus: 'COMPLETED',
      certificateEligible: true,
      completedAt: new Date().toISOString()
    };
    return this.http.post<CourseCompletion>(
      `${this.apiUrl}/enrollments/${enrollmentId}/completion`,
      {}
    ).pipe(
      catchError(() => of(completion))
    );
  }
}
