import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Course } from '../models/course.model';

export interface CourseSaveRequest {
  courseCode?: string;
  title: string;
  description?: string | null;
  category: string;
  courseType: string;
  courseLevel: string;
  pricingType: string;
  price: number;
  currencyCode: string;
  instructorName: string;
  durationHours: number;
  maxCapacity: number;
  passingScore: number;
  certificateEnabled: boolean;
  startDate?: string | null;
  endDate?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    `${environment.learningApiUrl}/courses`;

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl);
  }

  getCourseById(courseId: string): Observable<Course> {
    return this.http.get<Course>(
      `${this.apiUrl}/${courseId}`
    );
  }

  createCourse(request: CourseSaveRequest): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, request);
  }

  updateCourse(
    courseId: string,
    request: CourseSaveRequest
  ): Observable<Course> {
    return this.http.put<Course>(
      `${this.apiUrl}/${courseId}`,
      request
    );
  }

  deleteCourse(courseId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${courseId}`
    );
  }

  publishCourse(courseId: string): Observable<Course> {
    return this.http.patch<Course>(
      `${this.apiUrl}/${courseId}/publish`,
      {}
    );
  }

  archiveCourse(courseId: string): Observable<Course> {
    return this.http.patch<Course>(
      `${this.apiUrl}/${courseId}/archive`,
      {}
    );
  }
}
