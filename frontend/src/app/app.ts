import { Component, inject, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { NgClass } from '@angular/common';
import { ToastService } from './shared/services/toast.service';
import { ConfirmService } from './shared/services/confirm.service';
import { AuthService } from './core/auth/auth.service';
import { ThemeService } from './core/theme/theme.service';
import { NotificationBellComponent } from './core/services/notification-bell.compents';
import { AiAssistantComponent } from './shared/components/ai-assistant/ai-assistant';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NgClass, NotificationBellComponent, AiAssistantComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly toastService = inject(ToastService);
  readonly confirmService = inject(ConfirmService);
  readonly authService = inject(AuthService);
  readonly themeService = inject(ThemeService);

  private readonly router = inject(Router);

  readonly sidebarOpen = signal(false);

  toggleTheme(): void {
    this.themeService.toggle();
    this.toastService.showInfo(
      `Switched to ${this.themeService.isDarkTheme() ? 'Dark' : 'Light'} theme`
    );
  }

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
    this.closeSidebar();
    this.router.navigateByUrl('/login');
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase() ?? '')
      .join('') || 'U';
  }
}
