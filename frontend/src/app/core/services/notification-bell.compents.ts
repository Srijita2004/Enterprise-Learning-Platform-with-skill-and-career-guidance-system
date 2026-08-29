import { Component, OnDestroy, OnInit, effect, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';

import {
  NotificationService,
  AppNotification
} from './notification.service';
import { AuthService } from '../auth/auth.service';

const REFRESH_INTERVAL_MS = 30000;

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],

  template: `
    <div class="notification-host">

      <!-- Bell Button -->
      <button
        type="button"
        class="notification-bell-button"
        (click)="toggleDrawer()"
        title="Notifications"
        aria-label="Notifications">

        <svg
          class="notification-bell-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round">

          <path
            d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9">
          </path>

          <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>

        </svg>

        @if (unreadCount > 0) {
          <span class="notification-badge">
            {{ unreadCount > 99 ? '99+' : unreadCount }}
          </span>
        }

      </button>

      @if (isOpen) {
        <div
          class="notification-backdrop"
          (click)="closeDrawer()">
        </div>
      }

      <aside
        class="notification-drawer"
        [class.open]="isOpen"
        aria-label="Notifications">

        <div class="notification-header">

          <div>
            <div class="notification-title">
              Notifications
            </div>

            <div class="notification-subtitle">
              {{ unreadCount }} unread
            </div>
          </div>

          <div class="notification-header-actions">

            @if (unreadCount > 0) {
              <button
                type="button"
                class="mark-all-button"
                (click)="markAllRead()">
                Mark all read
              </button>
            }

            <button
              type="button"
              class="notification-close-button"
              (click)="closeDrawer()"
              aria-label="Close notifications">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>

          </div>

        </div>

        <div class="notification-list">

          @if (notifications.length === 0) {

            <div class="notification-empty">
              <svg
                width="32"
                height="32"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5">
                <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"></path>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
              </svg>

              <p>No notifications available.</p>
            </div>

          } @else {

            @for (item of notifications; track item.id) {

              <div
                class="notification-item"
                [class.unread]="!item.read"
                (click)="markRead(item)">

                <span
                  class="notification-dot"
                  [class.success]="item.type === 'SUCCESS'"
                  [class.warning]="item.type === 'WARNING'"
                  [class.alert]="item.type === 'ALERT'"
                  [class.info]="item.type === 'INFO'">
                </span>

                <div class="notification-content">

                  <div class="notification-row">

                    <strong>
                      {{ item.title }}
                    </strong>

                    <span>
                      {{ item.time }}
                    </span>

                  </div>

                  <p>
                    {{ item.message }}
                  </p>

                  <small>
                    {{ item.targetRole }}
                  </small>

                </div>

              </div>

            }

          }

        </div>

      </aside>

    </div>
  `,

  styles: [`

    :host {
      display: inline-flex;
      align-items: center;
      position: relative;
      z-index: 1100;
    }

    .notification-host {
      position: relative;
      display: inline-flex;
      align-items: center;
    }

    .notification-bell-button {
      position: relative;
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;

      border: 1px solid var(--border-color);
      border-radius: 11px;

      background: var(--surface-soft);
      color: var(--text-primary);

      cursor: pointer;

      transition:
        color 0.2s ease,
        background 0.2s ease,
        border-color 0.2s ease,
        transform 0.2s ease;
    }

    .notification-bell-button:hover {
      color: var(--accent-primary);
      background: var(--accent-primary-glow);
      border-color: var(--accent-primary);
      transform: translateY(-1px);
    }

    .notification-bell-icon {
      width: 20px;
      height: 20px;
      display: block;
    }

    .notification-badge {
      position: absolute;
      top: -5px;
      right: -5px;

      min-width: 17px;
      height: 17px;
      padding: 0 4px;

      display: flex;
      align-items: center;
      justify-content: center;

      border-radius: 999px;

      background: #ef4444;
      color: white;

      font-size: 9px;
      font-weight: 800;

      border: 2px solid var(--surface-soft);
      line-height: 1;
    }

    .notification-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, 0.45);
      z-index: 2000;
    }

    .notification-drawer {
      position: fixed;
      top: 0;
      right: 0;

      width: min(380px, 92vw);
      height: 100vh;

      display: flex;
      flex-direction: column;

      background: var(--bg-card, #0f172a);
      color: var(--text-primary, #f8fafc);

      border-left: 1px solid var(--border-color, rgba(255, 255, 255, 0.12));

      box-shadow: -10px 0 35px rgba(0, 0, 0, 0.35);

      transform: translateX(100%);
      transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);

      z-index: 2001;
    }

    .notification-drawer.open {
      transform: translateX(0);
    }

    .notification-header {
      min-height: 70px;
      padding: 16px 20px;

      display: flex;
      align-items: center;
      justify-content: space-between;

      border-bottom: 1px solid var(--border-color, rgba(255, 255, 255, 0.12));
      background: var(--bg-card, #0f172a);
    }

    .notification-title {
      font-size: 16px;
      font-weight: 800;
      color: var(--text-primary, #f8fafc);
    }

    .notification-subtitle {
      margin-top: 3px;
      font-size: 12px;
      font-weight: 600;
      color: var(--accent-primary, #38bdf8);
    }

    .notification-header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .mark-all-button {
      border: 1px solid var(--border-color, rgba(255, 255, 255, 0.15));
      background: var(--surface-soft, rgba(255, 255, 255, 0.05));
      color: var(--accent-primary, #38bdf8);
      font-size: 11px;
      font-weight: 700;
      padding: 4px 10px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .mark-all-button:hover {
      background: var(--accent-primary-glow, rgba(56, 189, 248, 0.15));
    }

    .notification-close-button {
      width: 32px;
      height: 32px;

      display: flex;
      align-items: center;
      justify-content: center;

      border: 1px solid var(--border-color, rgba(255, 255, 255, 0.15));
      border-radius: 8px;

      background: var(--surface-soft, rgba(255, 255, 255, 0.05));
      color: var(--text-primary, #f8fafc);

      font-size: 20px;
      line-height: 1;

      cursor: pointer;
      transition: all 0.2s ease;
    }

    .notification-close-button:hover {
      background: rgba(239, 68, 68, 0.15);
      color: #ef4444;
      border-color: #ef4444;
    }

    .notification-list {
      flex: 1;
      overflow-y: auto;
      background: var(--bg-card, #0f172a);
    }

    .notification-empty {
      min-height: 250px;

      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;

      gap: 10px;

      color: var(--text-muted, #94a3b8);
      font-size: 13px;
      font-weight: 500;
    }

    .notification-item {
      display: flex;
      gap: 12px;

      padding: 16px 20px;

      border-bottom: 1px solid var(--border-color, rgba(255, 255, 255, 0.08));

      cursor: pointer;
      transition: background 0.15s ease;
    }

    .notification-item:hover {
      background: var(--accent-primary-glow, rgba(56, 189, 248, 0.08));
    }

    .notification-item.unread {
      background: rgba(56, 189, 248, 0.06);
    }

    .notification-dot {
      width: 10px;
      height: 10px;
      min-width: 10px;
      margin-top: 5px;
      border-radius: 50%;
      box-shadow: 0 0 8px currentColor;
    }

    .notification-dot.success {
      background: #10b981;
      color: #10b981;
    }

    .notification-dot.warning {
      background: #f59e0b;
      color: #f59e0b;
    }

    .notification-dot.alert {
      background: #ef4444;
      color: #ef4444;
    }

    .notification-dot.info {
      background: #0284c7;
      color: #0284c7;
    }

    .notification-content {
      min-width: 0;
      flex: 1;
    }

    .notification-row {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 10px;
    }

    .notification-row strong {
      font-size: 13px;
      font-weight: 700;
      color: var(--text-primary, #f8fafc);
    }

    .notification-row span {
      flex-shrink: 0;
      font-size: 10px;
      font-weight: 600;
      color: var(--text-muted, #94a3b8);
    }

    .notification-content p {
      margin: 6px 0 8px;

      font-size: 12px;
      line-height: 1.5;
      color: var(--text-secondary, #cbd5e1);
      font-weight: 500;
    }

    .notification-content small {
      display: inline-block;
      font-size: 9px;
      font-weight: 800;
      padding: 2px 8px;
      border-radius: 999px;
      background: var(--surface-soft, rgba(255, 255, 255, 0.08));
      color: var(--accent-primary, #38bdf8);
      border: 1px solid var(--border-color, rgba(255, 255, 255, 0.1));
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }

  `]
})
export class NotificationBellComponent implements OnInit, OnDestroy {

