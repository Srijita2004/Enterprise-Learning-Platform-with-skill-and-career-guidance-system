import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Skill } from '../../learning/models/skill';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SkillService {
  private readonly baseUrl = `${environment.apiUrl}/skills`;

  private mockSkills: Skill[] = [
    { skillId: 1, skillName: 'Java', category: 'TECHNICAL', description: 'Core Java, Streams, Multithreading, Memory Management' },
    { skillId: 2, skillName: 'Spring Boot', category: 'TECHNICAL', description: 'Microservices architectures, Spring Cloud, Hibernate, Spring Security' },
    { skillId: 3, skillName: 'Angular', category: 'TECHNICAL', description: 'TypeScript SPA Framework, Standalone Components, Signals, RxJS' },
    { skillId: 4, skillName: 'PostgreSQL', category: 'TECHNICAL', description: 'Relational Database, complex indexing, queries optimization' },
    { skillId: 5, skillName: 'Agile Methodology', category: 'SOFT', description: 'Sprint Planning, Scrum ceremonies, project tracking' },
    { skillId: 6, skillName: 'Financial Analysis', category: 'DOMAIN', description: 'Investment portfolios, banking regulations, and credit analysis' }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Skill[]> {
    if (environment.useMock) {
      return of([...this.mockSkills]);
    }
    return this.http.get<Skill[]>(this.baseUrl).pipe(
      catchError(this.handleError)
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
