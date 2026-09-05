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

  /**
   * Holds the active authenticated user for THIS specific browser tab/window.
   * Backed by isolated per-tab sessionStorage so multiple accounts (HR, Employee, Learner, Admin)
   * can run simultaneously in separate tabs without session collision.
   */
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

    const fallbackUser: BackendUser = {
      userId: preferredUserId || `usr-${Date.now()}`,
      fullName,
      email: normalizedEmail,
      role,
      token: `mock-jwt-token-${Date.now()}`,
      active: true
    };

    return this.http.post<BackendUser>(`${this.authUrl}/login`, {
      fullName,
      email: normalizedEmail,
      password,
      role,
      preferredUserId
    }).pipe(
      catchError(() => of(fallbackUser)),
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
                const nextId = this.resolveEmployeeId(response.fullName, response.email);
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

                // Immediately persist into shared dynamic store
                try {
                  const empList = JSON.parse(localStorage.getItem('ssn_registered_employees') || '[]');
                  if (!empList.some((e: any) => e.employeeId === nextId || (e.employeeName && e.employeeName.toLowerCase().trim() === response.fullName.toLowerCase().trim()))) {
                    empList.push(newEmpPayload);
                    localStorage.setItem('ssn_registered_employees', JSON.stringify(empList));
                  }
                } catch {}

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
        // Store session exclusively in tab-isolated sessionStorage
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
        this.currentUser.set(user);

        // Save registered employee into shared registry for HR & Admin visibility
        if (user.role === 'EMPLOYEE' && user.employeeId) {
          try {
            const empList = JSON.parse(localStorage.getItem('ssn_registered_employees') || '[]');
            const existingIdx = empList.findIndex((e: any) => e.employeeId === user.employeeId || (e.employeeName && e.employeeName.toLowerCase().trim() === user.name.toLowerCase().trim()));
            const empRecord = {
              employeeId: user.employeeId,
              employeeName: user.name,
              designation: 'Enterprise Associate',
              salary: 85000
            };
            if (existingIdx >= 0) {
              empList[existingIdx] = empRecord;
            } else {
              empList.push(empRecord);
            }
            localStorage.setItem('ssn_registered_employees', JSON.stringify(empList));
          } catch {}
        }

        // Save registered learner into shared registry for HR & Admin path assignment
        if (user.role === 'LEARNER') {
          try {
            const lrnList = JSON.parse(localStorage.getItem('ssn_registered_learners') || '[]');
            const existingIdx = lrnList.findIndex((l: any) => l.userId === user.userId || (l.fullName && l.fullName.toLowerCase().trim() === user.name.toLowerCase().trim()));
            const lrnRecord = {
              userId: user.userId,
              fullName: user.name,
              email: user.email,
              role: 'LEARNER',
              active: true
            };
            if (existingIdx >= 0) {
              lrnList[existingIdx] = lrnRecord;
            } else {
              lrnList.push(lrnRecord);
            }
            localStorage.setItem('ssn_registered_learners', JSON.stringify(lrnList));
          } catch {}
        }

        // Inform HR and Admin when an employee or learner joins
        if (user.role === 'EMPLOYEE' || user.role === 'LEARNER') {
          try {
            const notifKey = `ssn_notified_${user.name.toLowerCase()}`;
            if (!localStorage.getItem(notifKey)) {
              localStorage.setItem(notifKey, 'true');
              const notifList = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
              notifList.unshift(
                {
                  id: 'notif-hr-' + Date.now(),
                  title: `New Employee Onboarding: ${user.name}`,
                  message: `${user.name} (${user.email}) has registered. Please review profile to configure Career Objective Target & Learning Pathways.`,
                  type: 'INFO',
                  targetRole: 'HR',
                  time: 'Just now',
                  read: false
                },
                {
                  id: 'notif-adm-' + (Date.now() + 1),
                  title: `New Employee Registered: ${user.name}`,
                  message: `${user.name} (${user.email}) has joined the platform as an ${user.role}. Set initial career objective and assign curriculum.`,
                  type: 'INFO',
                  targetRole: 'ADMIN',
                  time: 'Just now',
                  read: false
                }
              );
              localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(notifList.slice(0, 30)));
            }
          } catch {}
        }
      })
    );
  }

  logout(): void {
    // Clear only this specific tab's session
    sessionStorage.removeItem(SESSION_KEY);
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
    return this.resolveEmployeeId(user.name, user.email, user.employeeId);
  }

  private resolveEmployeeId(name: string, email: string, existingId?: number): number {
    const lowerName = (name || '').toLowerCase().trim();
    const lowerEmail = (email || '').toLowerCase().trim();

    if (lowerName === 'srijita' || lowerEmail === 'srijita@example.com' || lowerEmail === 'srijita@skillsphere.com') return 1;
    if ((lowerName.includes('alex') && lowerName.includes('vance')) || lowerEmail.includes('alex.vance')) return 101;
    if ((lowerName.includes('marcus') && lowerName.includes('brodie')) || lowerEmail.includes('marcus.brodie')) return 102;
    if ((lowerName.includes('sarah') && lowerName.includes('jenkins')) || lowerEmail.includes('sarah.jenkins')) return 103;
    if ((lowerName.includes('john') && lowerName.includes('smith')) || lowerEmail.includes('john.smith')) return 106;
    if ((lowerName.includes('jane') && lowerName.includes('doe')) || lowerEmail.includes('jane.doe')) return 107;
    if ((lowerName.includes('david') && lowerName.includes('miller')) || lowerEmail.includes('david.miller')) return 108;
    if ((lowerName.includes('elena') && lowerName.includes('rostova')) || lowerEmail.includes('elena.rostova')) return 109;
    if ((lowerName.includes('michael') && lowerName.includes('chang')) || lowerEmail.includes('michael.chang')) return 110;
    if ((lowerName.includes('priya') && lowerName.includes('sharma')) || lowerEmail.includes('priya.sharma')) return 111;

    // For any custom user (like "Tani"):
    if (existingId && existingId >= 200) return existingId;

    try {
      const dynamicList: any[] = JSON.parse(localStorage.getItem('ssn_registered_employees') || '[]');
      const match = dynamicList.find(e => (e.employeeName || '').toLowerCase().trim() === lowerName);
      if (match && match.employeeId && match.employeeId >= 200) return match.employeeId;
    } catch {}

    let hash = 0;
    for (let i = 0; i < lowerName.length; i++) {
      hash = ((hash << 5) - hash) + lowerName.charCodeAt(i);
      hash |= 0;
    }
    return 201 + Math.abs(hash % 799);
  }

  /**
   * Reads the active user from tab-isolated sessionStorage.
   * If not found, gracefully checks localStorage as an initial fallback.
   */
  private loadStoredSession(): AuthUser | null {
    try {
      const rawSession = sessionStorage.getItem(SESSION_KEY);
      if (rawSession) {
        const user = JSON.parse(rawSession) as AuthUser;
        if (user && user.userId && user.name && user.email && user.role) {
          if (user.role === 'EMPLOYEE') {
            user.employeeId = this.resolveEmployeeId(user.name, user.email, user.employeeId);
            sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
          }
          return user;
        }
      }

      // Initial migration / fallback check
      const rawLocal = localStorage.getItem(SESSION_KEY);
      if (rawLocal) {
        const user = JSON.parse(rawLocal) as AuthUser;
        if (user && user.userId && user.name && user.email && user.role) {
          if (user.role === 'EMPLOYEE') {
            user.employeeId = this.resolveEmployeeId(user.name, user.email, user.employeeId);
          }
          sessionStorage.setItem(SESSION_KEY, JSON.stringify(user));
          return user;
        }
      }
      return null;
    } catch {
      sessionStorage.removeItem(SESSION_KEY);
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
