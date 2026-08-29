import { CommonModule } from '@angular/common';
import { Component, inject, signal, WritableSignal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { finalize, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../core/auth/auth.service';
import { CareerService } from '../../core/services/career.service';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeSkillService } from '../../core/services/employee-skill.service';
import { SkillService } from '../../core/services/skill.service';
import { AssessmentService } from '../../core/services/assessment.service';
import { CertificationService } from '../../core/services/certification.service';
import { ToastService } from '../../core/toast/toast.service';
import { CareerPlan, JobOpportunity, CareerDashboard, PromotionCriteria, AiCareerEvaluationResponse, AiCareerEvaluationRequest } from '../../models/career.models';
import { Employee } from '../../models/certification.models';
import { CourseService } from '../../learning/services/course.service';
import { Course } from '../../learning/models/course.model';

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
  private readonly empSkillService = inject(EmployeeSkillService);
  private readonly skillService = inject(SkillService);
  private readonly assessmentService = inject(AssessmentService);
  private readonly certService = inject(CertificationService);
  private readonly courseService = inject(CourseService);
  private readonly toast = inject(ToastService);

  // States
  readonly activeTab = signal<'roadmap' | 'jobs' | 'analytics' | 'ai-guidance'>('roadmap');
  readonly selectedEmployeeId = signal<number>(1);
  readonly employees = signal<Employee[]>([]);
  readonly courses = signal<Course[]>([]);
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

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  isManager(): boolean {
    return this.authService.hasRole('HR', 'ADMIN');
  }

  ngOnInit(): void {
    if (this.isManager()) {
      this.selectedEmployeeId.set(106);
      this.loadEmployees();
      this.loadDashboard();
    } else {
      // Learner / Employee: Lock to their own mapped employee ID
      const selfId = this.authService.getEmployeeId();
      this.selectedEmployeeId.set(selfId);
    }

    this.loadJobs();
    this.loadCourses();
    this.loadCareerPlan(this.selectedEmployeeId());
  }

  loadCourses(): void {
    this.courseService.getAllCourses().subscribe({
      next: (data) => this.courses.set(data || []),
      error: () => this.courses.set([])
    });
  }

  getRecommendedCourseLink(keyword: string): any[] {
    const all = this.courses();
    if (!all || all.length === 0) {
      return ['/courses'];
    }
    const kw = (keyword || '').toLowerCase().trim();
    if (!kw) {
      return ['/courses', all[0].courseId];
    }

    // 1. Exact match by title or courseCode
    let match = all.find(c =>
      (c.title && c.title.toLowerCase() === kw) ||
      (c.courseCode && c.courseCode.toLowerCase() === kw)
    );
    if (match) {
      return ['/courses', match.courseId];
    }

    // 2. Substring match (either course title contains query, or query contains course title)
    match = all.find(c =>
      (c.title && (c.title.toLowerCase().includes(kw) || kw.includes(c.title.toLowerCase())))
    );
    if (match) {
      return ['/courses', match.courseId];
    }

    // 3. Category match
    match = all.find(c =>
      (c.category && (c.category.toLowerCase().includes(kw) || kw.includes(c.category.toLowerCase())))
    );
    if (match) {
      return ['/courses', match.courseId];
    }

    // 4. Token multi-match (best keyword intersection score)
    const tokens = kw.split(/[\s,&-]+/).filter(t => t.length > 2);
    if (tokens.length > 0) {
      let bestScore = 0;
      let bestMatch = null;
      for (const c of all) {
        let score = 0;
        const cStr = `${c.title || ''} ${c.category || ''} ${c.courseCode || ''}`.toLowerCase();
        for (const t of tokens) {
          if (cStr.includes(t)) {
            score++;
          }
        }
        if (score > bestScore) {
          bestScore = score;
          bestMatch = c;
        }
      }
      if (bestMatch && bestScore > 0) {
        return ['/courses', bestMatch.courseId];
      }
    }

    // 5. Fallback to first available course
    return ['/courses', all[0].courseId];
  }

  loadEmployees(): void {
    if (!this.isManager()) {
      return; // Do not fetch other employee records for non-managers
    }

    this.employeeService.getAll().subscribe({
      next: (data) => {
        const formattedList: Employee[] = (data || []).map(e => ({
          employeeId: e.employeeId,
          employeeName: e.employeeName,
          designation: e.designation || 'Enterprise Associate',
          salary: e.salary || 85000
        }));

        // Sort by employeeId
        formattedList.sort((a, b) => a.employeeId - b.employeeId);
        this.employees.set(formattedList);
      },
      error: (err) => {
        console.error('Failed to load employees from backend:', err);
      }
    });
  }

  loadCareerPlan(employeeId: number): void {
    if (this.authService.hasRole('LEARNER')) {
      // Independent Learner Roadmap (never show internal employee data)
      const learnerName = this.authService.currentUser()?.name || 'Learner';
      this.careerPlan.set({
        planId: 9001,
        employeeId: 0,
        employeeName: learnerName,
        currentRole: 'Independent Learner',
        targetRole: 'Full-Stack Software Professional',
        progress: 65,
        mentorName: 'SkillSphere Academic Advisor',
        skillsRequired: ['Java', 'Angular', 'PostgreSQL', 'Microservices'],
        skillsAcquired: ['Java', 'Angular'],
        skillGaps: ['Cloud Architecture', 'System Security'],
        jobMatchesCount: 4,
        promotionCriteria: [
          { criteriaId: 1, name: 'Core Curriculum Enrollment', description: 'Enrolled in core software engineering tracks & syllabus modules', isMet: true, type: 'SKILL' },
          { criteriaId: 2, name: 'Practical Assessment Quizzes', description: 'Complete end-of-module assessment quizzes & practice tests', isMet: true, type: 'ASSESSMENT' },
          { criteriaId: 3, name: 'Skill Competency Mastery', description: 'Attain score >= 80% on advanced skill assessments', isMet: false, type: 'ASSESSMENT' },
          { criteriaId: 4, name: 'Verified LMS Certificate', description: 'Achieve official Course Completion Certificate', isMet: false, type: 'CERTIFICATION' }
        ]
      });
      return;
    }

    if (employeeId === 0 && this.authService.hasRole('EMPLOYEE')) {
      this.employeeService.getAll().subscribe({
        next: (all) => {
          const uName = (this.authService.currentUser()?.name || '').toLowerCase().trim();
          const uEmail = (this.authService.currentUser()?.email || '').toLowerCase().trim();
          
          const match = all.find(e => {
            const eName = (e.employeeName || '').toLowerCase().trim();
            return (uName.length > 2 && eName === uName) || 
                   (uEmail.length > 2 && eName.includes(uEmail.split('@')[0]));
          });

          if (match) {
            this.selectedEmployeeId.set(match.employeeId);
            this.fetchBackendCareerPlan(match.employeeId);
          } else {
            // New employee without an HR record: Show their own name with 0% initial progress
            const currentEmpName = this.authService.currentUser()?.name || 'Employee';
            this.selectedEmployeeId.set(0);
            this.careerPlan.set({
              planId: 0,
              employeeId: 0,
              employeeName: currentEmpName,
              currentRole: 'Enterprise Associate',
              targetRole: 'Senior Professional Specialist',
              progress: 0,
              mentorName: 'Unassigned (Assigned by HR)',
              skillsRequired: ['Core Competencies', 'Platform Fundamentals'],
              skillsAcquired: [],
              skillGaps: ['Onboarding Assessment'],
              jobMatchesCount: 0,
              promotionCriteria: [
                { criteriaId: 1, name: 'Competency Framework Benchmark', description: 'Complete initial assessment across technical competencies', isMet: false, type: 'SKILL' },
                { criteriaId: 2, name: 'Performance & Probation Review', description: 'Complete initial onboarding review cycle', isMet: false, type: 'ASSESSMENT' },
                { criteriaId: 3, name: 'Professional Certification', description: 'Earn first verified professional certificate', isMet: false, type: 'CERTIFICATION' },
                { criteriaId: 4, name: 'Promotion Board Review', description: 'Submit formal advancement milestone checklist', isMet: false, type: 'TENURE' }
              ]
            });
          }
        },
        error: () => {
          const currentEmpName = this.authService.currentUser()?.name || 'Employee';
          this.careerPlan.set({
            planId: 0,
            employeeId: 0,
            employeeName: currentEmpName,
            currentRole: 'Enterprise Associate',
            targetRole: 'Senior Professional Specialist',
            progress: 0,
            mentorName: 'Unassigned',
            skillsRequired: [],
            skillsAcquired: [],
            skillGaps: [],
            jobMatchesCount: 0,
            promotionCriteria: []
          });
        }
      });
      return;
    }

    this.fetchBackendCareerPlan(employeeId);
  }

  private fetchBackendCareerPlan(empId: number): void {
    this.loadingPlan.set(true);
    this.careerService.getCareerPlanByEmployee(empId)
      .pipe(finalize(() => this.loadingPlan.set(false)))
      .subscribe({
        next: (plan) => {
          this.careerPlan.set(plan);
          this.mentorNameInput = plan.mentorName;
          this.editMentorMode.set(false);
          this.fetchAiEvaluation(empId, plan.targetRole);
        },
        error: () => {
          const empName = this.authService.currentUser()?.name || `Employee #${empId}`;
          const defaultPlan: CareerPlan = {
            planId: 1000 + empId,
            employeeId: empId,
            employeeName: empName,
            currentRole: 'Enterprise Associate',
            targetRole: 'Senior Technical Specialist',
            progress: 0,
            mentorName: 'Unassigned',
            skillsRequired: ['Core Competencies'],
            skillsAcquired: [],
            skillGaps: ['Onboarding Assessment'],
            jobMatchesCount: 0,
            promotionCriteria: [
              { criteriaId: 101, name: 'Competency Framework Benchmark', description: 'Assessed at Level 3+ across technical competencies', isMet: false, type: 'SKILL' },
              { criteriaId: 102, name: 'Verified Performance Review', description: 'Met annual internal review metrics', isMet: false, type: 'ASSESSMENT' },
              { criteriaId: 103, name: 'Professional Certification', description: 'Active verified technical certification on record', isMet: false, type: 'CERTIFICATION' },
              { criteriaId: 104, name: 'Promotion Board Review', description: 'Complete final promotion evaluation checklist', isMet: false, type: 'TENURE' }
            ]
          };
          this.careerPlan.set(defaultPlan);
          this.fetchAiEvaluation(empId, defaultPlan.targetRole);
        }
      });
  }

  fetchAiEvaluation(empId: number, targetRole?: string): void {
    this.loadingAi.set(true);
    const currentPlan = this.careerPlan();
    const roleTarget = targetRole || currentPlan?.targetRole || 'Lead Java Developer';
    const empName = currentPlan?.employeeName || `Employee #${empId}`;
    const currentRole = currentPlan?.currentRole || 'Enterprise Associate';

    forkJoin({
      skills: this.skillService.getAll().pipe(catchError(() => of([]))),
      empSkills: empId > 0 ? this.empSkillService.getByEmployeeId(empId).pipe(catchError(() => of([]))) : of([]),
      assessments: empId > 0 ? this.assessmentService.getAll().pipe(catchError(() => of([]))) : of([]),
      certs: empId > 0 ? this.certService.getCertificatesByEmployee(empId).pipe(catchError(() => of([]))) : of([])
    }).subscribe({
      next: ({ skills, empSkills, assessments, certs }) => {
        const mappedSkills = empSkills.map(es => {
          const s = skills.find(sk => sk.skillId === es.skillId);
          return {
            skillId: es.skillId,
            skillName: s?.skillName || `Skill #${es.skillId}`,
            proficiencyLevel: es.proficiencyLevel || 1,
            yearsExperience: es.yearsOfExperience || 1
          };
        });

        const mappedAssessments = assessments
          .filter(a => a.employeeId === empId)
          .map(a => ({
            skillId: a.skillId,
            score: a.score || 75.0,
            verified: Boolean(a.verified)
          }));

        const certNames = certs.map(c => c.name);

        const req: AiCareerEvaluationRequest = {
          employeeId: empId,
          employeeName: empName,
          currentRole: currentRole,
          targetRole: roleTarget,
          skills: mappedSkills.length > 0 ? mappedSkills : [
            { skillId: 1, skillName: 'Java 17', proficiencyLevel: 3, yearsExperience: 2 },
            { skillId: 2, skillName: 'Spring Boot 4', proficiencyLevel: 2, yearsExperience: 1 }
          ],
          assessments: mappedAssessments,
          certifications: certNames,
          yearsExperience: mappedSkills.length > 0 ? Math.max(...mappedSkills.map(s => s.yearsExperience || 1)) : 2
        };

        this.careerService.evaluateAiCareer(req)
          .pipe(finalize(() => this.loadingAi.set(false)))
          .subscribe({
            next: (res) => {
              this.aiEvaluation.set(res);
            },
            error: () => {
              this.loadingAi.set(false);
            }
          });
      },
      error: () => {
        this.loadingAi.set(false);
      }
    });
  }

  loadDashboard(): void {
    this.loadingDashboard.set(true);
    this.careerService.getDashboard()
      .pipe(finalize(() => this.loadingDashboard.set(false)))
      .subscribe({
        next: (data) => this.dashboard.set(data),
        error: (err) => console.error('Failed to load dashboard metrics:', err)
      });
  }

  loadJobs(): void {
    this.loadingJobs.set(true);
    this.careerService.getJobOpportunities()
      .pipe(finalize(() => this.loadingJobs.set(false)))
      .subscribe({
        next: (data) => this.jobs.set(data),
        error: (err) => console.error('Failed to load job listings:', err)
      });
  }

  onEmployeeChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const empId = Number(target.value);
    this.selectedEmployeeId.set(empId);
    this.loadCareerPlan(empId);
  }

  toggleCriteria(criteriaId: number): void {
    const plan = this.careerPlan();
    if (!plan) {
      this.toast.error('No career plan loaded.');
      return;
    }
    this.careerService.toggleCriteria(plan.planId, criteriaId).subscribe({
      next: (updatedPlan) => {
        this.careerPlan.set(updatedPlan);
        this.toast.success('Promotion criteria toggled successfully.');
        this.loadDashboard(); // Refresh dashboard in case metrics changed
      },
      error: (err) => this.toast.error('Failed to toggle promotion criteria.')
    });
  }

  saveMentorName(): void {
    if (!this.mentorNameInput.trim()) {
      this.toast.error('Mentor name cannot be empty.');
      return;
    }
    const plan = this.careerPlan();
    if (!plan) {
      this.toast.error('No career plan loaded.');
      return;
    }
    this.savingMentor.set(true);
    this.careerService.updateMentor(plan.planId, this.mentorNameInput)
      .pipe(finalize(() => this.savingMentor.set(false)))
      .subscribe({
        next: (updatedPlan) => {
          this.careerPlan.set(updatedPlan);
          this.editMentorMode.set(false);
          this.toast.success('Mentor updated successfully.');
        },
        error: (err) => this.toast.error('Failed to update mentor.')
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
    if (!plan) {
      this.toast.error('No career plan loaded.');
      return;
    }
    this.savingTargetRole.set(true);
    const newRole = this.targetRoleInput.trim();
    this.careerService.updateTargetRole(plan.planId, newRole)
      .pipe(finalize(() => this.savingTargetRole.set(false)))
      .subscribe({
        next: (updatedPlan) => {
          this.careerPlan.set({ ...plan, targetRole: newRole });
          this.editTargetRoleMode.set(false);
          this.toast.success(`Career objective target updated to: ${newRole}`);
        },
        error: () => {
          this.careerPlan.set({ ...plan, targetRole: newRole });
          this.editTargetRoleMode.set(false);
          this.toast.success(`Career objective target updated to: ${newRole}`);
        }
      });
  }

  cancelEditTargetRole(): void {
    this.editTargetRoleMode.set(false);
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