import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  NgZone,
  OnDestroy,
  ViewChild,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService, UserRole } from '../../core/auth/auth.service';
import { ThemeService } from '../../core/theme/theme.service';

interface ParticleNode {
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  baseAlpha: number;
  color: string;
  pulsePhase: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements AfterViewInit, OnDestroy {
  readonly authService = inject(AuthService);
  readonly router = inject(Router);
  private readonly ngZone = inject(NgZone);
  readonly themeService = inject(ThemeService);

  @ViewChild('heroCanvas', { static: false })
  private heroCanvasRef?: ElementRef<HTMLCanvasElement>;

  fullName = '';
  email = '';
  password = '';
  role: UserRole = 'EMPLOYEE';
  showPassword = false;
  errorMessage = '';
  isSigningIn = false;

  // Signal controlling floating navbar visibility
  readonly showFloatingNav = signal(true);

  // Active capabilities view toggle
  readonly activeFeatureTab = signal<'skills' | 'learning' | 'compliance' | 'career'>('skills');

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

  readonly roleShowcases: Record<
    UserRole,
    {
      badge: string;
      title: string;
      subtitle: string;
      accentColor: string;
      stats: { label: string; value: string; desc: string }[];
    }
  > = {
    EMPLOYEE: {
      badge: '✦ 360 WORKFORCE COMPETENCY PROFILE',
      title: 'Accelerate Skills & Career Trajectory',
      subtitle: 'Benchmark your L1–L5 competencies, complete personalized course tracks, earn verifiable certificates, and unlock automated career progression roadmaps.',
      accentColor: '#38bdf8',
      stats: [
        { label: 'Skill Matrix', value: 'L1–L5 Benchmarks', desc: 'Competency gap assessments' },
        { label: 'Learning Tracks', value: 'Curated LMS Modules', desc: 'Syllabus progress & tests' },
        { label: 'Career Growth', value: 'Target Roadmaps', desc: 'AI-guided promotion path' }
      ]
    },
    HR: {
      badge: '✦ TALENT MANAGEMENT & COMPETENCY INTELLIGENCE',
      title: 'Optimize Workforce Talent & Growth',
      subtitle: 'Monitor organization-wide competency coverage, assess employee skill gaps, assign tailored development plans, and track team growth velocity.',
      accentColor: '#34d399',
      stats: [
        { label: 'Talent Benchmarking', value: 'Skill Coverage', desc: 'Workforce competency matrices' },
        { label: 'Development Plans', value: 'Role Alignments', desc: 'Targeted upskilling journeys' },
        { label: 'Growth Velocity', value: 'Talent Velocity', desc: 'Succession & readiness index' }
      ]
    },
    ADMIN: {
      badge: '✦ PLATFORM OPERATIONS & CURRICULUM MANAGEMENT',
      title: 'Manage Platform Learning & Compliance',
      subtitle: 'Publish curated course catalogs, configure skill assessment criteria, manage digital certificate registries, and oversee system operations.',
      accentColor: '#c084fc',
      stats: [
        { label: 'Course Catalog', value: '850+ LMS Modules', desc: 'Curriculum & lesson authoring' },
        { label: 'Registry Hub', value: 'Audit-Ready Proofs', desc: 'Cryptographic certificates' },
        { label: 'System Health', value: 'High Availability', desc: 'Real-time platform operations' }
      ]
    },
    LEARNER: {
      badge: '✦ OPEN LEARNING & SKILL MASTERY',
      title: 'Explore Curriculums & Earn Certificates',
      subtitle: 'Discover open courses, take interactive quizzes, track self-paced learning milestones, and generate verifiable completion certificates.',
      accentColor: '#fbbf24',
      stats: [
        { label: 'Open Catalog', value: 'Universal Access', desc: 'Self-paced learning paths' },
        { label: 'Skill Validation', value: 'Interactive Tests', desc: 'Knowledge check assessments' },
        { label: 'Certifications', value: 'Digital Badges', desc: 'Verified achievement registry' }
      ]
    }
  };

  selectRole(newRole: UserRole): void {
    this.role = newRole;
  }

  private animFrameId: number | null = null;
  private particles: ParticleNode[] = [];
  private mousePos = { x: -9999, y: -9999, active: false };

  ngAfterViewInit(): void {
    this.initHeroCanvas();
  }

  ngOnDestroy(): void {
    if (this.animFrameId !== null) {
      cancelAnimationFrame(this.animFrameId);
      this.animFrameId = null;
    }
  }

  private initHeroCanvas(): void {
    if (!this.heroCanvasRef) {
      return;
    }

    const canvas = this.heroCanvasRef.nativeElement;
    const ctx = canvas.getContext('2d', { alpha: true });
    if (!ctx) {
      return;
    }

    const resizeCanvas = () => {
      const parent = canvas.parentElement;
      if (parent) {
        const dpr = Math.min(window.devicePixelRatio || 1, 2);
        const displayWidth = parent.clientWidth;
        const displayHeight = parent.clientHeight;
        canvas.width = displayWidth * dpr;
        canvas.height = displayHeight * dpr;
        ctx.setTransform(1, 0, 0, 1, 0, 0);
        ctx.scale(dpr, dpr);
      }
    };

    resizeCanvas();
    window.addEventListener('resize', resizeCanvas);

    // Generate balanced cyber-constellation nodes
    const nodeCount = Math.min(50, Math.floor((window.innerWidth * window.innerHeight) / 32000));
    const colors = [
      '#38bdf8', // Cyan
      '#818cf8', // Indigo
      '#c084fc', // Purple
      '#34d399', // Emerald
      '#ffffff'  // Crisp Starlight
    ];

    this.particles = Array.from({ length: nodeCount }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      vx: (Math.random() - 0.5) * 0.45,
      vy: (Math.random() - 0.5) * 0.45,
      radius: Math.random() * 2 + 1,
      baseAlpha: Math.random() * 0.4 + 0.3,
      color: colors[Math.floor(Math.random() * colors.length)],
      pulsePhase: Math.random() * Math.PI * 2
    }));

