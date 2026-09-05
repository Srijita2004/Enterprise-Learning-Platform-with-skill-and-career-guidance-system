import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import {
  LearningPath,
  LearningPathAssignment,
  LearningPathAssignmentRequest,
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
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.learningApiUrl;
  private readonly learningPathUrl = `${this.apiUrl}/learning-paths`;
  private readonly assignmentUrl = `${this.apiUrl}/learning-path-assignments`;

  private mockPaths: LearningPath[] = [
    {
      pathId: 'path-1',
      pathCode: 'LP-ARCH-01',
      title: 'Full-Stack Enterprise Cloud Architecture Track',
      description: 'Comprehensive pathway covering Spring Boot microservices, Angular 21 reactive state, and cloud-native resilience.',
      category: 'Enterprise Architecture',
      targetRole: 'Senior Backend Architect',
      level: 'ADVANCED',
      status: 'PUBLISHED',
      estimatedDurationHours: 64,
      totalCourses: 3,
      requiredCourses: 3,
      totalAssignments: 18,
      publishedAt: new Date(Date.now() - 30 * 86400000).toISOString(),
      createdAt: new Date(Date.now() - 30 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 10 * 86400000).toISOString()
    },
    {
      pathId: 'path-2',
      pathCode: 'LP-DEVOPS-01',
      title: 'DevOps, Multi-Region Kubernetes & SRE Mastery',
      description: 'Deep dive into automated CI/CD pipelines, container orchestration, zero-downtime failover, and Prometheus observability.',
      category: 'Cloud & Infrastructure',
      targetRole: 'Principal Cloud Architect',
      level: 'ADVANCED',
      status: 'PUBLISHED',
      estimatedDurationHours: 48,
      totalCourses: 3,
      requiredCourses: 3,
      totalAssignments: 14,
      publishedAt: new Date(Date.now() - 20 * 86400000).toISOString(),
      createdAt: new Date(Date.now() - 20 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 5 * 86400000).toISOString()
    },
    {
      pathId: 'path-3',
      pathCode: 'LP-QA-01',
      title: 'Quality Engineering & Automated Test Infrastructure',
      description: 'Master automated regression frameworks, load testing, performance benchmarks, and release pipeline gates.',
      category: 'Quality Engineering',
      targetRole: 'Quality Engineering Director',
      level: 'INTERMEDIATE',
      status: 'PUBLISHED',
      estimatedDurationHours: 40,
      totalCourses: 3,
      requiredCourses: 2,
      totalAssignments: 10,
      publishedAt: new Date(Date.now() - 15 * 86400000).toISOString(),
      createdAt: new Date(Date.now() - 15 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 2 * 86400000).toISOString()
    },
    {
      pathId: 'path-4',
      pathCode: 'LP-AI-01',
      title: 'Staff AI & Vector Space Machine Learning Architecture',
      description: 'End-to-end MLOps pipelines, RAG vector similarity engines, distributed embeddings, and LLM safety governance.',
      category: 'Data & Artificial Intelligence',
      targetRole: 'Staff AI & MLOps Architect',
      level: 'ADVANCED',
      status: 'PUBLISHED',
      estimatedDurationHours: 56,
      totalCourses: 4,
      requiredCourses: 4,
      totalAssignments: 22,
      publishedAt: new Date(Date.now() - 10 * 86400000).toISOString(),
      createdAt: new Date(Date.now() - 10 * 86400000).toISOString(),
      updatedAt: new Date(Date.now() - 1 * 86400000).toISOString()
    }
  ];

  private mockCoursesMap: Record<string, LearningPathCourse[]> = {
    'path-1': [
      {
        pathCourseId: 'pc-101',
        pathId: 'path-1',
        courseId: 'crs-1',
        courseCode: 'CRS-SPRING-01',
        courseTitle: 'Spring Boot 4 & Cloud Microservices Architecture',
        courseOrder: 1,
        requiredForCompletion: true,
        unlockAfterPrevious: false
      },
      {
        pathCourseId: 'pc-102',
        pathId: 'path-1',
        courseId: 'crs-2',
        courseCode: 'CRS-ANGULAR-01',
        courseTitle: 'Modern Angular 21 Enterprise Frontend Architecture',
        courseOrder: 2,
        requiredForCompletion: true,
        unlockAfterPrevious: true
      },
      {
        pathCourseId: 'pc-103',
        pathId: 'path-1',
        courseId: 'crs-3',
        courseCode: 'CRS-DEVOPS-01',
        courseTitle: 'Enterprise CI/CD, Docker & Container Orchestration',
        courseOrder: 3,
        requiredForCompletion: true,
        unlockAfterPrevious: true
      }
    ]
  };

  private mockAssignments: LearningPathAssignment[] = [
    {
      assignmentId: 'asg-1',
      pathId: 'path-1',
      pathCode: 'LP-ARCH-01',
      pathTitle: 'Full-Stack Enterprise Cloud Architecture Track',
      learnerId: '1',
      status: 'IN_PROGRESS',
      assignmentSource: 'SELF_ASSIGNED',
      progressPercentage: 66,
      currentCourseOrder: 2,
      totalCourses: 3,
      completedCourses: 2,
      overdue: false,
      assignedAt: new Date(Date.now() - 12 * 86400000).toISOString()
    },
    {
      assignmentId: 'asg-2',
      pathId: 'path-2',
      pathCode: 'LP-DEVOPS-01',
      pathTitle: 'DevOps, Multi-Region Kubernetes & SRE Mastery',
      learnerId: '101',
      status: 'IN_PROGRESS',
      assignmentSource: 'MANAGER_ASSIGNED',
      progressPercentage: 33,
      currentCourseOrder: 1,
      totalCourses: 3,
      completedCourses: 1,
      overdue: false,
      assignedAt: new Date(Date.now() - 8 * 86400000).toISOString()
    }
  ];

  getStoredPaths(): LearningPath[] {
    try {
      const stored: LearningPath[] = JSON.parse(localStorage.getItem('ssn_learning_paths') || '[]');
      const merged = [...this.mockPaths];
      stored.forEach(sp => {
        const idx = merged.findIndex(p => p.pathId === sp.pathId);
        if (idx >= 0) {
          merged[idx] = sp;
        } else {
          merged.unshift(sp);
        }
      });
      return merged;
    } catch {
      return this.mockPaths;
    }
  }

  private saveStoredPaths(paths: LearningPath[]): void {
    try {
      localStorage.setItem('ssn_learning_paths', JSON.stringify(paths));
    } catch {}
  }

  private getStoredAssignments(): LearningPathAssignment[] {
    try {
      return JSON.parse(localStorage.getItem('ssn_learning_path_assignments') || '[]');
    } catch {
      return [];
    }
  }

  private saveStoredAssignments(asgs: LearningPathAssignment[]): void {
    try {
      localStorage.setItem('ssn_learning_path_assignments', JSON.stringify(asgs));
    } catch {}
  }

  createLearningPath(request: LearningPathCreateRequest): Observable<LearningPath> {
    const created: LearningPath = {
      pathId: `path-${Date.now()}`,
      pathCode: request.pathCode || `LP-${Date.now()}`,
      title: request.title,
      description: request.description,
      category: request.category || 'General Engineering',
      targetRole: request.targetRole || 'Enterprise Professional',
      level: request.level || 'INTERMEDIATE',
      status: 'DRAFT',
      estimatedDurationHours: request.estimatedDurationHours || 30,
      totalCourses: 0,
      requiredCourses: 0,
      totalAssignments: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    
    const all = this.getStoredPaths();
    all.unshift(created);
    this.saveStoredPaths(all);
    this.mockPaths = all;

    return this.http.post<LearningPath>(this.learningPathUrl, request).pipe(
      catchError(() => of(created))
    );
  }

  updateLearningPath(pathId: string, request: LearningPathUpdateRequest): Observable<LearningPath> {
    const all = this.getStoredPaths();
    const found = all.find(p => p.pathId === pathId);
    if (found) {
      Object.assign(found, request);
      found.updatedAt = new Date().toISOString();
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }
    return this.http.put<LearningPath>(`${this.learningPathUrl}/${pathId}`, request).pipe(
      catchError(() => of(found || this.mockPaths[0]))
    );
  }

  getLearningPathById(pathId: string): Observable<LearningPath> {
    const all = this.getStoredPaths();
    const found = all.find(p => p.pathId === pathId) || this.mockPaths[0];
    return this.http.get<LearningPath>(`${this.learningPathUrl}/${pathId}`).pipe(
      catchError(() => of(found))
    );
  }

  getLearningPathByCode(pathCode: string): Observable<LearningPath> {
    const all = this.getStoredPaths();
    const found = all.find(p => p.pathCode.toLowerCase() === pathCode.toLowerCase()) || this.mockPaths[0];
    return this.http.get<LearningPath>(`${this.learningPathUrl}/code/${encodeURIComponent(pathCode)}`).pipe(
      catchError(() => of(found))
    );
  }

  getAllLearningPaths(): Observable<LearningPath[]> {
    const all = this.getStoredPaths();
    return this.http.get<LearningPath[]>(this.learningPathUrl).pipe(
      map(res => {
        if (!res || res.length === 0) return all;
        // Merge server paths with local created paths
        const merged = [...res];
        all.forEach(p => {
          if (!merged.some(m => m.pathId === p.pathId || m.pathCode === p.pathCode)) {
            merged.unshift(p);
          }
        });
        return merged;
      }),
      catchError(() => of(all))
    );
  }

  getLearningPathsByStatus(status: LearningPathStatus): Observable<LearningPath[]> {
    const all = this.getStoredPaths();
    const filtered = all.filter(p => p.status === status);
    return this.http.get<LearningPath[]>(`${this.learningPathUrl}/status/${status}`).pipe(
      map(res => (!res || res.length === 0) ? filtered : res),
      catchError(() => of(filtered))
    );
  }

  getLearningPathsByCategory(category: string): Observable<LearningPath[]> {
    const params = new HttpParams().set('category', category);
    const all = this.getStoredPaths();
    return this.http.get<LearningPath[]>(`${this.learningPathUrl}/category`, { params }).pipe(
      catchError(() => of(all.filter(p => p.category.toLowerCase().includes(category.toLowerCase()))))
    );
  }

  getLearningPathsByTargetRole(targetRole: string): Observable<LearningPath[]> {
    const params = new HttpParams().set('targetRole', targetRole);
    const all = this.getStoredPaths();
    return this.http.get<LearningPath[]>(`${this.learningPathUrl}/target-role`, { params }).pipe(
      catchError(() => of(all.filter(p => p.targetRole?.toLowerCase().includes(targetRole.toLowerCase()))))
    );
  }

  publishLearningPath(pathId: string): Observable<LearningPath> {
    const all = this.getStoredPaths();
    const found = all.find(p => p.pathId === pathId);
    if (found) {
      found.status = 'PUBLISHED';
      found.publishedAt = new Date().toISOString();
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }
    return this.http.post<LearningPath>(`${this.learningPathUrl}/${pathId}/publish`, {}).pipe(
      catchError(() => of(found || this.mockPaths[0]))
    );
  }

  archiveLearningPath(pathId: string): Observable<LearningPath> {
    const all = this.getStoredPaths();
    const found = all.find(p => p.pathId === pathId);
    if (found) {
      found.status = 'ARCHIVED';
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }
    return this.http.post<LearningPath>(`${this.learningPathUrl}/${pathId}/archive`, {}).pipe(
      catchError(() => of(found || this.mockPaths[0]))
    );
  }

  deleteLearningPath(pathId: string): Observable<void> {
    const all = this.getStoredPaths();
    const idx = all.findIndex(p => p.pathId === pathId);
    if (idx !== -1) {
      all.splice(idx, 1);
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }
    return this.http.delete<void>(`${this.learningPathUrl}/${pathId}`).pipe(
      catchError(() => of(undefined))
    );
  }

  addCourseToLearningPath(pathId: string, request: LearningPathCourseRequest): Observable<LearningPathCourse> {
    const created: LearningPathCourse = {
      pathCourseId: `pc-${Date.now()}`,
      pathId: pathId,
      courseId: request.courseId,
      courseCode: 'CRS-ADV',
      courseTitle: 'Enterprise Curriculum Course',
      courseOrder: request.courseOrder || 1,
      requiredForCompletion: request.requiredForCompletion !== undefined ? request.requiredForCompletion : true,
      unlockAfterPrevious: request.unlockAfterPrevious !== undefined ? request.unlockAfterPrevious : false
    };
    if (!this.mockCoursesMap[pathId]) this.mockCoursesMap[pathId] = [];
    this.mockCoursesMap[pathId].push(created);

    const all = this.getStoredPaths();
    const target = all.find(p => p.pathId === pathId);
    if (target) {
      target.totalCourses = (target.totalCourses || 0) + 1;
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }

    return this.http.post<LearningPathCourse>(`${this.learningPathUrl}/${pathId}/courses`, request).pipe(
      catchError(() => of(created))
    );
  }

  updateLearningPathCourse(pathId: string, pathCourseId: string, request: LearningPathCourseRequest): Observable<LearningPathCourse> {
    const list = this.mockCoursesMap[pathId] || [];
    const found = list.find(c => c.pathCourseId === pathCourseId);
    if (found) {
      Object.assign(found, request);
    }
    return this.http.put<LearningPathCourse>(`${this.learningPathUrl}/${pathId}/courses/${pathCourseId}`, request).pipe(
      catchError(() => of(found || list[0]))
    );
  }

  getLearningPathCourses(pathId: string): Observable<LearningPathCourse[]> {
    const courses = this.mockCoursesMap[pathId] || this.mockCoursesMap['path-1'] || [];
    return this.http.get<LearningPathCourse[]>(`${this.learningPathUrl}/${pathId}/courses`).pipe(
      map(res => (!res || res.length === 0) ? courses : res),
      catchError(() => of(courses))
    );
  }

  removeCourseFromLearningPath(pathId: string, pathCourseId: string): Observable<void> {
    const list = this.mockCoursesMap[pathId] || [];
    const idx = list.findIndex(c => c.pathCourseId === pathCourseId);
    if (idx !== -1) list.splice(idx, 1);

    const all = this.getStoredPaths();
    const target = all.find(p => p.pathId === pathId);
    if (target && target.totalCourses > 0) {
      target.totalCourses -= 1;
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }

    return this.http.delete<void>(`${this.learningPathUrl}/${pathId}/courses/${pathCourseId}`).pipe(
      catchError(() => of(undefined))
    );
  }

  assignLearningPath(pathId: string, request: LearningPathAssignmentRequest): Observable<LearningPathAssignment> {
    const all = this.getStoredPaths();
    const targetPath = all.find(p => p.pathId === pathId);
    
    // Auto-publish path if it was draft so assignment is active
    if (targetPath && targetPath.status !== 'PUBLISHED') {
      targetPath.status = 'PUBLISHED';
      targetPath.publishedAt = new Date().toISOString();
    }
    if (targetPath) {
      targetPath.totalAssignments = (targetPath.totalAssignments || 0) + 1;
      this.saveStoredPaths(all);
      this.mockPaths = all;
    }

    const pathCode = targetPath ? targetPath.pathCode : 'LP-01';
    const pathTitle = targetPath ? targetPath.title : 'Learning Path';

    let targetLearnerName = '';
    try {
      const dynamicLearners: any[] = JSON.parse(localStorage.getItem('ssn_registered_learners') || '[]');
      const found = dynamicLearners.find(l => l.userId === request.learnerId);
      if (found) targetLearnerName = found.fullName;
    } catch {}

    const assigned: LearningPathAssignment = {
      assignmentId: `asg-${Date.now()}`,
      pathId: pathId,
      pathCode: pathCode,
      pathTitle: pathTitle,
      learnerId: request.learnerId,
      learnerName: targetLearnerName,
      status: 'ASSIGNED',
      assignmentSource: request.assignmentSource || 'MANAGER_ASSIGNED',
      progressPercentage: 0,
      totalCourses: targetPath?.totalCourses || 1,
      completedCourses: 0,
      overdue: false,
      assignedAt: new Date().toISOString()
    };

    const storedAsgs = this.getStoredAssignments();
    storedAsgs.unshift(assigned);
    this.saveStoredAssignments(storedAsgs);
    this.mockAssignments = storedAsgs;

    return this.http.post<LearningPathAssignment>(`${this.assignmentUrl}/path/${pathId}`, request).pipe(
      catchError(() => of(assigned))
    );
  }

  getAssignmentById(assignmentId: string): Observable<LearningPathAssignment> {
    const stored = this.getStoredAssignments();
    const fallback = stored.find(a => a.assignmentId === assignmentId) || this.mockAssignments.find(a => a.assignmentId === assignmentId) || this.mockAssignments[0];
    return this.http.get<LearningPathAssignment>(`${this.assignmentUrl}/${assignmentId}`).pipe(
      catchError(() => of(fallback))
    );
  }

  getAssignmentsByLearner(learnerId: string): Observable<LearningPathAssignment[]> {
    const stored = this.getStoredAssignments();
    
    let currentLearnerName = '';
    try {
      const rawUser = sessionStorage.getItem('ssn_auth_user') || localStorage.getItem('ssn_auth_user');
      if (rawUser) {
        const u = JSON.parse(rawUser);
        currentLearnerName = (u.name || '').toLowerCase().trim();
      }
    } catch {}

    const matches = stored.filter(a => 
      a.learnerId === learnerId ||
      (a.learnerName && currentLearnerName && a.learnerName.toLowerCase().trim() === currentLearnerName)
    );

    return this.http.get<LearningPathAssignment[]>(`${this.assignmentUrl}/learner/${learnerId}`).pipe(
      map(res => {
        const merged = [...(res || [])];
        matches.forEach(m => {
          if (!merged.some(a => a.assignmentId === m.assignmentId || (a.pathId === m.pathId && a.learnerId === m.learnerId))) {
            merged.unshift(m);
          }
        });
        return merged;
      }),
      catchError(() => of(matches))
    );
  }

  getAssignmentsByPath(pathId: string): Observable<LearningPathAssignment[]> {
    const stored = this.getStoredAssignments();
    const matches = stored.filter(a => a.pathId === pathId);
    return this.http.get<LearningPathAssignment[]>(`${this.assignmentUrl}/path/${pathId}`).pipe(
      map(res => (!res || res.length === 0) ? matches : res),
      catchError(() => of(matches))
    );
  }

  startLearningPath(pathId: string, learnerId?: string): Observable<LearningPathAssignment> {
    const lrnId = learnerId || '1';
    const stored = this.getStoredAssignments();
    const found = stored.find(a => a.pathId === pathId && (a.learnerId === lrnId || !learnerId));
    if (found) {
      found.status = 'IN_PROGRESS';
      this.saveStoredAssignments(stored);
      return of(found);
    }
    
    const all = this.getStoredPaths();
    const path = all.find(p => p.pathId === pathId);

    const newAss: LearningPathAssignment = {
      assignmentId: `asg-${Date.now()}`,
      pathId,
      pathCode: path ? path.pathCode : 'LP-01',
      pathTitle: path ? path.title : 'Learning Path',
      learnerId: lrnId,
      status: 'IN_PROGRESS',
      assignmentSource: 'SELF_ASSIGNED',
      progressPercentage: 0,
      totalCourses: path?.totalCourses || 1,
      completedCourses: 0,
      overdue: false,
      assignedAt: new Date().toISOString()
    };
    stored.unshift(newAss);
    this.saveStoredAssignments(stored);
    return of(newAss);
  }

  refreshProgress(assignmentId: string): Observable<LearningPathAssignment> {
    const stored = this.getStoredAssignments();
    const found = stored.find(a => a.assignmentId === assignmentId) || this.mockAssignments[0];
    return of(found);
  }
}