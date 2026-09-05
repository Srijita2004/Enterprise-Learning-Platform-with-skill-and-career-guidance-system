import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Employee } from '../../models/certification.models';
import { EmployeeService } from '../services/employee.service';

@Injectable({ providedIn: 'root' })
export class EmployeeDirectoryService {
  private readonly empService = inject(EmployeeService);

  employees(): Observable<Employee[]> { 
    return this.empService.getAll().pipe(
      map(list => list.map(e => ({ employeeId: e.employeeId, employeeName: e.employeeName, designation: e.designation } as Employee)))
    );
  }
}
