import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { UserRole } from '../auth/auth.service';

export interface DirectoryUser {
  userId: string;
  fullName: string;
  role: UserRole;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserDirectoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.learningApiUrl}/users`;

  getUsers(role?: UserRole): Observable<DirectoryUser[]> {
    const params = role
      ? new HttpParams().set('role', role)
      : undefined;

    return this.http.get<DirectoryUser[]>(this.apiUrl, { params });
  }

  getLearners(): Observable<DirectoryUser[]> {
    return this.getUsers('LEARNER');
  }
}
