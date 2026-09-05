import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Employee } from '../../learning/models/employee';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly baseUrl = `${environment.apiUrl}/employee`;

  // Complete 10-Employee Enterprise Dataset
  private mockEmployees: Employee[] = [
    { employeeId: 1, employeeName: 'Srijita', designation: 'Senior Java Developer', salary: 115000 },
    { employeeId: 101, employeeName: 'Alex Vance', designation: 'Cloud Infrastructure Engineer', salary: 110000 },
    { employeeId: 102, employeeName: 'Marcus Brodie', designation: 'Principal Systems Architect', salary: 145000 },
    { employeeId: 103, employeeName: 'Sarah Jenkins', designation: 'Senior QA Lead', salary: 98000 },
    { employeeId: 106, employeeName: 'John Smith', designation: 'Frontend Software Engineer', salary: 102000 },
    { employeeId: 107, employeeName: 'Jane Doe', designation: 'Data & Machine Learning Engineer', salary: 125000 },
    { employeeId: 108, employeeName: 'David Miller', designation: 'DevOps & Site Reliability Engineer', salary: 118000 },
    { employeeId: 109, employeeName: 'Elena Rostova', designation: 'Cybersecurity & Compliance Analyst', salary: 130000 },
    { employeeId: 110, employeeName: 'Michael Chang', designation: 'Full-Stack Application Developer', salary: 108000 },
    { employeeId: 111, employeeName: 'Priya Sharma', designation: 'Database Performance Specialist', salary: 122000 }
  ];

  constructor(private http: HttpClient) {}

  private getDynamicEmployees(): Employee[] {
    const list: Employee[] = [];
    try {
      const stored = JSON.parse(localStorage.getItem('ssn_registered_employees') || '[]');
      if (Array.isArray(stored)) list.push(...stored);
    } catch {}

    try {
      const rawSession = sessionStorage.getItem('ssn_auth_user');
      if (rawSession) {
        const u = JSON.parse(rawSession);
        if (u && u.role === 'EMPLOYEE' && u.name) {
          const empId = u.employeeId || 201;
          if (!list.some(e => e.employeeId === empId || (e.employeeName && e.employeeName.toLowerCase().trim() === u.name.toLowerCase().trim()))) {
            list.push({
              employeeId: empId,
              employeeName: u.name,
              designation: 'Enterprise Associate',
              salary: 85000
            });
          }
        }
      }
    } catch {}

    try {
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && (key.startsWith('ssn_') || key.includes('user') || key.includes('account'))) {
          const raw = localStorage.getItem(key);
          if (raw) {
            try {
              const obj = JSON.parse(raw);
              if (obj && obj.role === 'EMPLOYEE' && obj.name) {
                const empId = obj.employeeId || 201;
                if (!list.some(e => e.employeeId === empId || (e.employeeName && e.employeeName.toLowerCase().trim() === obj.name.toLowerCase().trim()))) {
                  list.push({
                    employeeId: empId,
                    employeeName: obj.name,
                    designation: 'Enterprise Associate',
                    salary: 85000
                  });
                }
              }
            } catch {}
          }
        }
      }
    } catch {}

    return list;
  }

  saveDynamicEmployee(emp: Employee): void {
    try {
      const list = this.getDynamicEmployees();
      const existingIdx = list.findIndex(e => e.employeeId === emp.employeeId || (e.employeeName && emp.employeeName && e.employeeName.toLowerCase().trim() === emp.employeeName.toLowerCase().trim()));
      if (existingIdx >= 0) {
        list[existingIdx] = emp;
      } else {
        list.push(emp);
      }
      localStorage.setItem('ssn_registered_employees', JSON.stringify(list));
    } catch {}
  }

  getAll(): Observable<Employee[]> {
    const dynamicEmps = this.getDynamicEmployees();
    const baseList = [...this.mockEmployees];
    
    const seenNames = new Set(baseList.map(e => (e.employeeName || '').toLowerCase().trim()));
    const seenIds = new Set(baseList.map(e => e.employeeId));

    dynamicEmps.forEach(d => {
      const dName = (d.employeeName || '').toLowerCase().trim();
      if (!seenNames.has(dName) && !seenIds.has(d.employeeId)) {
        baseList.push(d);
        seenNames.add(dName);
        seenIds.add(d.employeeId);
      }
    });

    if (environment.useMock) {
      baseList.sort((a, b) => a.employeeId - b.employeeId);
      return of([...baseList]);
    }

    return this.http.get<Employee[]>(this.baseUrl).pipe(
      map(res => {
        if (!res || !Array.isArray(res) || res.length === 0) {
          baseList.sort((a, b) => a.employeeId - b.employeeId);
          return [...baseList];
        }
        const merged = [...res];
        const resNames = new Set(merged.map(e => (e.employeeName || '').toLowerCase().trim()));
        const resIds = new Set(merged.map(e => e.employeeId));

        baseList.forEach(m => {
          const mName = (m.employeeName || '').toLowerCase().trim();
          if (!resNames.has(mName) && !resIds.has(m.employeeId)) {
            merged.push(m);
            resNames.add(mName);
            resIds.add(m.employeeId);
          }
        });
        merged.sort((a, b) => a.employeeId - b.employeeId);
        return merged;
      }),
      catchError(() => {
        baseList.sort((a, b) => a.employeeId - b.employeeId);
        return of([...baseList]);
      })
    );
  }

  getById(id: number): Observable<Employee> {
    const all = [...this.mockEmployees, ...this.getDynamicEmployees()];
    const emp = all.find(e => e.employeeId === id) || all[0];
    if (environment.useMock) {
      return of({ ...emp });
    }
    return this.http.get<Employee>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => of({ ...emp }))
    );
  }

  create(employee: Employee): Observable<Employee> {
    const dynamicList = this.getDynamicEmployees();
    const allKnown = [...this.mockEmployees, ...dynamicList];
    const existingIds = allKnown.map(e => e.employeeId || 0);
    const newId = employee.employeeId || (existingIds.length > 0 ? Math.max(...existingIds, 200) + 1 : 201);
    const newEmp: Employee = { ...employee, employeeId: newId };
    
    this.saveDynamicEmployee(newEmp);
    this.mockEmployees.push(newEmp);

    return this.http.post<Employee>(this.baseUrl, newEmp).pipe(
      catchError(() => of(newEmp))
    );
  }

  update(id: number, employee: Employee): Observable<Employee> {
    if (environment.useMock) {
      const index = this.mockEmployees.findIndex(e => e.employeeId === id);
      if (index === -1) {
        return throwError(() => new Error('Employee not found'));
      }
      this.mockEmployees[index] = { ...employee, employeeId: id };
      return of(this.mockEmployees[index]);
    }
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, employee).pipe(
      catchError(this.handleError)
    );
  }

  delete(id: number): Observable<string> {
    if (environment.useMock) {
      const index = this.mockEmployees.findIndex(e => e.employeeId === id);
      if (index === -1) {
        return throwError(() => new Error('Employee not found'));
      }
      this.mockEmployees.splice(index, 1);
      return of('Employee deleted successfully');
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
