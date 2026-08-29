import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  LearningPath,
  LearningPathAssignment,
  LearningPathAssignmentRequest,
  LearningPathAssignmentStatus,
  LearningPathCourse,
  LearningPathCourseRequest,
  LearningPathCreateRequest,
  LearningPathStatus,
  LearningPathUpdateRequest
} from '../models/learning-path.model';

@Injectable({
  providedIn: 'root'
})
export class LearningPathService {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    environment.learningApiUrl;

  private readonly learningPathUrl =
    `${this.apiUrl}/learning-paths`;

  private readonly assignmentUrl =
    `${this.apiUrl}/learning-path-assignments`;

  createLearningPath(
    request: LearningPathCreateRequest
  ): Observable<LearningPath> {
    return this.http.post<LearningPath>(
      this.learningPathUrl,
      request
    );
  }

  updateLearningPath(
    pathId: string,
    request: LearningPathUpdateRequest
  ): Observable<LearningPath> {
    return this.http.put<LearningPath>(
      `${this.learningPathUrl}/${pathId}`,
      request
    );
  }

  getLearningPathById(
    pathId: string
  ): Observable<LearningPath> {
    return this.http.get<LearningPath>(
      `${this.learningPathUrl}/${pathId}`
    );
  }

  getLearningPathByCode(
    pathCode: string
  ): Observable<LearningPath> {
    return this.http.get<LearningPath>(
      `${this.learningPathUrl}/code/${encodeURIComponent(
        pathCode
      )}`
    );
  }

  getAllLearningPaths():
    Observable<LearningPath[]> {
    return this.http.get<LearningPath[]>(
      this.learningPathUrl
    );
  }

  getLearningPathsByStatus(
    status: LearningPathStatus
  ): Observable<LearningPath[]> {
    return this.http.get<LearningPath[]>(
      `${this.learningPathUrl}/status/${status}`
    );
  }

  getLearningPathsByCategory(
    category: string
  ): Observable<LearningPath[]> {
    const params =
      new HttpParams().set(
        'category',
        category
      );

    return this.http.get<LearningPath[]>(
      `${this.learningPathUrl}/category`,
      { params }
    );
  }

  getLearningPathsByTargetRole(
    targetRole: string
  ): Observable<LearningPath[]> {
    const params =
      new HttpParams().set(
        'targetRole',
        targetRole
      );

    return this.http.get<LearningPath[]>(
      `${this.learningPathUrl}/target-role`,
      { params }
    );
  }

  publishLearningPath(
    pathId: string
  ): Observable<LearningPath> {
    return this.http.post<LearningPath>(
      `${this.learningPathUrl}/${pathId}/publish`,
      {}
    );
  }

  archiveLearningPath(
    pathId: string
  ): Observable<LearningPath> {
    return this.http.post<LearningPath>(
      `${this.learningPathUrl}/${pathId}/archive`,
      {}
    );
  }

  deleteLearningPath(
    pathId: string
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.learningPathUrl}/${pathId}`
    );
  }

  addCourseToLearningPath(
    pathId: string,
    request: LearningPathCourseRequest
  ): Observable<LearningPathCourse> {
    return this.http.post<LearningPathCourse>(
      `${this.learningPathUrl}/${pathId}/courses`,
      request
    );
  }

  updateLearningPathCourse(
    pathId: string,
    pathCourseId: string,
    request: LearningPathCourseRequest
  ): Observable<LearningPathCourse> {
    return this.http.put<LearningPathCourse>(
      `${this.learningPathUrl}/${pathId}/courses/${pathCourseId}`,
      request
    );
  }

  getLearningPathCourses(
    pathId: string
  ): Observable<LearningPathCourse[]> {
    return this.http.get<LearningPathCourse[]>(
      `${this.learningPathUrl}/${pathId}/courses`
    );
  }

  removeCourseFromLearningPath(
    pathId: string,
    pathCourseId: string
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.learningPathUrl}/${pathId}/courses/${pathCourseId}`
    );
  }

  assignLearningPath(
    pathId: string,
    request: LearningPathAssignmentRequest
  ): Observable<LearningPathAssignment> {
    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/path/${pathId}`,
      request
    );
  }

  getAssignmentById(
    assignmentId: string
  ): Observable<LearningPathAssignment> {
    return this.http.get<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}`
    );
  }

  getAssignment(
    pathId: string,
    learnerId: string
  ): Observable<LearningPathAssignment> {
    return this.http.get<LearningPathAssignment>(
      `${this.assignmentUrl}/path/${pathId}/learner/${learnerId}`
    );
  }

  getAssignmentsByLearner(
    learnerId: string
  ): Observable<LearningPathAssignment[]> {
    return this.http.get<LearningPathAssignment[]>(
      `${this.assignmentUrl}/learner/${learnerId}`
    );
  }

  getAssignmentsByPath(
    pathId: string
  ): Observable<LearningPathAssignment[]> {
    return this.http.get<LearningPathAssignment[]>(
      `${this.assignmentUrl}/path/${pathId}`
    );
  }

  getAssignmentsByStatus(
    status: LearningPathAssignmentStatus
  ): Observable<LearningPathAssignment[]> {
    return this.http.get<LearningPathAssignment[]>(
      `${this.assignmentUrl}/status/${status}`
    );
  }

  getLearnerAssignmentsByStatus(
    learnerId: string,
    status: LearningPathAssignmentStatus
  ): Observable<LearningPathAssignment[]> {
    return this.http.get<LearningPathAssignment[]>(
      `${this.assignmentUrl}/learner/${learnerId}/status/${status}`
    );
  }

  startLearningPath(
    assignmentId: string
  ): Observable<LearningPathAssignment> {
    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}/start`,
      {}
    );
  }

  refreshProgress(
    assignmentId: string
  ): Observable<LearningPathAssignment> {
    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}/refresh-progress`,
      {}
    );
  }

  completeLearningPath(
    assignmentId: string
  ): Observable<LearningPathAssignment> {
    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}/complete`,
      {}
    );
  }

  cancelAssignment(
    assignmentId: string,
    reason: string
  ): Observable<LearningPathAssignment> {
    const params =
      new HttpParams().set(
        'reason',
        reason
      );

    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}/cancel`,
      {},
      { params }
    );
  }

  reactivateAssignment(
    assignmentId: string
  ): Observable<LearningPathAssignment> {
    return this.http.post<LearningPathAssignment>(
      `${this.assignmentUrl}/${assignmentId}/reactivate`,
      {}
    );
  }

  countAssignmentsByLearner(
    learnerId: string
  ): Observable<number> {
    return this.http.get<number>(
      `${this.assignmentUrl}/count/learner/${learnerId}`
    );
  }

  countAssignmentsByPath(
    pathId: string
  ): Observable<number> {
    return this.http.get<number>(
      `${this.assignmentUrl}/count/path/${pathId}`
    );
  }

  countAssignmentsByPathAndStatus(
    pathId: string,
    status: LearningPathAssignmentStatus
  ): Observable<number> {
    return this.http.get<number>(
      `${this.assignmentUrl}/count/path/${pathId}/status/${status}`
    );
  }
}