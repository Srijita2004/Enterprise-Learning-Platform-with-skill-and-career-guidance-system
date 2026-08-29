import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService, UserRole } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly themeService = inject(ThemeService);

  fullName = '';
  email = '';
  password = '';
  role: UserRole = 'EMPLOYEE';
  showPassword = false;
  errorMessage = '';
  isSigningIn = false;

  // Signal controlling floating navbar visibility
  readonly showFloatingNav = signal(true);

  readonly roles: { value: UserRole; label: string; shortLabel: string; description: string }[] = [
    {
      value: 'HR',
      label: 'HR',
      shortLabel: 'HR',
      description: 'People, development plans, and talent verification'
    },
    {
      value: 'EMPLOYEE',
      label: 'Employee',
      shortLabel: 'Employee',
      description: 'Connected 360 profile, skills, career roadmap & certs'
    },
    {
      value: 'ADMIN',
      label: 'Administrator',
      shortLabel: 'Admin',
      description: 'Courses, content, and platform operations'
    },
    {
      value: 'LEARNER',
      label: 'Learner',
      shortLabel: 'Learner',
      description: 'Open learning catalog, paths, and achievements'
    }
  ];



  @HostListener('window:scroll', [])
  onWindowScroll(): void {
    const scrollPos = window.scrollY || document.documentElement.scrollTop || 0;
    // Hides pill navbar when scrolled down past 120px
    this.showFloatingNav.set(scrollPos < 120);
  }

  scrollToLogin(): void {
    this.showFloatingNav.set(false);
    const loginElement = document.getElementById('login-section');
    if (loginElement) {
      loginElement.scrollIntoView({ behavior: 'smooth' });
    }
  }

  submit(): void {
    if (this.isSigningIn) {
      return;
    }

    const name = this.fullName.trim();
    const email = this.email.trim().toLowerCase();

    if (name.length < 2) {
      this.errorMessage = 'Enter your full name.';
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      this.errorMessage = 'Enter a valid email address.';
      return;
    }

    if (this.role === 'LEARNER') {
      if (!this.password || this.password.trim().length === 0) {
        this.errorMessage = 'Please enter a password.';
        return;
      }
    } else if (this.password.length < 6) {
      this.errorMessage = 'Password must contain at least 6 characters.';
      return;
    }

    this.errorMessage = '';
    this.isSigningIn = true;

    this.authService.login(name, email, this.password, this.role).subscribe({
      next: () => {
        this.isSigningIn = false;
        this.password = '';
        this.router.navigateByUrl('/dashboard');
      },
      error: (error: HttpErrorResponse) => {
        this.isSigningIn = false;
        this.errorMessage =
          error.error?.message ||
          'Sign in failed. Check your credentials and make sure the backend services are running.';
      }
    });
  }

  toggleTheme(): void {
    this.themeService.toggle();
  }
}