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
  targetRole: string; // 'ALL' | 'LEARNER' | 'HR' | 'ADMIN' | 'EMPLOYEE'
  recipientEmployeeId?: number;
  recipientLearnerId?: string;
  recipientUserId?: string;
  recipientName?: string;
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

  private readonly defaultRoleNotifications: Record<string, AppNotification[]> = {
    LEARNER: [],
    EMPLOYEE: [],
    HR: [
      {
        id: 'notif-hr-1',
        title: 'Payment Verification Pending',
        message: 'Learner Alex Vance submitted wire transfer transaction ref #TXN-98421.',
        type: 'WARNING',
        targetRole: 'HR',
        time: '5m ago',
        read: false
      },
      {
        id: 'notif-hr-2',
        title: 'Candidate Promotion Nomination',
        message: 'Srijita was nominated for Lead Java & Cloud Solutions Architect candidate review.',
        type: 'SUCCESS',
        targetRole: 'HR',
        time: '45m ago',
        read: false
      }
    ],
    ADMIN: [
      {
        id: 'notif-adm-1',
        title: 'Enterprise Compliance Benchmark',
        message: '98% workforce skill coverage achieved across primary architecture divisions.',
        type: 'SUCCESS',
        targetRole: 'ADMIN',
        time: '12m ago',
        read: false
      },
      {
        id: 'notif-adm-2',
        title: 'Microservices Health Monitor',
        message: 'All 5 backend services (M1-M4 & Notification Engine) report healthy status.',
        type: 'INFO',
        targetRole: 'ADMIN',
        time: '1h ago',
        read: false
      }
    ]
  };

  private getDynamicNotifications(role: string): AppNotification[] {
    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      const user = this.auth.currentUser();
      const currentEmpId = this.auth.getEmployeeId();
      const currentUserId = user?.userId || '';
      const currentUserName = (user?.name || '').toLowerCase().trim();

      return stored.filter(n => {
        if (role === 'ADMIN') return true;
        if (role === 'HR') return n.targetRole === 'HR' || n.targetRole === 'HR_MANAGER' || n.targetRole === 'ALL';

        if (role === 'EMPLOYEE') {
          if (n.targetRole !== 'EMPLOYEE' && n.targetRole !== 'ALL') return false;

          // STRICT RECIPIENT ISOLATION FOR EMPLOYEES
          if (n.recipientEmployeeId !== undefined && n.recipientEmployeeId !== null) {
            return n.recipientEmployeeId === currentEmpId;
          }
          if (n.recipientUserId && currentUserId) {
            return n.recipientUserId === currentUserId;
          }
          if (n.recipientName) {
            const rec = n.recipientName.toLowerCase().trim();
            return rec === currentUserName || currentUserName.includes(rec) || rec.includes(currentUserName);
          }
          // If no specific recipient is tagged, check if message is personalized to another employee
          const allOtherDemos = ['srijita', 'alex vance', 'marcus brodie', 'sarah jenkins', 'john smith', 'jane doe', 'david miller', 'elena rostova', 'michael chang', 'priya sharma', 'tani', 'disha'];
          const text = `${n.title} ${n.message}`.toLowerCase();
          const mentionsOther = allOtherDemos.some(d => text.includes(d) && !currentUserName.includes(d));
          if (mentionsOther) return false;

          return false;
        }

        if (role === 'LEARNER') {
          if (n.targetRole !== 'LEARNER' && n.targetRole !== 'ALL') return false;

          // STRICT RECIPIENT ISOLATION FOR LEARNERS
          if (n.recipientLearnerId && currentUserId && n.recipientLearnerId === currentUserId) {
            return true;
          }
          if (n.recipientUserId && currentUserId && n.recipientUserId === currentUserId) {
            return true;
          }
          if (n.recipientName && currentUserName) {
            const rec = n.recipientName.toLowerCase().trim();
            if (rec === currentUserName || currentUserName.includes(rec) || rec.includes(currentUserName)) {
              return true;
            }
          }
          const text = `${n.title} ${n.message}`.toLowerCase();
          if (currentUserName && currentUserName.length >= 2 && text.includes(currentUserName)) {
            return true;
          }

          return false;
        }

        return false;
      });
    } catch {
      return [];
    }
  }

  public notifyNewEmployeeJoined(employeeName: string, email: string, role: string): void {
    const hrNotif: AppNotification = {
      id: 'notif-new-emp-hr-' + Date.now(),
      title: `New Employee Onboarding: ${employeeName}`,
      message: `${employeeName} (${email}) has registered as an ${role}. Please review profile to configure Career Objective Target & Learning Pathways.`,
      type: 'INFO',
      targetRole: 'HR',
      time: 'Just now',
      read: false
    };

    const adminNotif: AppNotification = {
      id: 'notif-new-emp-adm-' + (Date.now() + 1),
      title: `New Employee Registered: ${employeeName}`,
      message: `${employeeName} (${email}) has joined the platform as an ${role}. Set initial career objective and assign curriculum.`,
      type: 'INFO',
      targetRole: 'ADMIN',
      time: 'Just now',
      read: false
    };

    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      // Avoid duplicate notifications for same employee
      if (!stored.some(n => n.message.includes(employeeName))) {
        stored.unshift(hrNotif, adminNotif);
        localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(stored.slice(0, 30)));
      }
    } catch {}

    this.http.post(this.apiUrl, {
      title: `New Employee Onboarding: ${employeeName}`,
      message: `${employeeName} (${email}) has registered. Configure their career roadmap and courses.`,
      type: 'EMPLOYEE_CREATED',
      targetRole: 'HR_MANAGER'
    }).pipe(catchError(() => of(null))).subscribe();

    this.fetchNotifications();
  }

  public notifyExternalCertificateSubmitted(employeeName: string, certName: string, certId?: string, employeeId?: number): void {
    const hrNotif: AppNotification = {
      id: 'notif-cert-hr-' + Date.now(),
      title: `External Certificate Verification Request: ${certName}`,
      message: `${employeeName} submitted external certificate "${certName}" for review and compliance verification.`,
      type: 'WARNING',
      targetRole: 'HR',
      time: 'Just now',
      read: false
    };

    const adminNotif: AppNotification = {
      id: 'notif-cert-adm-' + (Date.now() + 1),
      title: `Certificate Verification Required: ${certName}`,
      message: `${employeeName} submitted external credential. Action required in Certification Hub.`,
      type: 'INFO',
      targetRole: 'ADMIN',
      time: 'Just now',
      read: false
    };

    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      stored.unshift(hrNotif, adminNotif);
      localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(stored.slice(0, 50)));
    } catch {}

    this.fetchNotifications();
  }

  public notifyCertificateVerified(employeeName: string, certName: string, status: string, employeeId?: number): void {
    const isApproved = status === 'VERIFIED';
    const empNotif: AppNotification = {
      id: 'notif-cert-emp-' + Date.now(),
      title: isApproved ? `Certificate Approved: ${certName}` : `Certificate Rejected: ${certName}`,
      message: isApproved 
        ? `Your external credential "${certName}" has been verified and approved by HR/Admin.` 
        : `Your external credential submission "${certName}" was reviewed and not approved.`,
      type: isApproved ? 'SUCCESS' : 'ALERT',
      targetRole: 'EMPLOYEE',
      recipientEmployeeId: employeeId,
      recipientName: employeeName,
      time: 'Just now',
      read: false
    };

    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      stored.unshift(empNotif);
      localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(stored.slice(0, 50)));
    } catch {}

    this.fetchNotifications();
  }

  public notifyLearningPathAssigned(learnerId: string, pathTitle: string, assignedBy: string, learnerName?: string): void {
    const notif: AppNotification = {
      id: 'notif-lp-assigned-' + Date.now(),
      title: `Learning Path Assigned: ${pathTitle}`,
      message: `You have been assigned the learning path "${pathTitle}" by ${assignedBy}. Start your learning journey.`,
      type: 'INFO',
      targetRole: 'LEARNER',
      recipientLearnerId: learnerId,
      recipientUserId: learnerId,
      recipientName: learnerName,
      time: 'Just now',
      read: false
    };

    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      stored.unshift(notif);
      localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(stored.slice(0, 50)));
    } catch {}

    this.fetchNotifications();
  }

  public notifyCertificateRenewal(employeeId: number, employeeName: string, certName: string, dueDate?: string, daysRemaining?: number): void {
    const isExpired = daysRemaining !== undefined && daysRemaining <= 0;
    const notif: AppNotification = {
      id: 'notif-cert-renew-' + Date.now() + '-' + employeeId,
      title: isExpired ? `Certificate Expired: ${certName}` : `Certificate Renewal Due: ${certName}`,
      message: isExpired
        ? `Your credential "${certName}" has expired. Please submit renewal verification in the Certification Hub.`
        : `Your credential "${certName}" is due for renewal${dueDate ? ' on ' + dueDate : ''}. Take action in the Certification Hub.`,
      type: isExpired ? 'ALERT' : 'WARNING',
      targetRole: 'EMPLOYEE',
      recipientEmployeeId: employeeId,
      recipientName: employeeName,
      time: 'Just now',
      read: false
    };

    try {
      const stored: AppNotification[] = JSON.parse(localStorage.getItem('ssn_dynamic_notifications') || '[]');
      if (!stored.some(n => n.recipientEmployeeId === employeeId && n.title === notif.title && !n.read)) {
        stored.unshift(notif);
        localStorage.setItem('ssn_dynamic_notifications', JSON.stringify(stored.slice(0, 50)));
      }
    } catch {}

    this.fetchNotifications();
  }

  public fetchNotifications(): void {
    const user = this.auth.currentUser();
    const role = user?.role || 'EMPLOYEE';
    const dynamicList = this.getDynamicNotifications(role);

    this.http.get<NotificationApiResponse[]>(this.apiUrl).pipe(
      catchError(() => of<NotificationApiResponse[]>([])),
      tap((data) => {
        const mapped = (data || [])
          .filter((n) => this.isVisibleToCurrentUser(n.targetRole, role, n))
          .map((n) => this.toAppNotification(n));

        if (role === 'EMPLOYEE') {
          // For Employee: strictly show ONLY notifications explicitly addressed to this employee. If none exist, return clean empty list []
          const allEmp = [...dynamicList, ...mapped];
          const seen = new Set<string>();
          const result: AppNotification[] = [];
          for (const item of allEmp) {
            if (!seen.has(item.id)) {
              seen.add(item.id);
              result.push(item);
            }
          }
          this.notificationsSubject.next(result);
          return;
        }

        if (role === 'LEARNER') {
          // For Learner: strictly show ONLY notifications explicitly addressed to this learner. If none exist, return clean empty list []
          const allLrn = [...dynamicList, ...mapped];
          const seen = new Set<string>();
          const result: AppNotification[] = [];
          for (const item of allLrn) {
            if (!seen.has(item.id)) {
              seen.add(item.id);
              result.push(item);
            }
          }
          this.notificationsSubject.next(result);
          return;
        }

        // For HR / ADMIN: show dynamic + mapped (or fallback if empty)
        const fallbackList = this.defaultRoleNotifications[role] || [];
        const merged = [...dynamicList, ...(mapped.length > 0 ? mapped : fallbackList)];
        const seen = new Set<string>();
        const result: AppNotification[] = [];
        for (const item of merged) {
          if (!seen.has(item.id)) {
            seen.add(item.id);
            result.push(item);
          }
        }
        this.notificationsSubject.next(result);
      })
    ).subscribe();
  }

  public markAsRead(id: string): void {
    const current = this.notificationsSubject.value;
    const target = current.find((n) => n.id === id);
    if (!target || target.read) {
      return;
    }
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

  private isVisibleToCurrentUser(targetRole: string, currentRole: UserRole | undefined, n?: NotificationApiResponse): boolean {
    if (!currentRole) return false;
    if (currentRole === 'ADMIN') return true;
    if (currentRole === 'HR') {
      return targetRole === 'HR' || targetRole === 'HR_MANAGER' || targetRole === 'ALL';
    }

    const user = this.auth.currentUser();
    const currentUserName = (user?.name || '').toLowerCase().trim();
    const currentUserId = user?.userId || '';

    if (currentRole === 'EMPLOYEE') {
      const appTargetRole = TARGET_ROLE_TO_APP_ROLE[targetRole] || targetRole;
      if (appTargetRole !== 'EMPLOYEE' && appTargetRole !== 'ALL') return false;

      const currentEmpId = this.auth.getEmployeeId();

      if (n) {
        if (n.referenceId && !isNaN(Number(n.referenceId))) {
          const refId = Number(n.referenceId);
          if (refId !== currentEmpId) return false;
        }
        const allOtherDemos = ['srijita', 'alex vance', 'marcus brodie', 'sarah jenkins', 'john smith', 'jane doe', 'david miller', 'elena rostova', 'michael chang', 'priya sharma', 'tani', 'disha'];
        const text = `${n.title || ''} ${n.message || ''}`.toLowerCase();
        const mentionsOther = allOtherDemos.some(d => text.includes(d) && !currentUserName.includes(d));
        if (mentionsOther) return false;

        // If backend notification targetRole is generic EMPLOYEE, require either referenceId match or current user name match
        if (appTargetRole === 'EMPLOYEE') {
          const matchesName = currentUserName && text.includes(currentUserName);
          const matchesId = n.referenceId && Number(n.referenceId) === currentEmpId;
          if (!matchesName && !matchesId) {
            return false;
          }
        }
      }

      return true;
    }

    if (currentRole === 'LEARNER') {
      const appTargetRole = TARGET_ROLE_TO_APP_ROLE[targetRole] || targetRole;
      if (appTargetRole !== 'LEARNER' && appTargetRole !== 'ALL') return false;

      if (n) {
        if (n.referenceId && currentUserId) {
          if (n.referenceId !== currentUserId) return false;
        }
        const allOtherDemos = ['srijita', 'alex vance', 'marcus brodie', 'sarah jenkins', 'john smith', 'jane doe', 'david miller', 'elena rostova', 'michael chang', 'priya sharma', 'tani', 'disha'];
        const text = `${n.title || ''} ${n.message || ''}`.toLowerCase();
        const mentionsOther = allOtherDemos.some(d => text.includes(d) && !currentUserName.includes(d));
        if (mentionsOther) return false;

        if (appTargetRole === 'LEARNER') {
          const matchesName = currentUserName && currentUserName.length >= 3 && text.includes(currentUserName);
          const matchesId = n.referenceId && n.referenceId === currentUserId;
          if (!matchesName && !matchesId) {
            return false;
          }
        }
      }

      return true;
    }

    return false;
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