  isOpen = false;

  notifications: AppNotification[] = [];

  private refreshHandle: ReturnType<typeof setInterval> | null = null;
  private subscription?: Subscription;

  private readonly auth = inject(AuthService);

  constructor(
    private readonly service: NotificationService
  ) {
    effect(() => {
      // Whenever currentUser changes (e.g. login, logout, switch account), refresh notifications
      const user = this.auth.currentUser();
      if (user) {
        this.service.fetchNotifications();
      }
    });
  }

  ngOnInit(): void {

    this.subscription = this.service.notifications$.subscribe(
      data => {
        this.notifications = data;
      }
    );

    this.service.fetchNotifications();

    this.refreshHandle = setInterval(
      () => this.service.fetchNotifications(),
      REFRESH_INTERVAL_MS
    );
  }

  ngOnDestroy(): void {

    this.subscription?.unsubscribe();

    if (this.refreshHandle) {
      clearInterval(this.refreshHandle);
    }
  }

  get unreadCount(): number {
    return this.notifications.filter(
      notification => !notification.read
    ).length;
  }

  toggleDrawer(): void {
    this.isOpen = !this.isOpen;
  }

  closeDrawer(): void {
    this.isOpen = false;
  }

  markRead(item: AppNotification): void {
    if (!item.read) {
      this.service.markAsRead(item.id);
    }
  }

  markAllRead(): void {
    this.service.markAllAsRead();
  }
}