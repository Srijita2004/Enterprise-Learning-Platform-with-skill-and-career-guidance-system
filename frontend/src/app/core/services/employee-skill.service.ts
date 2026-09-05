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
    // Srijita (ID: 1)
    { employeeSkillId: 101, employeeId: 1, skillId: 1, proficiencyLevel: 5, yearsOfExperience: 6 },
    { employeeSkillId: 102, employeeId: 1, skillId: 2, proficiencyLevel: 5, yearsOfExperience: 5 },
    { employeeSkillId: 103, employeeId: 1, skillId: 4, proficiencyLevel: 4, yearsOfExperience: 4 },
    { employeeSkillId: 104, employeeId: 1, skillId: 10, proficiencyLevel: 4, yearsOfExperience: 4 },

    // Alex Vance (ID: 101)
    { employeeSkillId: 201, employeeId: 101, skillId: 5, proficiencyLevel: 5, yearsOfExperience: 7 },
    { employeeSkillId: 202, employeeId: 101, skillId: 9, proficiencyLevel: 5, yearsOfExperience: 6 },
    { employeeSkillId: 203, employeeId: 101, skillId: 10, proficiencyLevel: 4, yearsOfExperience: 5 },

    // Marcus Brodie (ID: 102)
    { employeeSkillId: 301, employeeId: 102, skillId: 10, proficiencyLevel: 5, yearsOfExperience: 12 },
    { employeeSkillId: 302, employeeId: 102, skillId: 5, proficiencyLevel: 5, yearsOfExperience: 10 },
    { employeeSkillId: 303, employeeId: 102, skillId: 2, proficiencyLevel: 5, yearsOfExperience: 11 },

    // Sarah Jenkins (ID: 103)
    { employeeSkillId: 401, employeeId: 103, skillId: 6, proficiencyLevel: 5, yearsOfExperience: 8 },
    { employeeSkillId: 402, employeeId: 103, skillId: 1, proficiencyLevel: 4, yearsOfExperience: 5 },
    { employeeSkillId: 403, employeeId: 103, skillId: 9, proficiencyLevel: 4, yearsOfExperience: 4 },

    // John Smith (ID: 106)
    { employeeSkillId: 501, employeeId: 106, skillId: 3, proficiencyLevel: 5, yearsOfExperience: 6 },
    { employeeSkillId: 502, employeeId: 106, skillId: 8, proficiencyLevel: 3, yearsOfExperience: 2 },

    // Jane Doe (ID: 107)
    { employeeSkillId: 601, employeeId: 107, skillId: 8, proficiencyLevel: 5, yearsOfExperience: 7 },
    { employeeSkillId: 602, employeeId: 107, skillId: 4, proficiencyLevel: 4, yearsOfExperience: 5 },

    // David Miller (ID: 108)
    { employeeSkillId: 701, employeeId: 108, skillId: 9, proficiencyLevel: 5, yearsOfExperience: 8 },
    { employeeSkillId: 702, employeeId: 108, skillId: 5, proficiencyLevel: 5, yearsOfExperience: 6 },

    // Elena Rostova (ID: 109)
    { employeeSkillId: 801, employeeId: 109, skillId: 7, proficiencyLevel: 5, yearsOfExperience: 9 },
    { employeeSkillId: 802, employeeId: 109, skillId: 10, proficiencyLevel: 4, yearsOfExperience: 6 },

    // Michael Chang (ID: 110)
    { employeeSkillId: 901, employeeId: 110, skillId: 1, proficiencyLevel: 4, yearsOfExperience: 5 },
    { employeeSkillId: 902, employeeId: 110, skillId: 3, proficiencyLevel: 4, yearsOfExperience: 4 },

    // Priya Sharma (ID: 111)
    { employeeSkillId: 1001, employeeId: 111, skillId: 4, proficiencyLevel: 5, yearsOfExperience: 9 },
    { employeeSkillId: 1002, employeeId: 111, skillId: 10, proficiencyLevel: 4, yearsOfExperience: 5 }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<EmployeeSkill[]> {
    if (environment.useMock) {
      return of([...this.mockEmployeeSkills]);
    }
    return this.http.get<EmployeeSkill[]>(this.baseUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) return [...this.mockEmployeeSkills];
        return res;
      }),
      catchError(() => of([...this.mockEmployeeSkills]))
    );
  }

  getById(id: number): Observable<EmployeeSkill> {
    if (environment.useMock) {
      const empSkill = this.mockEmployeeSkills.find(es => es.employeeSkillId === id) || this.mockEmployeeSkills[0];
      return of({ ...empSkill });
    }
    return this.http.get<EmployeeSkill>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => {
        const empSkill = this.mockEmployeeSkills.find(es => es.employeeSkillId === id) || this.mockEmployeeSkills[0];
        return of({ ...empSkill });
      })
    );
  }

  getByEmployeeId(employeeId: number): Observable<EmployeeSkill[]> {
    const matching = this.mockEmployeeSkills.filter(es => es.employeeId === employeeId);
    if (environment.useMock) {
      return of(matching);
    }
    return this.http.get<EmployeeSkill[]>(this.baseUrl).pipe(
      map(list => {
        if (!list || !Array.isArray(list)) return matching;
        const filtered = list.filter(es => es.employeeId === employeeId);
        return filtered.length > 0 ? filtered : matching;
      }),
      catchError(() => of(matching))
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
