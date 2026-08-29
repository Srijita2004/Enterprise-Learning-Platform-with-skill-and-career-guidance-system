import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employee } from '../../models/certification.models';

@Injectable({ providedIn: 'root' })
export class EmployeeDirectoryService {
  private readonly http = inject(HttpClient);
  employees(): Observable<Employee[]> { return this.http.get<Employee[]>(`${environment.m1ApiUrl}/employee`); }
}
