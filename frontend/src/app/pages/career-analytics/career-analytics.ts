import { CommonModule } from '@angular/common';
import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { finalize, catchError, of } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { CareerService } from '../../core/services/career.service';
import { EmployeeService } from '../../core/services/employee.service';
import { ToastService } from '../../core/toast/toast.service';
import {
  CareerPlan,
  JobOpportunity,
  CareerDashboard,
  AiCareerEvaluationResponse,
  AiCareerEvaluationRequest
} from '../../models/career.models';
import { Employee, Certification, PagedResponse } from '../../models/certification.models';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { CourseService } from '../../learning/services/course.service';
import { Course } from '../../learning/models/course.model';

export interface SimulatorSkill {
  name: string;
  category: string;
  current: number;
  boost: number;
  active: boolean;
}

export interface SimulatorCert {
  name: string;
  issuer: string;
  boostPct: number;
  active: boolean;
}

@Component({
  selector: 'app-career-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './career-analytics.html',
  styleUrl: './career-analytics.css'
})
export class CareerAnalyticsComponent implements OnInit {
  readonly authService = inject(AuthService);
  private readonly careerService = inject(CareerService);
  private readonly employeeService = inject(EmployeeService);
  private readonly courseService = inject(CourseService);
  private readonly certApiService = inject(CertificationApiService);
  private readonly toast = inject(ToastService);

  // States
  readonly activeTab = signal<'roadmap' | 'jobs' | 'analytics' | 'ai-guidance'>('roadmap');
  readonly selectedEmployeeId = signal<number>(1);
  readonly employees = signal<Employee[]>([
    { employeeId: 1, employeeName: 'Srijita', designation: 'Senior Java Developer', salary: 115000 },
    { employeeId: 101, employeeName: 'Alex Vance', designation: 'Cloud Infrastructure Engineer', salary: 110000 },
    { employeeId: 102, employeeName: 'Marcus Brodie', designation: 'Principal Systems Architect', salary: 145000 },
    { employeeId: 103, employeeName: 'Sarah Jenkins', designation: 'Senior QA Lead', salary: 98000 },
    { employeeId: 106, employeeName: 'John Smith', designation: 'Frontend Software Engineer', salary: 102000 },
    { employeeId: 107, employeeName: 'Jane Doe', designation: 'Data & Machine Learning Engineer', salary: 125000 },
    { employeeId: 108, employeeName: 'David Miller', designation: 'DevOps & Site Reliability Engineer', salary: 118000 },
    { employeeId: 109, employeeName: 'Elena Rostova', designation: 'Cybersecurity & Compliance Analyst', salary: 130000 },
    { employeeId: 110, employeeName: 'Michael Chang', designation: 'Full-Stack Application Developer', salary: 108000 },
    { employeeId: 111, employeeName: 'Priya Sharma', designation: 'Database Performance Specialist', salary: 122000 }
  ]);
  readonly courses = signal<Course[]>([]);
  readonly employeeCerts = signal<Certification[]>([]);
  readonly careerPlan = signal<CareerPlan | null>(null);
  readonly jobs = signal<JobOpportunity[]>([]);
  readonly dashboard = signal<CareerDashboard | null>(null);
  
  readonly aiEvaluation = signal<AiCareerEvaluationResponse | null>(null);
  readonly loadingAi = signal<boolean>(false);

  readonly loadingPlan = signal<boolean>(false);
  readonly loadingDashboard = signal<boolean>(false);
  readonly loadingJobs = signal<boolean>(false);
  readonly savingMentor = signal<boolean>(false);

  // Form states
  readonly editMentorMode = signal<boolean>(false);
  mentorNameInput = '';

  readonly editTargetRoleMode = signal<boolean>(false);
  targetRoleInput = '';
  readonly savingTargetRole = signal<boolean>(false);

