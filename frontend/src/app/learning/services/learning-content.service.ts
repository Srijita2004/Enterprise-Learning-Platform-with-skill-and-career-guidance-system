import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  ContentProgress,
  ContentProgressUpdateRequest,
  CourseContent,
  CourseModule,
  CourseProgressSummary
} from '../models/learning-content.model';

@Injectable({
  providedIn: 'root'
})
export class LearningContentService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    environment.learningApiUrl;

  getPublishedModules(
    courseId: string
  ): Observable<CourseModule[]> {
    return this.http.get<CourseModule[]>(
      `${this.apiUrl}/courses/${courseId}/modules/published`
    );
  }

  // ---- Admin: Module management ----

  getAllModules(
    courseId: string
  ): Observable<CourseModule[]> {
    return this.http.get<CourseModule[]>(
      `${this.apiUrl}/courses/${courseId}/modules`
    );
  }

  createModule(
    courseId: string,
    request: { title: string; description?: string | null; moduleOrder: number }
  ): Observable<CourseModule> {
    return this.http.post<CourseModule>(
      `${this.apiUrl}/courses/${courseId}/modules`,
      request
    );
  }

  updateModule(
    courseId: string,
    moduleId: string,
    request: { title: string; description?: string | null; moduleOrder: number }
  ): Observable<CourseModule> {
    return this.http.put<CourseModule>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}`,
      request
    );
  }

  deleteModule(
    courseId: string,
    moduleId: string
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}`
    );
  }

  publishModule(
    courseId: string,
    moduleId: string
  ): Observable<CourseModule> {
    return this.http.patch<CourseModule>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/publish`,
      {}
    );
  }

  unpublishModule(
    courseId: string,
    moduleId: string
  ): Observable<CourseModule> {
    return this.http.patch<CourseModule>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/unpublish`,
      {}
    );
  }

  // ---- Admin: Content management ----

  getAllContents(
    courseId: string,
    moduleId: string
  ): Observable<CourseContent[]> {
    return this.http.get<CourseContent[]>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents`
    );
  }

  createContent(
    courseId: string,
    moduleId: string,
    request: Record<string, unknown>
  ): Observable<CourseContent> {
    return this.http.post<CourseContent>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents`,
      request
    );
  }

  updateContent(
    courseId: string,
    moduleId: string,
    contentId: string,
    request: Record<string, unknown>
  ): Observable<CourseContent> {
    return this.http.put<CourseContent>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents/${contentId}`,
      request
    );
  }

  deleteContent(
    courseId: string,
    moduleId: string,
    contentId: string
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents/${contentId}`
    );
  }

  publishContent(
    courseId: string,
    moduleId: string,
    contentId: string
  ): Observable<CourseContent> {
    return this.http.patch<CourseContent>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents/${contentId}/publish`,
      {}
    );
  }

  unpublishContent(
    courseId: string,
    moduleId: string,
    contentId: string
  ): Observable<CourseContent> {
    return this.http.patch<CourseContent>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents/${contentId}/unpublish`,
      {}
    );
  }

  getPublishedContentsForLearner(
    courseId: string,
    moduleId: string,
    learnerId: string
  ): Observable<CourseContent[]> {
    return this.http.get<CourseContent[]>(
      `${this.apiUrl}/courses/${courseId}/modules/${moduleId}/contents/published/learner/${learnerId}`
    );
  }

  getProgressByEnrollment(
    enrollmentId: string
  ): Observable<ContentProgress[]> {
    return this.http.get<ContentProgress[]>(
      `${this.apiUrl}/enrollments/${enrollmentId}/progress`
    );
  }

  getProgressSummary(
    enrollmentId: string
  ): Observable<CourseProgressSummary> {
    return this.http.get<CourseProgressSummary>(
      `${this.apiUrl}/enrollments/${enrollmentId}/progress/summary`
    );
  }

  updateContentProgress(
    enrollmentId: string,
    contentId: string,
    request: ContentProgressUpdateRequest
  ): Observable<ContentProgress> {
    return this.http.patch<ContentProgress>(
      `${this.apiUrl}/enrollments/${enrollmentId}/progress/contents/${contentId}`,
      request
    );
  }
}