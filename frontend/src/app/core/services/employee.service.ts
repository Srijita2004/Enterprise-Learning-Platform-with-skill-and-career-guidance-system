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

  // Realistic mock data store
  private mockEmployees: Employee[] = [
    { employeeId: 101, employeeName: 'Alice Vance', designation: 'Senior Java Developer', salary: 98000 },
    { employeeId: 102, employeeName: 'Marcus Brodie', designation: 'Principal Architect', salary: 145000 },
    { employeeId: 103, employeeName: 'Sarah Jenkins', designation: 'QA Lead', salary: 85000 },
    { employeeId: 104, employeeName: 'David Kross', designation: 'DevOps Engineer', salary: 92000 },
    { employeeId: 105, employeeName: 'Emily Watson', designation: 'Product Specialist', salary: 78000 }
  ];

  constructor(private http: HttpClient) {}

  getAll(): Observable<Employee[]> {
    if (environment.useMock) {
      return of([...this.mockEmployees]);
    }
    return this.http.get<Employee[]>(this.baseUrl).pipe(
      catchError(this.handleError)
    );
  }

  getById(id: number): Observable<Employee> {
    if (environment.useMock) {
      const emp = this.mockEmployees.find(e => e.employeeId === id);
      return emp ? of({ ...emp }) : throwError(() => new Error('Employee not found'));
    }
    return this.http.get<Employee>(`${this.baseUrl}/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  create(employee: Employee): Observable<Employee> {
    if (environment.useMock) {
      // Ensure unique ID
      const newId = employee.employeeId || Math.max(...this.mockEmployees.map(e => e.employeeId), 100) + 1;
      const newEmp = { ...employee, employeeId: newId };
      this.mockEmployees.push(newEmp);
      return of(newEmp);
    }
    return this.http.post<Employee>(this.baseUrl, employee).pipe(
      catchError(this.handleError)
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