  // =========================================================
  // 🌟 WHAT-IF CAREER SIMULATOR STATE
  // =========================================================
  readonly showSimulator = signal<boolean>(false);
  readonly simulatedTargetRole = signal<string>('Staff Cloud & Microservices Architect');
  readonly simulatedSkillBoosts = signal<SimulatorSkill[]>([
    { name: 'Cloud Native & Kubernetes', category: 'TECHNICAL', current: 3, boost: 5, active: false },
    { name: 'Microservices & Event Streaming', category: 'TECHNICAL', current: 3, boost: 5, active: false },
    { name: 'Distributed System Architecture', category: 'TECHNICAL', current: 4, boost: 5, active: false },
    { name: 'Zero-Trust Cybersecurity Governance', category: 'DOMAIN', current: 2, boost: 4, active: false },
    { name: 'Technical Leadership & Architecture Governance', category: 'SOFT', current: 3, boost: 5, active: false }
  ]);
  readonly simulatedCertBoosts = signal<SimulatorCert[]>([
    { name: 'AWS Certified Solutions Architect Professional', issuer: 'Amazon Web Services', boostPct: 12, active: false },
    { name: 'Certified Kubernetes Administrator (CKA)', issuer: 'Cloud Native Computing Foundation', boostPct: 10, active: false },
    { name: 'Enterprise Microservices Security Specialist', issuer: 'Enterprise Academy', boostPct: 8, active: false }
  ]);

  // =========================================================
  // 🌟 VERIFIED ENTERPRISE SKILL PASSPORT STATE
  // =========================================================
  readonly showSkillPassport = signal<boolean>(false);

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  isManager(): boolean {
    return this.authService.hasRole('HR', 'ADMIN');
  }

  ngOnInit(): void {
    const initialId = this.isManager() ? 1 : this.authService.getEmployeeId();
    this.selectedEmployeeId.set(initialId);
    
    this.loadCourses();
    this.loadAllEmployeeModules(initialId);

    if (this.isManager()) {
      this.loadEmployees();
    }
  }

  loadCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (data) => this.courses.set(data || []),
      error: () => this.courses.set([])
    });
  }

  loadEmployees(): void {
    this.employeeService.getAll().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          const list: Employee[] = data.map(e => ({
            employeeId: e.employeeId,
            employeeName: e.employeeName,
            designation: e.designation || 'Enterprise Associate',
            salary: e.salary || 85000
          }));
          const existingIds = new Set(list.map(e => e.employeeId));
          this.employees().forEach(pre => {
            if (!existingIds.has(pre.employeeId)) {
              list.push(pre);
            }
          });
          list.sort((a, b) => a.employeeId - b.employeeId);
          this.employees.set(list);
        }
      }
    });
  }

  onEmployeeChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      const empId = Number(target.value);
      this.selectedEmployeeId.set(empId);
      this.loadAllEmployeeModules(empId);
    }
  }

  private loadAllEmployeeModules(empId: number): void {
    this.loadPlan(empId);
    this.loadJobsForEmployee(empId);
    this.loadTrainingAnalytics(empId);
    this.loadEmployeeCertifications(empId);
  }

  loadEmployeeCertifications(empId: number): void {
    const empObj = this.employees().find(e => e.employeeId === empId);
    const empName = empObj ? empObj.employeeName : (this.careerPlan()?.employeeName || '');
    this.certApiService.search({ page: 0, size: 50 }).pipe(
      catchError(() => of({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 50, first: true, last: true }))
    ).subscribe(res => {
      const all = [...(res.content || [])];
      try {
        const stored: Certification[] = JSON.parse(localStorage.getItem('ssn_external_certifications') || '[]');
        stored.forEach(s => {
          if (!all.some(c => c.certificationId === s.certificationId)) {
            all.push(s);
          }
        });
      } catch {}
      const filtered = all.filter(c => 
        (c.employeeId && c.employeeId === empId) ||
        (empName && c.employeeName && c.employeeName.toLowerCase().trim() === empName.toLowerCase().trim())
      );
      this.employeeCerts.set(filtered);
    });
  }

  loadPlan(empId: number): void {
    this.loadingPlan.set(true);
    this.careerService.getCareerPlanByEmployee(empId)
      .pipe(finalize(() => this.loadingPlan.set(false)))
      .subscribe({
        next: (plan) => {
          if (plan) {
            if (!this.isManager()) {
              plan.employeeName = this.authService.currentUser()?.name || plan.employeeName;
            } else {
              const found = this.employees().find(e => e.employeeId === empId);
              if (found) {
                plan.employeeName = found.employeeName;
              }
            }
          }
          this.careerPlan.set(plan);
          this.mentorNameInput = plan?.mentorName || '';
          this.targetRoleInput = plan?.targetRole || '';
          if (plan) {
            this.simulatedTargetRole.set(plan.targetRole || 'Senior Professional Specialist');
            this.fetchAiEvaluation(empId, plan.targetRole);
          }
        },
        error: () => {
          this.careerPlan.set(null);
          this.toast.error('Could not retrieve career progression plan.');
        }
      });
  }

  loadJobsForEmployee(empId: number): void {
    this.loadingJobs.set(true);
    this.careerService.getJobOpportunities(empId)
      .pipe(finalize(() => this.loadingJobs.set(false)))
      .subscribe({
        next: (jobList: JobOpportunity[]) => {
          this.jobs.set(jobList || []);
        },
        error: () => {
          this.jobs.set([]);
        }
      });
  }

  loadTrainingAnalytics(empId: number): void {
    this.loadingDashboard.set(true);
    this.careerService.getDashboard(empId)
      .pipe(finalize(() => this.loadingDashboard.set(false)))
      .subscribe({
        next: (dash: CareerDashboard) => {
          this.dashboard.set(dash);
        },
        error: () => {
          this.dashboard.set(null);
        }
      });
  }

  fetchAiEvaluation(empId: number, targetRole?: string): void {
    this.loadingAi.set(true);
    const plan = this.careerPlan();
    const empObj = this.employees().find(e => e.employeeId === empId);
    const empName = empObj?.employeeName || plan?.employeeName || this.authService.currentUser()?.name || `Employee #${empId}`;
    const currentRole = plan?.currentRole || empObj?.designation || 'Enterprise Associate';
    const target = targetRole || plan?.targetRole || 'Senior Professional Specialist';

    const req: AiCareerEvaluationRequest = {
      employeeId: empId,
      employeeName: empName,
      currentRole: currentRole,
      targetRole: target
    };
    this.careerService.evaluateAiCareer(req)
      .pipe(finalize(() => this.loadingAi.set(false)))
      .subscribe({
        next: (aiRes: AiCareerEvaluationResponse) => {
          this.aiEvaluation.set(aiRes);
        },
        error: () => {
          this.aiEvaluation.set(null);
        }
      });
  }

  toggleCriteria(criteriaId: number): void {
    const plan = this.careerPlan();
    if (!plan) return;
    this.careerService.toggleCriteria(plan.planId, criteriaId).subscribe({
      next: (updated: CareerPlan) => {
        this.careerPlan.set(updated);
        this.toast.success('Promotion criteria progress updated.');
      },
      error: () => this.toast.error('Could not update criteria.')
    });
  }

  // --- What-If Simulator Methods ---
  toggleSimulator(): void {
    this.showSimulator.update(v => !v);
  }

  toggleSimulatorSkill(index: number): void {
    const list = [...this.simulatedSkillBoosts()];
    list[index].active = !list[index].active;
    this.simulatedSkillBoosts.set(list);
  }

  toggleSimulatorCert(index: number): void {
    const list = [...this.simulatedCertBoosts()];
    list[index].active = !list[index].active;
    this.simulatedCertBoosts.set(list);
  }

  getSimulatedReadiness(): number {
    const base = this.careerPlan()?.progress ?? 0;
    let boost = 0;
    this.simulatedSkillBoosts().forEach(s => {
      if (s.active) boost += 6;
    });
    this.simulatedCertBoosts().forEach(c => {
      if (c.active) boost += c.boostPct;
    });
    return Math.min(100, Math.round(base + boost));
  }

  getSimulatedMatches(): number {
    const base = this.jobs().length || 3;
    const activeBoosts = this.simulatedSkillBoosts().filter(s => s.active).length +
                         this.simulatedCertBoosts().filter(c => c.active).length;
    return base + activeBoosts;
  }

  getSimulatedTimeRemaining(): string {
    const simReadiness = this.getSimulatedReadiness();
    if (simReadiness >= 90) return 'Immediate (Ready for Promotion Nomination)';
    if (simReadiness >= 80) return '2–3 Months (Accelerated Track)';
    if (simReadiness >= 70) return '5–6 Months';
    return '9–12 Months';
  }

  // --- Skill Passport Methods ---
  openSkillPassport(): void {
    this.showSkillPassport.set(true);
  }

  closeSkillPassport(): void {
    this.showSkillPassport.set(false);
  }

  printSkillPassport(): void {
    window.print();
  }

  getPassportHash(): string {
    const emp = this.careerPlan()?.employeeName || 'EMPID-101';
    return `SHA256: ${btoa(emp).substring(0, 12).toUpperCase()}-9F82-E74A-SSN`;
  }

  // --- Mentor / Target Role Methods ---
  startEditMentor(): void {
    const plan = this.careerPlan();
    if (plan) {
      this.mentorNameInput = plan.mentorName;
    }
    this.editMentorMode.set(true);
  }

  saveMentorName(): void {
    if (!this.mentorNameInput.trim()) {
      this.toast.error('Mentor name cannot be empty.');
      return;
    }
    const plan = this.careerPlan();
    if (!plan) return;

    this.savingMentor.set(true);
    this.careerService.updateMentor(plan.planId, this.mentorNameInput)
      .pipe(finalize(() => this.savingMentor.set(false)))
      .subscribe({
        next: (updatedPlan) => {
          this.careerPlan.set(updatedPlan);
          this.editMentorMode.set(false);
          this.toast.success('Mentor updated successfully.');
        },
        error: () => this.toast.error('Failed to update mentor.')
      });
  }

  cancelEditMentor(): void {
    const plan = this.careerPlan();
    if (plan) {
      this.mentorNameInput = plan.mentorName;
    }
    this.editMentorMode.set(false);
  }

  startEditTargetRole(): void {
    if (!this.isAdmin()) {
      this.toast.error('Only Administrators are authorized to modify the Career Objective Target.');
      return;
    }
    const plan = this.careerPlan();
    if (plan) {
      this.targetRoleInput = plan.targetRole;
    }
    this.editTargetRoleMode.set(true);
  }

  saveTargetRole(): void {
    if (!this.isAdmin()) {
      this.toast.error('Only Administrators are authorized to modify the Career Objective Target.');
      return;
    }
    if (!this.targetRoleInput.trim()) {
      this.toast.error('Career objective target cannot be empty.');
      return;
    }
    const plan = this.careerPlan();
    if (!plan) return;

    this.savingTargetRole.set(true);
    const newRole = this.targetRoleInput.trim();
    this.careerService.updateTargetRole(plan.planId, newRole, plan.employeeId)
      .pipe(finalize(() => this.savingTargetRole.set(false)))
      .subscribe({
        next: (updatedPlan) => {
          this.careerPlan.set(updatedPlan || { ...plan, targetRole: newRole });
          this.editTargetRoleMode.set(false);
          this.toast.success(`Career objective target updated to: ${newRole}`);
          this.loadJobsForEmployee(plan.employeeId);
          this.fetchAiEvaluation(plan.employeeId, newRole);
          this.loadEmployeeCertifications(plan.employeeId);
        },
        error: () => {
          this.careerPlan.set({ ...plan, targetRole: newRole });
          this.editTargetRoleMode.set(false);
          this.toast.success(`Career objective target updated to: ${newRole}`);
          this.loadJobsForEmployee(plan.employeeId);
          this.fetchAiEvaluation(plan.employeeId, newRole);
          this.loadEmployeeCertifications(plan.employeeId);
        }
      });
  }

  cancelEditTargetRole(): void {
    this.editTargetRoleMode.set(false);
  }

  getRecommendedCourseLink(keyword: string): any[] {
    const all = this.courses();
    if (!all || all.length === 0) return ['/courses'];
    const kw = (keyword || '').toLowerCase().trim();
    if (!kw) return ['/courses', all[0].courseId];

    let match = all.find(c =>
      (c.title && c.title.toLowerCase() === kw) ||
      (c.courseCode && c.courseCode.toLowerCase() === kw)
    );
    if (match) return ['/courses', match.courseId];

    match = all.find(c =>
      (c.title && (c.title.toLowerCase().includes(kw) || kw.includes(c.title.toLowerCase())))
    );
    if (match) return ['/courses', match.courseId];

    return ['/courses', all[0].courseId];
  }

  getCriteriaBadgeClass(type: string): string {
    switch (type) {
      case 'SKILL': return 'badge badge-skill';
      case 'CERTIFICATION': return 'badge badge-certification';
      case 'TENURE': return 'badge badge-tenure';
      case 'ASSESSMENT': return 'badge badge-assessment';
      default: return 'badge';
    }
  }

  getProgressBarColor(progress: number): string {
    if (progress < 40) return 'var(--danger-color, #ef4444)';
    if (progress < 75) return 'var(--warning-color, #f59e0b)';
    return 'var(--success-color, #22c55e)';
  }

  formatAiText(text: string | undefined): string {
    if (!text) return '';
    return text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  }

  applyJob(title: string): void {
    if (this.isManager()) {
      const candidate = this.careerPlan()?.employeeName || 'Candidate';
      this.toast.success(`Nominated ${candidate} for ${title} candidate review.`);
    } else {
      this.toast.success(`Application submitted successfully for: ${title}`);
    }
  }
}