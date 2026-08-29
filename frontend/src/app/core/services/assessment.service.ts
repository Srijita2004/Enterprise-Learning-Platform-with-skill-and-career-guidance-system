import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Assessment } from '../../learning/models/assessment';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssessmentService {
  private readonly baseUrl = `${environment.apiUrl}/assessments`;

  private mockAssessments: Assessment[] = [
    { assessmentId: 301, employeeId: 101, skillId: 1, score: 85, verified: true },
    { assessmentId: 302, employeeId: 101, skillId: 2, score: 72, verified: false },
    { assessmentId: 303, employeeId: 102, skillId: 1, score: 95, verified: true },
    { assessmentId: 304, employeeId: 102, skillId: 2, score: 98, verified: true },
    { assessmentId: 305, employeeId: 103, skillId: 5, score: 88, verified: true }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Assessment[]> {
    if (environment.useMock) {
      return of([...this.mockAssessments]);
    }
    return this.http.get<Assessment[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  getById(id: number): Observable<Assessment> {
    if (environment.useMock) {
      const assessment = this.mockAssessments.find(a => a.assessmentId === id);
      return assessment ? of({ ...assessment }) : throwError(() => new Error('Assessment not found'));
    }
    return this.http.get<Assessment>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  create(assessment: Assessment): Observable<Assessment> {
    if (environment.useMock) {
      const newId = assessment.assessmentId || Math.max(...this.mockAssessments.map(a => a.assessmentId), 0) + 1;
      const newAssessment = { ...assessment, assessmentId: newId, verified: !!assessment.verified };
      this.mockAssessments.push(newAssessment);
      return of(newAssessment);
    }
    return this.http.post<Assessment>(this.baseUrl, assessment).pipe(
      catchError(this.handleError)
    );
  }

  update(id: number, assessment: Assessment): Observable<Assessment> {
    if (environment.useMock) {
      const index = this.mockAssessments.findIndex(a => a.assessmentId === id);
      if (index === -1) {
        return throwError(() => new Error('Assessment not found'));
      }
      this.mockAssessments[index] = { ...assessment, assessmentId: id };
      return of(this.mockAssessments[index]);
    }
    return this.http.put<Assessment>(`${this.baseUrl}/${id}`, assessment).pipe(
      catchError(this.handleError)
    );
  }

  delete(id: number): Observable<string> {
    if (environment.useMock) {
      const index = this.mockAssessments.findIndex(a => a.assessmentId === id);
      if (index === -1) {
        return throwError(() => new Error('Assessment not found'));
      }
      this.mockAssessments.splice(index, 1);
      return of('Assessment deleted successfully');
    }
    return this.http.delete(`${this.baseUrl}/${id}`, { responseType: 'text' }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: any) {
    console.error('API Error:', error);
    return throwError(() => new Error(error.message || 'Server error occurred'));
  }
}
