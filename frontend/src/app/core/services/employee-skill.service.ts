import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { EmployeeSkill } from '../../learning/models/employee-skill';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeSkillService {
  private readonly baseUrl = `${environment.apiUrl}/employeeSkills`;

  private mockEmployeeSkills: EmployeeSkill[] = [
    { employeeSkillId: 201, employeeId: 101, skillId: 1, proficiencyLevel: 4, yearsOfExperience: 5 },
    { employeeSkillId: 202, employeeId: 101, skillId: 2, proficiencyLevel: 3, yearsOfExperience: 3 },
    { employeeSkillId: 203, employeeId: 102, skillId: 1, proficiencyLevel: 5, yearsOfExperience: 10 },
    { employeeSkillId: 204, employeeId: 102, skillId: 2, proficiencyLevel: 5, yearsOfExperience: 8 },
    { employeeSkillId: 205, employeeId: 103, skillId: 5, proficiencyLevel: 4, yearsOfExperience: 6 },
    { employeeSkillId: 206, employeeId: 104, skillId: 4, proficiencyLevel: 3, yearsOfExperience: 4 }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<EmployeeSkill[]> {
    if (environment.useMock) {
      return of([...this.mockEmployeeSkills]);
    }
    return this.http.get<EmployeeSkill[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  getById(id: number): Observable<EmployeeSkill> {
    if (environment.useMock) {
      const empSkill = this.mockEmployeeSkills.find(es => es.employeeSkillId === id);
      return empSkill ? of({ ...empSkill }) : throwError(() => new Error('Employee skill mapping not found'));
    }
    return this.http.get<EmployeeSkill>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  getByEmployeeId(employeeId: number): Observable<EmployeeSkill[]> {
    if (environment.useMock) {
      const mappings = this.mockEmployeeSkills.filter(es => es.employeeId === employeeId);
      return of(mappings);
    }
    return this.http.get<EmployeeSkill[]>(this.baseUrl).pipe(
      map(list => list.filter(es => es.employeeId === employeeId)),
      catchError(this.handleError)
    );
  }

  create(employeeSkill: EmployeeSkill): Observable<EmployeeSkill> {
    if (environment.useMock) {
      const newId = employeeSkill.employeeSkillId || Math.max(...this.mockEmployeeSkills.map(es => es.employeeSkillId), 0) + 1;
      const newMapping = { ...employeeSkill, employeeSkillId: newId };
      this.mockEmployeeSkills.push(newMapping);
      return of(newMapping);
    }
    return this.http.post<EmployeeSkill>(this.baseUrl, employeeSkill).pipe(
      catchError(this.handleError)
    );
  }

  update(id: number, employeeSkill: EmployeeSkill): Observable<EmployeeSkill> {
    if (environment.useMock) {
      const index = this.mockEmployeeSkills.findIndex(es => es.employeeSkillId === id);
      if (index === -1) {
        return throwError(() => new Error('Employee skill mapping not found'));
      }
      this.mockEmployeeSkills[index] = { ...employeeSkill, employeeSkillId: id };
      return of(this.mockEmployeeSkills[index]);
    }
    return this.http.put<EmployeeSkill>(`${this.baseUrl}/${id}`, employeeSkill).pipe(
      catchError(this.handleError)
    );
  }

  delete(id: number): Observable<string> {
    if (environment.useMock) {
      const index = this.mockEmployeeSkills.findIndex(es => es.employeeSkillId === id);
      if (index === -1) {
        return throwError(() => new Error('Employee skill mapping not found'));
      }
      this.mockEmployeeSkills.splice(index, 1);
      return of('Employee skill deleted successfully');
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
