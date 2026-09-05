import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { AuthService } from '../../core/auth/auth.service';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeSkillService } from '../../core/services/employee-skill.service';
import { SkillService } from '../../core/services/skill.service';
import { AssessmentService } from '../../core/services/assessment.service';
import { CompetencyService } from '../../core/services/competency.service';
import { CertificationService } from '../../core/services/certification.service';
import { CertificationApiService } from '../../core/api/certification-api.service';
import { CareerService } from '../../core/services/career.service';
import { CourseService } from '../../learning/services/course.service';
import { EnrollmentService } from '../../learning/services/enrollment.service';
import { NotificationService } from '../../core/services/notification.service';
import { ToastService } from '../../shared/services/toast.service';

import { Employee } from '../../learning/models/employee';
import { EmployeeSkill } from '../../learning/models/employee-skill';
import { Skill } from '../../learning/models/skill';
import { Competency } from '../../learning/models/competency';
import { Certificate } from '../../learning/models/certification';
import { Certification } from '../../models/certification.models';
import { CareerPlan, JobOpportunity, AiCareerEvaluationResponse, AiCareerEvaluationRequest } from '../../models/career.models';
import { Course } from '../../learning/models/course.model';
import { Enrollment } from '../../learning/models/enrollment.model';

@Component({
  selector: 'app-employee-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './employee-profile.html',
  styleUrls: ['./employee-profile.css']
})
export class EmployeeProfile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  readonly authService = inject(AuthService);
  private readonly empService = inject(EmployeeService);
  private readonly empSkillService = inject(EmployeeSkillService);
  private readonly skillService = inject(SkillService);
  private readonly assessmentService = inject(AssessmentService);
  private readonly competencyService = inject(CompetencyService);
  private readonly m1CertService = inject(CertificationService);
  private readonly certApiService = inject(CertificationApiService);
  private readonly careerService = inject(CareerService);
  private readonly courseService = inject(CourseService);
  private readonly enrollmentService = inject(EnrollmentService);
  private readonly notifService = inject(NotificationService);
  private readonly toast = inject(ToastService);

  // Active Tab
  readonly activeTab = signal<'overview' | 'skills' | 'learning' | 'certifications' | 'career'>('overview');

  // Employee State
  readonly employeeId = signal<number>(1);
  readonly employee = signal<Employee | null>(null);
  readonly isLoading = signal<boolean>(true);
  readonly isSelfProfile = signal<boolean>(true);

  // M1 States
  readonly skills = signal<(EmployeeSkill & { skillName?: string; category?: string; verified?: boolean; score?: number })[]>([]);
  readonly competencies = signal<Competency[]>([]);
  readonly allSkillsLibrary = signal<Skill[]>([]);

  // M2 States
  readonly courses = signal<Course[]>([]);
  readonly enrollments = signal<Enrollment[]>([]);

  // M3 States
  readonly certifications = signal<Certification[]>([]);
  readonly showAddCertModal = signal<boolean>(false);
  readonly isSubmittingCert = signal<boolean>(false);
  certForm!: FormGroup;

  // M4 States
  readonly careerPlan = signal<CareerPlan | null>(null);
  readonly jobMatches = signal<JobOpportunity[]>([]);
  readonly aiEvaluation = signal<AiCareerEvaluationResponse | null>(null);
  readonly loadingAi = signal<boolean>(false);

  ngOnInit(): void {
    this.initCertForm();
    this.resolveEmployeeTarget();
  }

  private initCertForm(): void {
    const today = new Date().toISOString().substring(0, 10);
    const defaultExpiry = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().substring(0, 10);

    this.certForm = this.fb.group({
      certificationName: ['', [Validators.required, Validators.minLength(3)]],
      issuingOrganization: ['', [Validators.required]],
      certificateId: ['CERT-' + Math.floor(100000 + Math.random() * 900000)],
      issueDate: [today, [Validators.required]],
      expiryDate: [defaultExpiry],
      warningWindowDays: [30, [Validators.required, Validators.min(7)]]
    });
  }

  private resolveEmployeeTarget(): void {
    const routeId = this.route.snapshot.paramMap.get('id');
    const currentUser = this.authService.currentUser();

    if (routeId) {
      const idNum = Number(routeId);
      this.employeeId.set(idNum);
      this.isSelfProfile.set(currentUser?.employeeId === idNum);
    } else {
      const selfId = this.authService.getEmployeeId();
      this.employeeId.set(selfId);
      this.isSelfProfile.set(true);
    }

    this.loadAllEmployeeData(this.employeeId());
  }

  loadAllEmployeeData(empId: number): void {
    this.isLoading.set(true);

    const learnerId = this.authService.getLearnerId();
    const enrollments$ = learnerId
      ? this.enrollmentService.getLearnerEnrollments(learnerId).pipe(catchError(() => of([])))
      : of([]);

    forkJoin({
      allEmployees: this.empService.getAll().pipe(catchError(() => of([]))),
      employee: empId > 0 ? this.empService.getById(empId).pipe(catchError(() => of(null))) : of(null),
      skillsLibrary: this.skillService.getAll().pipe(catchError(() => of([]))),
      empSkills: empId > 0 ? this.empSkillService.getByEmployeeId(empId).pipe(catchError(() => of([]))) : of([]),
      assessments: this.assessmentService.getAll().pipe(catchError(() => of([]))),
      competencies: this.competencyService.getAll().pipe(catchError(() => of([]))),
      courses: this.courseService.getAllCourses().pipe(catchError(() => of([]))),
      enrollments: enrollments$,
      careerPlan: empId > 0 ? this.careerService.getCareerPlanByEmployee(empId).pipe(catchError(() => of(null))) : of(null),
      jobs: this.careerService.getJobOpportunities().pipe(catchError(() => of([])))
    }).subscribe({
      next: (res) => {
        let currentEmp = res.employee;
        const loggedInUser = this.authService.currentUser();
        if (this.isSelfProfile() && loggedInUser) {
          currentEmp = {
            employeeId: loggedInUser.employeeId || this.employeeId(),
            employeeName: loggedInUser.name,
            designation: (res.employee && res.employee.employeeName.toLowerCase() === loggedInUser.name.toLowerCase()) ? res.employee.designation : 'Enterprise Professional',
            salary: (res.employee && res.employee.employeeName.toLowerCase() === loggedInUser.name.toLowerCase()) ? res.employee.salary : 85000
          };
          this.employeeId.set(currentEmp.employeeId);
        } else if (!currentEmp) {
          const userName = (loggedInUser?.name || '').toLowerCase().trim();
          const match = res.allEmployees.find(e => {
            const eName = (e.employeeName || '').toLowerCase().trim();
            return (userName.length > 2 && eName === userName) || (userName.length > 2 && eName.includes(userName));
          });

          if (match) {
            currentEmp = match;
            this.employeeId.set(match.employeeId);
          } else {
            currentEmp = {
              employeeId: empId,
              employeeName: loggedInUser?.name || `Employee #${empId}`,
              designation: 'Enterprise Professional',
              salary: 85000
            };
          }
        }

        this.employee.set(currentEmp);
        this.allSkillsLibrary.set(res.skillsLibrary);
        this.competencies.set(res.competencies);
        this.courses.set(res.courses);
        this.enrollments.set(res.enrollments);

        if (res.careerPlan) {
          const plan = { ...res.careerPlan };
          plan.employeeName = currentEmp.employeeName;
          this.careerPlan.set(plan);
        } else {
          this.careerPlan.set({
            planId: 0,
            employeeId: currentEmp.employeeId,
            employeeName: currentEmp.employeeName,
            currentRole: currentEmp.designation || 'Enterprise Associate',
            targetRole: 'Senior Professional Specialist',
            progress: 0,
            mentorName: 'Unassigned',
            skillsRequired: ['Platform Fundamentals', 'Core Technical Skills', 'Domain Methodology'],
            skillsAcquired: [],
            skillGaps: ['Onboarding Competency Benchmark Required'],
            jobMatchesCount: 0,
            promotionCriteria: [
              { criteriaId: 1, name: 'Competency Framework Benchmark', description: 'Complete initial assessment across technical competencies', isMet: false, type: 'SKILL' },
              { criteriaId: 2, name: 'Foundational Learning Path', description: 'Enroll and complete first required enterprise training course', isMet: false, type: 'ASSESSMENT' },
              { criteriaId: 3, name: 'Professional Certification', description: 'Earn first verified professional certificate', isMet: false, type: 'CERTIFICATION' },
              { criteriaId: 4, name: 'Probation Review', description: 'Complete initial onboarding review cycle', isMet: false, type: 'TENURE' }
            ]
          });
        }

        this.jobMatches.set(res.jobs);

        // Enhance employee skills with names, category, and assessment scores
        const targetId = currentEmp.employeeId;
        const empAssessments = res.assessments.filter(a => a.employeeId === targetId);
        const mappedSkills = res.empSkills.filter(es => es.employeeId === targetId).map(es => {
          const libSkill = res.skillsLibrary.find(s => s.skillId === es.skillId);
          const assessment = empAssessments.find(a => a.skillId === es.skillId);
          return {
            ...es,
            skillName: libSkill?.skillName || `Skill #${es.skillId}`,
            category: libSkill?.category || 'TECHNICAL',
            verified: assessment?.verified || false,
            score: assessment?.score || 0
          };
        });
        this.skills.set(mappedSkills);

        // Load M3 Certifications
        this.loadCertifications(currentEmp.employeeName);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  loadCertifications(employeeName: string): void {
    const targetId = this.employeeId();
    const empNameLower = (employeeName || '').toLowerCase().trim();

    forkJoin({
      m3Certs: this.certApiService.search({ size: 50 }).pipe(
        map(p => p.content || []),
        catchError(() => of([]))
      ),
      m1Certs: this.m1CertService.getCertificatesByEmployee(targetId).pipe(
        catchError(() => of([]))
      )
    }).subscribe({
      next: ({ m3Certs, m1Certs }) => {
        const matchedM3 = (m3Certs || []).filter(c => 
          c.employeeId === targetId && (c.employeeName && c.employeeName.toLowerCase().trim() === empNameLower)
        );

        const mappedM1: Certification[] = (m1Certs || []).filter(m => m.empid === targetId).map((m1: Certificate): Certification => ({
          certificationId: 'm1-' + m1.certid,
          employeeId: m1.empid,
          employeeName: employeeName,
          certificationName: m1.name,
          issuingOrganization: m1.issuingOrganization || 'Professional Body',
          credentialNumber: 'M1-CERT-' + m1.certid,
          issueDate: m1.issueDate || '2024-01-01',
          expiryDate: m1.expiry || null,
          daysRemaining: 365,
          status: (m1.status === 'Valid' ? 'VALID' : 'EXPIRED'),
          renewalStatus: 'NOT_REQUIRED',
          verificationStatus: 'VERIFIED',
          complianceStatus: 'COMPLIANT',
          active: m1.status === 'Valid',
          warningWindowDays: 30,
          renewalDueDate: null,
          legacyCertificateId: m1.certid,
          sourceSystem: 'M1_LEGACY',
          lastEvaluatedAt: null,
          createdAt: m1.issueDate || new Date().toISOString(),
          updatedAt: m1.issueDate || new Date().toISOString()
        }));

        const combined = [...matchedM3];
        for (const c1 of mappedM1) {
          if (!combined.some(c3 => c3.certificationName.toLowerCase() === c1.certificationName.toLowerCase())) {
            combined.push(c1);
          }
        }

        this.certifications.set(combined);
      }
    });
  }

  openAddCertModal(): void {
    this.initCertForm();
    this.showAddCertModal.set(true);
  }

  closeAddCertModal(): void {
    this.showAddCertModal.set(false);
  }

  submitExternalCert(): void {
    if (this.certForm.invalid) {
      this.certForm.markAllAsTouched();
      return;
    }

    this.isSubmittingCert.set(true);
    const formVal = this.certForm.value;
    const emp = this.employee();

    const payload = {
      employeeId: this.employeeId(),
      certificationName: formVal.certificationName,
      issuingOrganization: formVal.issuingOrganization,
      credentialNumber: formVal.certificateId || formVal.credentialNumber,
      issueDate: formVal.issueDate,
      expiryDate: formVal.expiryDate || null,
      warningWindowDays: formVal.warningWindowDays || 30
    };

    const newPendingCert: Certification = {
      certificationId: 'ext-' + Date.now(),
      employeeId: this.employeeId(),
      employeeName: emp?.employeeName || 'Employee',
      certificationName: formVal.certificationName,
      issuingOrganization: formVal.issuingOrganization,
      credentialNumber: formVal.certificateId || formVal.credentialNumber,
      issueDate: formVal.issueDate,
      expiryDate: formVal.expiryDate || null,
      daysRemaining: 365,
      status: 'VALID',
      renewalStatus: 'NOT_REQUIRED',
      verificationStatus: 'PENDING',
      complianceStatus: 'PENDING',
      active: true,
      warningWindowDays: formVal.warningWindowDays || 30,
      renewalDueDate: null,
      legacyCertificateId: null,
      sourceSystem: 'EXTERNAL_SUBMISSION',
      lastEvaluatedAt: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    // Store in local submission pool
    try {
      const stored: Certification[] = JSON.parse(localStorage.getItem('ssn_external_certifications') || '[]');
      stored.unshift(newPendingCert);
      localStorage.setItem('ssn_external_certifications', JSON.stringify(stored));
    } catch {}

    // Dispatch notification to HR and Admin
    this.notifService.notifyExternalCertificateSubmitted(
      emp?.employeeName || 'Employee',
      formVal.certificationName,
      newPendingCert.certificationId,
      emp?.employeeId
    );

    this.certApiService.create(payload).pipe(catchError(() => of(newPendingCert))).subscribe({
      next: (created) => {
        this.isSubmittingCert.set(false);
        this.toast.showSuccess(`Certificate "${formVal.certificationName}" submitted for HR/Admin verification.`);
        this.closeAddCertModal();
        if (emp) {
          this.loadCertifications(emp.employeeName);
        }
      },
      error: () => {
        this.isSubmittingCert.set(false);
        this.toast.showSuccess(`Certificate "${formVal.certificationName}" submitted for HR/Admin verification.`);
        this.closeAddCertModal();
        if (emp) {
          this.loadCertifications(emp.employeeName);
        }
      }
    });
  }

  toggleCriteria(criteriaId: number): void {
    const plan = this.careerPlan();
    if (!plan) return;

    this.careerService.toggleCriteria(plan.planId, criteriaId).subscribe({
      next: (updated) => {
        this.careerPlan.set(updated);
        this.toast.showSuccess('Promotion criteria progress updated.');
      },
      error: () => this.toast.showError('Could not update criteria.')
    });
  }

  getSkillProficiencyLabel(level: number): string {
    switch (level) {
      case 5: return 'Master (L5)';
      case 4: return 'Expert (L4)';
      case 3: return 'Proficient (L3)';
      case 2: return 'Intermediate (L2)';
      default: return 'Beginner (L1)';
    }
  }

  getCertStatusClass(status: string): string {
    switch (status) {
      case 'VALID': return 'badge-success';
      case 'EXPIRING_SOON': return 'badge-warning';
      case 'EXPIRED': return 'badge-danger';
      default: return 'badge-info';
    }
  }

  formatAiText(text: string | undefined): string {
    if (!text) return '';
    return text.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  }

  generateAiCareerGuidance(): void {
    const emp = this.employee();
    const plan = this.careerPlan();
    if (!emp) return;

    this.loadingAi.set(true);
    const mappedSkills = this.skills().map(s => ({
      skillId: s.skillId,
      skillName: s.skillName || `Skill #${s.skillId}`,
      proficiencyLevel: s.proficiencyLevel || 1,
      yearsExperience: s.yearsOfExperience || 1
    }));

    const certNames = this.certifications().map(c => c.certificationName);

    const req: AiCareerEvaluationRequest = {
      employeeId: emp.employeeId,
      employeeName: emp.employeeName,
      currentRole: emp.designation,
      targetRole: plan?.targetRole || 'Lead Technical Architect',
      skills: mappedSkills,
      certifications: certNames,
      yearsExperience: mappedSkills.length > 0 ? Math.max(...mappedSkills.map(s => s.yearsExperience || 1)) : 2
    };

    this.careerService.evaluateAiCareer(req).subscribe({
      next: (res) => {
        this.aiEvaluation.set(res);
        this.loadingAi.set(false);
        this.toast.showSuccess('AI Career Evaluation generated successfully!');
      },
      error: () => {
        this.loadingAi.set(false);
        this.toast.showError('Could not generate AI Career guidance.');
      }
    });
  }

  // =========================================================
  // 🌟 SKILL FRESHNESS & DECAY INDEX
  // =========================================================
  getSkillFreshness(s: any): { status: 'FRESH' | 'AGING' | 'STALE'; percentage: number; label: string; class: string } {
    const level = s.proficiencyLevel || 1;
    const score = s.score || 0;
    
    if (level >= 4 || score >= 85) {
      return { status: 'FRESH', percentage: 100, label: 'Fresh (Active & Current)', class: 'fresh-pill' };
    }
    if (level === 3 || score >= 70) {
      return { status: 'AGING', percentage: 75, label: 'Aging (Refresher Due)', class: 'aging-pill' };
    }
    return { status: 'STALE', percentage: 45, label: 'Stale (Recertification Needed)', class: 'stale-pill' };
  }

  // =========================================================
  // 🌟 VERIFIED SKILL PASSPORT STATE
  // =========================================================
  readonly showSkillPassport = signal<boolean>(false);

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
    const emp = this.employee()?.employeeName || 'EMPID-101';
    return `SHA256: ${btoa(emp).substring(0, 12).toUpperCase()}-9F82-E74A-SSN`;
  }
}
