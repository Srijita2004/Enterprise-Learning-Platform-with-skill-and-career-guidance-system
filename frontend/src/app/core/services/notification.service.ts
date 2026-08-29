import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { AuthService, UserRole } from '../auth/auth.service';

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: 'SUCCESS' | 'WARNING' | 'INFO' | 'ALERT';
  targetRole: string; // 'ALL' | 'LEARNER' | 'HR' | 'ADMIN'
  time: string;
  read: boolean;
}

/** Raw shape returned by GET /notification-api/api/notifications. */
interface NotificationApiResponse {
  notificationId: string;
  type: string;
  title: string;
  message: string;
  sourceService: string | null;
  referenceId: string | null;
  targetRole: 'ADMINISTRATOR' | 'HR_MANAGER' | 'EMPLOYEE' | 'LEARNER' | 'ALL';
  isRead: boolean;
  createdAt: string;
  readAt: string | null;
}

/** The backend's TargetRole enum uses different labels than the app's UserRole. */
const TARGET_ROLE_TO_APP_ROLE: Record<string, string> = {
  ADMINISTRATOR: 'ADMIN',
  HR_MANAGER: 'HR',
  EMPLOYEE: 'EMPLOYEE',
  LEARNER: 'LEARNER',
  ALL: 'ALL'
};

/** Maps the backend notification "type" enum to a badge color category. */
const TYPE_TO_BADGE: Record<string, AppNotification['type']> = {
  EMPLOYEE_CREATED: 'INFO',
  SKILL_UPDATED: 'INFO',
  COURSE_COMPLETED: 'SUCCESS',
  CERTIFICATE_ISSUED: 'SUCCESS',
  CERTIFICATE_RENEWED: 'SUCCESS',
  HR_ANNOUNCEMENT: 'WARNING',
  COMPLIANCE_ALERT: 'ALERT'
};

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  private readonly apiUrl = '/notification-api/api/notifications';

  private notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();

  public fetchNotifications(): void {
    this.http.get<NotificationApiResponse[]>(this.apiUrl).pipe(
      catchError(() => of<NotificationApiResponse[]>([])),
      tap((data) => {
        const role = this.auth.currentUser()?.role;
        const mapped = data
          .filter((n) => this.isVisibleToCurrentUser(n.targetRole, role))
          .map((n) => this.toAppNotification(n));
        this.notificationsSubject.next(mapped);
      })
    ).subscribe();
  }

  public markAsRead(id: string): void {
    const current = this.notificationsSubject.value;
    const target = current.find((n) => n.id === id);
    if (!target || target.read) {
      return;
    }
    // Optimistic update so the bell feels instant, reconciled by the refetch below.
    this.notificationsSubject.next(
      current.map((n) => (n.id === id ? { ...n, read: true } : n))
    );
    this.http.patch(`${this.apiUrl}/${id}/read`, {}).pipe(
      catchError(() => of(null)),
      tap(() => this.fetchNotifications())
    ).subscribe();
  }

  public markAllAsRead(): void {
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next(current.map((n) => ({ ...n, read: true })));
    this.http.patch(`${this.apiUrl}/read-all`, {}).pipe(
      catchError(() => of(null)),
      tap(() => this.fetchNotifications())
    ).subscribe();
  }

  private isVisibleToCurrentUser(targetRole: string, currentRole: UserRole | undefined): boolean {
    if (targetRole === 'ALL' || !currentRole || currentRole === 'ADMIN') {
      return true;
    }
    if (currentRole === 'HR' && (targetRole === 'HR_MANAGER' || targetRole === 'EMPLOYEE')) {
      return true;
    }
    return TARGET_ROLE_TO_APP_ROLE[targetRole] === currentRole;
  }

  private toAppNotification(n: NotificationApiResponse): AppNotification {
    return {
      id: n.notificationId,
      title: n.title,
      message: n.message,
      type: TYPE_TO_BADGE[n.type] ?? 'INFO',
      targetRole: TARGET_ROLE_TO_APP_ROLE[n.targetRole] ?? 'ALL',
      time: this.formatRelativeTime(n.createdAt),
      read: n.isRead
    };
  }

  private formatRelativeTime(iso: string): string {
    const date = new Date(iso);
    const diffMs = Date.now() - date.getTime();
    const diffMinutes = Math.floor(diffMs / 60000);
    if (diffMinutes < 1) return 'just now';
    if (diffMinutes < 60) return `${diffMinutes}m ago`;
    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) return `${diffHours}h ago`;
    const diffDays = Math.floor(diffHours / 24);
    return `${diffDays}d ago`;
  }
}