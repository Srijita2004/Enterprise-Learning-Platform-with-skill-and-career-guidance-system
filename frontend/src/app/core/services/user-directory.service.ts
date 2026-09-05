import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

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

    return this.http.get<DirectoryUser[]>(this.apiUrl, { params }).pipe(
      catchError(() => of([])),
      map(serverUsers => {
        const merged = [...(serverUsers || [])];
        if (!role || role === 'LEARNER') {
          try {
            const dynamicLearners: DirectoryUser[] = JSON.parse(localStorage.getItem('ssn_registered_learners') || '[]');
            dynamicLearners.forEach(dl => {
              if (!merged.some(u => u.userId === dl.userId || (u.fullName && u.fullName.toLowerCase().trim() === dl.fullName.toLowerCase().trim()))) {
                merged.push(dl);
              }
            });
          } catch {}
        }
        return merged;
      })
    );
  }

  getLearners(): Observable<DirectoryUser[]> {
    return this.getUsers('LEARNER');
  }
}
