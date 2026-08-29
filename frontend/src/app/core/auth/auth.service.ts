import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';

import { environment } from '../../../environments/environment';

export type UserRole = 'ADMIN' | 'HR' | 'LEARNER' | 'EMPLOYEE';

export interface AuthUser {
  userId: string;
  name: string;
  email: string;
  role: UserRole;
  token?: string;
  employeeId?: number;
}

interface BackendUser {
  userId: string;
  fullName: string;
  email: string;
  role: UserRole;
  token?: string;
  active: boolean;
}

interface LegacyStoredAccount {
  userId: string;
  name: string;
  role: UserRole;
}

const SESSION_KEY = 'ssn_auth_user';
const LEGACY_ACCOUNTS_KEY = 'ssn_registered_accounts';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = `${environment.learningApiUrl}/auth`;

  readonly currentUser = signal<AuthUser | null>(this.loadStoredSession());

  login(
    name: string,
    email: string,
    password: string,
    role: UserRole
  ): Observable<AuthUser> {
    const fullName = this.normalizeName(name);
    const normalizedEmail = email.trim().toLowerCase();
    const preferredUserId = this.findLegacyUserId(fullName, role);

    return this.http.post<BackendUser>(`${this.authUrl}/login`, {
      fullName,
      email: normalizedEmail,
      password,
      role,
      preferredUserId
    }).pipe(
      switchMap((response): Observable<AuthUser> => {
        if (response.role === 'EMPLOYEE') {
          return this.http.get<any[]>(`${environment.apiUrl}/employee`, {
            headers: { 'X-User-Role': 'EMPLOYEE', 'X-User-Id': response.userId }
          }).pipe(
            catchError(() => of([])),
            switchMap((allEmployees) => {
              const uName = response.fullName.toLowerCase().trim();
              const uEmail = response.email.toLowerCase().trim();
              const match = (allEmployees || []).find(e => {
                const eName = (e.employeeName || '').toLowerCase().trim();
                return (uName.length >= 2 && eName === uName) ||
                       (uEmail.length >= 2 && eName.includes(uEmail.split('@')[0]));
              });

              if (match) {
                return of({
                  userId: response.userId,
                  name: response.fullName,
                  email: response.email,
                  role: response.role,
                  token: response.token,
                  employeeId: match.employeeId
                });
              } else {
                const existingIds = (allEmployees || []).map(e => e.employeeId || 0);
                const nextId = existingIds.length > 0 ? Math.max(...existingIds, 100) + 1 : 101;
                const newEmpPayload = {
                  employeeId: nextId,
                  employeeName: response.fullName,
                  designation: 'Enterprise Associate',
                  salary: 85000
                };

                const reqHeaders: Record<string, string> = {
                  'X-User-Role': 'EMPLOYEE',
                  'X-User-Id': response.userId
                };
                if (response.token) {
                  reqHeaders['Authorization'] = `Bearer ${response.token}`;
                }

                return this.http.post<any>(`${environment.apiUrl}/employee`, newEmpPayload, {
                  headers: reqHeaders
                }).pipe(
                  catchError(() => of(newEmpPayload)),
                  map((createdEmp) => ({
                    userId: response.userId,
                    name: response.fullName,
                    email: response.email,
                    role: response.role,
                    token: response.token,
                    employeeId: createdEmp.employeeId || nextId
                  }))
                );
              }
            })
          );
        } else {
          const empId = this.resolveEmployeeId(response.fullName, response.email);
          return of({
            userId: response.userId,
            name: response.fullName,
            email: response.email,
            role: response.role,
            token: response.token,
            employeeId: empId
          });
        }
      }),
      tap((user) => {
        localStorage.setItem(SESSION_KEY, JSON.stringify(user));
        this.currentUser.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    this.currentUser.set(null);
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  getToken(): string | undefined {
    return this.currentUser()?.token;
  }

  hasRole(...roles: UserRole[]): boolean {
    const user = this.currentUser();
    return !!user && roles.includes(user.role);
  }

  getLearnerId(): string {
    return this.currentUser()?.userId ?? '';
  }

  getCurrentUserId(): string {
    return this.currentUser()?.userId ?? '';
  }

  getEmployeeId(): number {
    const user = this.currentUser();
    if (!user || user.role === 'LEARNER') return 0;
    if (user.employeeId) return user.employeeId;
    return this.resolveEmployeeId(user.name, user.email);
  }

  private resolveEmployeeId(name: string, email: string): number {
    const lowerName = (name || '').toLowerCase();
    const lowerEmail = (email || '').toLowerCase();

    if (lowerName.includes('srijita') || lowerEmail.includes('srijita')) return 1;
    if (lowerName.includes('john') || lowerName.includes('smith') || lowerEmail.includes('john')) return 106;
    if (lowerName.includes('alex') || lowerName.includes('vance') || lowerEmail.includes('alex')) return 101;
    if (lowerName.includes('sarah') || lowerName.includes('jenkins') || lowerEmail.includes('sarah')) return 103;
    if (lowerName.includes('employee') || lowerEmail.includes('employee')) return 107;
    return 0;
  }

  private loadStoredSession(): AuthUser | null {
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      if (!raw) {
        return null;
      }

      const user = JSON.parse(raw) as AuthUser;
      if (!user.userId || !user.name || !user.email || !user.role) {
        localStorage.removeItem(SESSION_KEY);
        return null;
      }
      return user;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private findLegacyUserId(name: string, role: UserRole): string | null {
    try {
      const raw = localStorage.getItem(LEGACY_ACCOUNTS_KEY);
      if (!raw) {
        return null;
      }

      const accounts = JSON.parse(raw) as Record<string, LegacyStoredAccount>;
      const key = `${role}:${name.toLowerCase()}`;
      return accounts[key]?.userId ?? null;
    } catch {
      return null;
    }
  }

  private normalizeName(name: string): string {
    return name.trim().replace(/\s+/g, ' ');
  }
}
