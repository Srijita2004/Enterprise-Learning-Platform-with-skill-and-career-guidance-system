import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

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

  private readonly mockCourses: Course[] = [
    {
      courseId: 'crs-1',
      courseCode: 'CRS-SPRING-01',
      title: 'Spring Boot 4 & Cloud Microservices Architecture',
      description: 'Master enterprise microservices, Spring Cloud Gateway, Eureka Discovery, resilience patterns, and PostgreSQL integration.',
      category: 'Backend & Cloud',
      courseType: 'ONLINE',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'PAID',
      price: 450,
      currencyCode: 'USD',
      instructorName: 'Marcus Brodie',
      durationHours: 24,
      maxCapacity: 100,
      enrolledCount: 38,
      passingScore: 80,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 60 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 5 * 86400000).toISOString()
    },
    {
      courseId: 'crs-2',
      courseCode: 'CRS-ANGULAR-01',
      title: 'Modern Angular 21 Enterprise Frontend Architecture',
      description: 'Production-ready Single Page Applications, Standalone Components, Signal-based reactivity, Sub-second LCP optimization.',
      category: 'Frontend Engineering',
      courseType: 'ONLINE',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'PAID',
      price: 380,
      currencyCode: 'USD',
      instructorName: 'John Smith',
      durationHours: 20,
      maxCapacity: 120,
      enrolledCount: 45,
      passingScore: 80,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 50 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 4 * 86400000).toISOString()
    },
    {
      courseId: 'crs-3',
      courseCode: 'CRS-DEVOPS-01',
      title: 'Enterprise CI/CD, Docker & Container Orchestration',
      description: 'Automated GitHub Actions, Docker multi-stage builds, Kubernetes Helm charts, zero-downtime rolling updates.',
      category: 'Cloud & Infrastructure',
      courseType: 'WORKSHOP',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'PAID',
      price: 520,
      currencyCode: 'USD',
      instructorName: 'David Miller',
      durationHours: 28,
      maxCapacity: 80,
      enrolledCount: 29,
      passingScore: 85,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 40 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 3 * 86400000).toISOString()
    },
    {
      courseId: 'crs-4',
      courseCode: 'CRS-AI-01',
      title: 'Staff AI & Vector Space Machine Learning Pathway',
      description: 'Large language models, cosine similarity vector search, RAG pipelines, Google Gemini AI API integration.',
      category: 'Data & Artificial Intelligence',
      courseType: 'ONLINE',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'PAID',
      price: 600,
      currencyCode: 'USD',
      instructorName: 'Jane Doe',
      durationHours: 32,
      maxCapacity: 75,
      enrolledCount: 34,
      passingScore: 85,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 30 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 2 * 86400000).toISOString()
    },
    {
      courseId: 'crs-5',
      courseCode: 'CRS-SEC-01',
      title: 'Zero-Trust Enterprise Cybersecurity & Governance',
      description: 'JWT token signing, stateless RBAC guards, TLS encryption, OAuth2 SSO, vulnerability threat modeling.',
      category: 'Security & Governance',
      courseType: 'ONLINE',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'PAID',
      price: 490,
      currencyCode: 'USD',
      instructorName: 'Elena Rostova',
      durationHours: 18,
      maxCapacity: 90,
      enrolledCount: 22,
      passingScore: 85,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 20 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 1 * 86400000).toISOString()
    },
    {
      courseId: 'crs-6',
      courseCode: 'CRS-DB-01',
      title: 'PostgreSQL Advanced Tuning & High-Throughput Indexing',
      description: 'B-tree/GIN index optimization, table partitioning, connection pool tuning, distributed transaction isolation.',
      category: 'Database Systems',
      courseType: 'WORKSHOP',
      courseLevel: 'ADVANCED',
      status: 'PUBLISHED',
      pricingType: 'FREE',
      price: 0,
      currencyCode: 'USD',
      instructorName: 'Priya Sharma',
      durationHours: 16,
      maxCapacity: 150,
      enrolledCount: 62,
      passingScore: 80,
      certificateEnabled: true,
      createdAt: new Date(Date.now() - 15 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 1 * 86400000).toISOString()
    }
  ];

  getAllCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(this.apiUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) return [...this.mockCourses];
        const existingIds = new Set(res.map(c => c.courseId));
        this.mockCourses.forEach(m => {
          if (!existingIds.has(m.courseId)) res.push(m);
        });
        return res;
      }),
      catchError(() => of([...this.mockCourses]))
    );
  }

  getCourseById(courseId: string): Observable<Course> {
    return this.http.get<Course>(
      `${this.apiUrl}/${courseId}`
    ).pipe(
      catchError(() => {
        const found = this.mockCourses.find(c => c.courseId === courseId || c.courseCode === courseId) || this.mockCourses[0];
        return of({ ...found });
      })
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