    // Mouse interactive tracker on hero
    canvas.parentElement?.addEventListener('mousemove', (e: MouseEvent) => {
      const rect = canvas.getBoundingClientRect();
      this.mousePos.x = e.clientX - rect.left;
      this.mousePos.y = e.clientY - rect.top;
      this.mousePos.active = true;
    });

    canvas.parentElement?.addEventListener('mouseleave', () => {
      this.mousePos.active = false;
    });

    // Run animation outside Angular zone for high 60fps performance
    this.ngZone.runOutsideAngular(() => {
      let lastTime = performance.now();

      const render = (time: number) => {
        const delta = Math.min(time - lastTime, 64) / 1000;
        lastTime = time;

        const w = window.innerWidth;
        const h = canvas.parentElement?.clientHeight || window.innerHeight;

        ctx.clearRect(0, 0, w, h);

        const connectionDist = 135;
        const mouseDist = 160;

        // 1. Update and draw nodes
        for (let i = 0; i < this.particles.length; i++) {
          const p = this.particles[i];

          // Particle movement
          p.x += p.vx * delta * 60;
          p.y += p.vy * delta * 60;

          // Boundary bouncing / wrap
          if (p.x < -20) p.x = w + 20;
          if (p.x > w + 20) p.x = -20;
          if (p.y < -20) p.y = h + 20;
          if (p.y > h + 20) p.y = -20;

          // Mouse gentle deflection / attraction
          if (this.mousePos.active) {
            const dx = this.mousePos.x - p.x;
            const dy = this.mousePos.y - p.y;
            const d = Math.sqrt(dx * dx + dy * dy);
            if (d < mouseDist && d > 0) {
              const force = (1 - d / mouseDist) * 0.45;
              p.x += (dx / d) * force;
              p.y += (dy / d) * force;
            }
          }

          // Pulsing glow
          p.pulsePhase += delta * 1.5;
          const currentAlpha = p.baseAlpha + Math.sin(p.pulsePhase) * 0.15;

          ctx.beginPath();
          ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
          ctx.fillStyle = p.color;
          ctx.globalAlpha = Math.max(0.1, Math.min(0.9, currentAlpha));
          ctx.shadowColor = p.color;
          ctx.shadowBlur = p.radius * 4;
          ctx.fill();

          // 2. Draw connections between nearby nodes
          for (let j = i + 1; j < this.particles.length; j++) {
            const p2 = this.particles[j];
            const dx = p.x - p2.x;
            const dy = p.y - p2.y;
            const dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < connectionDist) {
              const lineAlpha = (1 - dist / connectionDist) * 0.22;
              ctx.beginPath();
              ctx.moveTo(p.x, p.y);
              ctx.lineTo(p2.x, p2.y);
              ctx.strokeStyle = '#38bdf8';
              ctx.globalAlpha = lineAlpha;
              ctx.lineWidth = 0.8;
              ctx.shadowBlur = 0;
              ctx.stroke();
            }
          }
        }

        ctx.globalAlpha = 1;
        ctx.shadowBlur = 0;
        this.animFrameId = requestAnimationFrame(render);
      };

      this.animFrameId = requestAnimationFrame(render);
    });
  }

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

  handleAccessWorkspace(): void {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    } else {
      this.scrollToLogin();
    }
  }

  setFeatureTab(tab: 'skills' | 'learning' | 'compliance' | 'career'): void {
    this.activeFeatureTab.set(tab);
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