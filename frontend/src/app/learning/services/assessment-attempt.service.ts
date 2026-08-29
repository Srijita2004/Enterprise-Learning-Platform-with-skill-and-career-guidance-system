import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  AssessmentAttempt,
  AssessmentGradeRequest
} from '../models/assessment-model';

@Injectable({
  providedIn: 'root'
})
export class AssessmentAttemptService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    environment.learningApiUrl;

  startAttempt(
    enrollmentId: string,
    contentId: string
  ): Observable<AssessmentAttempt> {
    return this.http.post<AssessmentAttempt>(
      `${this.apiUrl}/enrollments/${enrollmentId}/assessments/${contentId}/attempts`,
      {}
    );
  }

  submitAttempt(
    attemptId: string
  ): Observable<AssessmentAttempt> {
    return this.http.patch<AssessmentAttempt>(
      `${this.apiUrl}/assessment-attempts/${attemptId}/submit`,
      {}
    );
  }

  gradeAttempt(
    attemptId: string,
    request: AssessmentGradeRequest
  ): Observable<AssessmentAttempt> {
    return this.http.patch<AssessmentAttempt>(
      `${this.apiUrl}/assessment-attempts/${attemptId}/grade`,
      request
    );
  }

  getAttemptById(
    attemptId: string
  ): Observable<AssessmentAttempt> {
    return this.http.get<AssessmentAttempt>(
      `${this.apiUrl}/assessment-attempts/${attemptId}`
    );
  }

  getAttemptsByEnrollmentAndContent(
    enrollmentId: string,
    contentId: string
  ): Observable<AssessmentAttempt[]> {
    return this.http.get<AssessmentAttempt[]>(
      `${this.apiUrl}/enrollments/${enrollmentId}/assessments/${contentId}/attempts`
    );
  }

  getLatestAttempt(
    enrollmentId: string,
    contentId: string
  ): Observable<AssessmentAttempt> {
    return this.http.get<AssessmentAttempt>(
      `${this.apiUrl}/enrollments/${enrollmentId}/assessments/${contentId}/attempts/latest`
    );
  }

  getBestPassedAttempt(
    enrollmentId: string,
    contentId: string
  ): Observable<AssessmentAttempt> {
    return this.http.get<AssessmentAttempt>(
      `${this.apiUrl}/enrollments/${enrollmentId}/assessments/${contentId}/attempts/best-passed`
    );
  }
}