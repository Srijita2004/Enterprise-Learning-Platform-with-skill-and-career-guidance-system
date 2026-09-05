import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Skill } from '../../learning/models/skill';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private readonly baseUrl = `${environment.apiUrl}/skills`;

  private mockSkills: Skill[] = [
    { skillId: 1, skillName: 'Java 17 & Concurrency', category: 'TECHNICAL', description: 'Core Java, Virtual Threads, Streams, Memory Optimization, JVM Tuning' },
    { skillId: 2, skillName: 'Spring Boot 4 & Cloud', category: 'TECHNICAL', description: 'Microservices architectures, Spring Cloud Gateway, Eureka, Hibernate, Spring Security' },
    { skillId: 3, skillName: 'Angular 21 & Signals', category: 'TECHNICAL', description: 'TypeScript SPA Framework, Standalone Architecture, Signals, Sub-second LCP' },
    { skillId: 4, skillName: 'PostgreSQL & Query Tuning', category: 'TECHNICAL', description: 'Relational Database, complex indexing, partitioning, query optimization' },
    { skillId: 5, skillName: 'Kubernetes & Multi-Cloud', category: 'TECHNICAL', description: 'Container orchestration, Helm charts, service mesh, zero-downtime rollouts' },
    { skillId: 6, skillName: 'Automated QA & Testing', category: 'TECHNICAL', description: 'JUnit, Mockito, Cypress, Playwright, performance benchmarking' },
    { skillId: 7, skillName: 'Zero-Trust Cybersecurity', category: 'DOMAIN', description: 'JWT authentication, RBAC authorization, TLS encryption, SOC2 compliance' },
    { skillId: 8, skillName: 'AI & Vector MLOps', category: 'TECHNICAL', description: 'Vector embeddings, cosine similarity search, Gemini LLM prompt orchestration' },
    { skillId: 9, skillName: 'DevOps & Terraform IaC', category: 'TECHNICAL', description: 'CI/CD pipeline automation, Docker containerization, Infrastructure as Code' },
    { skillId: 10, skillName: 'Distributed System Design', category: 'TECHNICAL', description: 'Event-driven architecture, Apache Kafka messaging, CAP theorem, resilience' }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Skill[]> {
    if (environment.useMock) {
      return of([...this.mockSkills]);
    }
    return this.http.get<Skill[]>(this.baseUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) return [...this.mockSkills];
        const existingIds = new Set(res.map(s => s.skillId));
        this.mockSkills.forEach(m => {
          if (!existingIds.has(m.skillId)) res.push(m);
        });
        return res;
      }),
      catchError(() => of([...this.mockSkills]))
    );
  }

  getById(id: number): Observable<Skill> {
    if (environment.useMock) {
      const skill = this.mockSkills.find(s => s.skillId === id);
      return skill ? of({ ...skill }) : throwError(() => new Error('Skill not found'));
    }
    return this.http.get<Skill>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  create(skill: Skill): Observable<Skill> {
    if (environment.useMock) {
      const newId = skill.skillId || Math.max(...this.mockSkills.map(s => s.skillId), 0) + 1;
      const newSkill = { ...skill, skillId: newId };
      this.mockSkills.push(newSkill);
      return of(newSkill);
    }
    return this.http.post<Skill>(this.baseUrl, skill).pipe(
      catchError(this.handleError)
    );
  }

  update(id: number, skill: Skill): Observable<Skill> {
    if (environment.useMock) {
      const index = this.mockSkills.findIndex(s => s.skillId === id);
      if (index === -1) {
        return throwError(() => new Error('Skill not found'));
      }
      this.mockSkills[index] = { ...skill, skillId: id };
      return of(this.mockSkills[index]);
    }
    return this.http.put<Skill>(`${this.baseUrl}/${id}`, skill).pipe(
      catchError(this.handleError)
    );
  }

  delete(id: number): Observable<string> {
    if (environment.useMock) {
      const index = this.mockSkills.findIndex(s => s.skillId === id);
      if (index === -1) {
        return throwError(() => new Error('Skill not found'));
      }
      this.mockSkills.splice(index, 1);
      return of('Skill deleted successfully');
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
