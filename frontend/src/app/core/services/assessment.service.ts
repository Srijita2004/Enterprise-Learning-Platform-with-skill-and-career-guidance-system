import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Assessment } from '../../learning/models/assessment';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AssessmentService {
  private readonly baseUrl = `${environment.apiUrl}/assessments`;

  private mockAssessments: Assessment[] = [
    // Srijita (ID: 1)
    { assessmentId: 101, employeeId: 1, skillId: 1, score: 96, verified: true },
    { assessmentId: 102, employeeId: 1, skillId: 2, score: 94, verified: true },
    { assessmentId: 103, employeeId: 1, skillId: 10, score: 90, verified: true },

    // Alex Vance (ID: 101)
    { assessmentId: 201, employeeId: 101, skillId: 5, score: 98, verified: true },
    { assessmentId: 202, employeeId: 101, skillId: 9, score: 92, verified: true },

    // Marcus Brodie (ID: 102)
    { assessmentId: 301, employeeId: 102, skillId: 10, score: 99, verified: true },
    { assessmentId: 302, employeeId: 102, skillId: 5, score: 95, verified: true },

    // Sarah Jenkins (ID: 103)
    { assessmentId: 401, employeeId: 103, skillId: 6, score: 97, verified: true },
    { assessmentId: 402, employeeId: 103, skillId: 1, score: 88, verified: true },

    // John Smith (ID: 106)
    { assessmentId: 501, employeeId: 106, skillId: 3, score: 95, verified: true },

    // Jane Doe (ID: 107)
    { assessmentId: 601, employeeId: 107, skillId: 8, score: 96, verified: true },

    // David Miller (ID: 108)
    { assessmentId: 701, employeeId: 108, skillId: 9, score: 94, verified: true },

    // Elena Rostova (ID: 109)
    { assessmentId: 801, employeeId: 109, skillId: 7, score: 98, verified: true },

    // Michael Chang (ID: 110)
    { assessmentId: 901, employeeId: 110, skillId: 1, score: 89, verified: true },

    // Priya Sharma (ID: 111)
    { assessmentId: 1001, employeeId: 111, skillId: 4, score: 96, verified: true }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Assessment[]> {
    if (environment.useMock) {
      return of([...this.mockAssessments]);
    }
    return this.http.get<Assessment[]>(this.baseUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) return [...this.mockAssessments];
        return res;
      }),
      catchError(() => of([...this.mockAssessments]))
    );
  }

  getById(id: number): Observable<Assessment> {
    if (environment.useMock) {
      const assessment = this.mockAssessments.find(a => a.assessmentId === id) || this.mockAssessments[0];
      return of({ ...assessment });
    }
    return this.http.get<Assessment>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => {
        const assessment = this.mockAssessments.find(a => a.assessmentId === id) || this.mockAssessments[0];
        return of({ ...assessment });
      })
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
