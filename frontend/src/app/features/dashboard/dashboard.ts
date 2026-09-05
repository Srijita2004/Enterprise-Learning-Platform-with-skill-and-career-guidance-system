import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import {
  DatePipe,
  DecimalPipe,
  KeyValuePipe,
  JsonPipe
} from '@angular/common';

import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import {
  EmployeeService
} from '../../core/services/employee.service';

import {
  SkillService
} from '../../core/services/skill.service';

import {
  EmployeeSkillService
} from '../../core/services/employee-skill.service';

import {
  AssessmentService
} from '../../core/services/assessment.service';

import {
  CertificationService
} from '../../core/services/certification.service';

import {
  CompetencyService
} from '../../core/services/competency.service';

import {
  LearnerDashboard
} from '../../learning/models/learner-dashboard.model';

import { HrDashboard } from '../../learning/models/hr-dashboard.model';

import {
  LearnerDashboardService
} from '../../learning/services/learner-dashboard.service';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    KeyValuePipe,
    DecimalPipe,
    DatePipe,
    JsonPipe,
    RouterLink
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private readonly empService =
    inject(EmployeeService);

  private readonly skillService =
    inject(SkillService);

  private readonly empSkillService =
    inject(EmployeeSkillService);

  private readonly assessmentService =
    inject(AssessmentService);

  private readonly certService =
    inject(CertificationService);

  private readonly competencyService =
    inject(CompetencyService);

  private readonly learnerDashboardService =
    inject(LearnerDashboardService);

  readonly authService = inject(AuthService);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  // M1 Dashboard Stats
  readonly employeeCount = signal(0);

  readonly skillCount = signal(0);

  readonly assessmentCount = signal(0);

  readonly activeCertCount = signal(0);

  readonly avgAssessmentScore = signal(0);

  readonly competencyCount = signal(0);

  readonly skillProficiencies =
    signal<{ [level: string]: number }>({});

  readonly skillCategories =
    signal<{ [category: string]: number }>({});

  readonly statusLoading = signal(true);

  readonly systemDashboardError = signal('');

  readonly hrDashboard = signal<HrDashboard | null>(null);
  readonly hrDashboardLoading = signal(false);
  readonly hrDashboardError = signal('');

  // M2 Learner Dashboard
  readonly learnerDashboard =
    signal<LearnerDashboard | null>(null);

  readonly learningStatusLoading =
    signal(true);

  readonly learningDashboardError =
    signal('');

  ngOnInit(): void {
    if (this.authService.hasRole('ADMIN', 'HR')) {
      this.fetchDashboardData();
      this.fetchHrDashboardData();
    } else {
      this.statusLoading.set(false);
    }

    if (this.authService.hasRole('LEARNER', 'EMPLOYEE')) {
      this.fetchLearnerDashboardData();
    } else {
      this.learningStatusLoading.set(false);
    }
  }

  fetchDashboardData(): void {
    this.statusLoading.set(true);

    this.systemDashboardError.set('');

    forkJoin({
      employees:
        this.empService.getAll(),

      skills:
        this.skillService.getAll(),

      empSkills:
        this.empSkillService.getAll(),

      assessments:
        this.assessmentService.getAll(),

      certs:
        this.certService.getAll(),

      competencies:
        this.competencyService.getAll()
    }).subscribe({
      next: (data) => {
        this.employeeCount.set(
          data.employees.length
        );

        this.skillCount.set(
          data.skills.length
        );

        this.assessmentCount.set(
          data.assessments.length
        );

        this.competencyCount.set(
          data.competencies.length
        );

        if (data.assessments.length > 0) {
          const total =
            data.assessments.reduce(
              (sum, current) =>
                sum + current.score,
              0
            );

          this.avgAssessmentScore.set(
            Math.round(
              total /
              data.assessments.length
            )
          );
        } else {
          this.avgAssessmentScore.set(0);
        }

        const activeCerts =
          data.certs.filter(
            (certificate) =>
              certificate.status === 'Valid'
          ).length;

        this.activeCertCount.set(
          activeCerts
        );

        const levels:
          { [key: string]: number } = {
            '1': 0,
            '2': 0,
            '3': 0,
            '4': 0,
            '5': 0
          };

        data.empSkills.forEach(
          (employeeSkill) => {
            const level =
              employeeSkill
                .proficiencyLevel
                .toString();

            if (
              levels[level] !== undefined
            ) {
              levels[level]++;
            }
          }
        );

        this.skillProficiencies.set(
          levels
        );

        const categories:
          { [key: string]: number } = {};

        data.skills.forEach((skill) => {
          const category =
            skill.category ||
            'TECHNICAL';

          categories[category] =
            (categories[category] || 0) + 1;
        });

        this.skillCategories.set(
          categories
        );

        this.statusLoading.set(false);
      },

      error: (error: unknown) => {
        console.error(
          'Error fetching system dashboard stats:',
          error
        );

        this.statusLoading.set(false);

        this.systemDashboardError.set(
          'Organization statistics could not be loaded.'
        );
      }
    });
  }

  fetchHrDashboardData(): void {
    this.hrDashboardLoading.set(true);
    this.hrDashboardError.set('');

    this.learnerDashboardService.getHrDashboard().subscribe({
      next: (dashboard) => {
        this.hrDashboard.set(dashboard);
        this.hrDashboardLoading.set(false);
      },
      error: (error: unknown) => {
        console.error('Error fetching HR dashboard:', error);
        this.hrDashboardError.set('Learning operations statistics could not be loaded.');
        this.hrDashboardLoading.set(false);
      }
    });
  }

  fetchLearnerDashboardData(): void {
    this.learningStatusLoading.set(true);
    this.learningDashboardError.set('');

    const emptyLearnerDashboard: LearnerDashboard = {
      learnerId: this.learnerId || '',
      totalEnrollments: 0,
      activeEnrollments: 0,
      completedCourses: 0,
      pendingEnrollments: 0,
      averageCourseProgress: 0,
      totalAssessmentAttempts: 0,
      passedAssessments: 0,
      failedAssessments: 0,
      certificatesEarned: 0,
      assignedLearningPaths: 0,
      inProgressLearningPaths: 0,
      completedLearningPaths: 0
    };

    if (!this.learnerId) {
      this.learnerDashboard.set(emptyLearnerDashboard);
      this.learningStatusLoading.set(false);
      return;
    }

    const syncLocalCounts = (d: LearnerDashboard) => {
      try {
        const stored = JSON.parse(localStorage.getItem('ssn_learning_path_assignments') || '[]');
        const matches = stored.filter((a: any) => a.learnerId === this.learnerId);
        if (matches.length > 0) {
          d.assignedLearningPaths = matches.length;
          d.inProgressLearningPaths = matches.filter((a: any) => a.status === 'IN_PROGRESS').length;
          d.completedLearningPaths = matches.filter((a: any) => a.status === 'COMPLETED').length;
        }
      } catch {}
      return d;
    };

    this.learnerDashboardService
      .getLearnerDashboard(this.learnerId)
      .subscribe({
        next: (dashboard) => {
          this.learnerDashboard.set(syncLocalCounts(dashboard));
          this.learningStatusLoading.set(false);
        },
        error: (error: unknown) => {
          console.error('Error fetching learner dashboard:', error);
          this.learnerDashboard.set(syncLocalCounts(emptyLearnerDashboard));
          this.learningStatusLoading.set(false);
        }
      });
  }

  getAssessmentPassRate(): number {
    const dashboard =
      this.learnerDashboard();

    if (
      !dashboard ||
      dashboard.totalAssessmentAttempts === 0
    ) {
      return 0;
    }

    return Math.round(
      dashboard.passedAssessments /
      dashboard.totalAssessmentAttempts *
      100
    );
  }

  getLearningPathCompletionRate(): number {
    const dashboard =
      this.learnerDashboard();

    if (
      !dashboard ||
      dashboard.assignedLearningPaths === 0
    ) {
      return 0;
    }

    return Math.round(
      dashboard.completedLearningPaths /
      dashboard.assignedLearningPaths *
      100
    );
  }

  // =========================================================
  // 🌟 KAFKA ARCHITECTURE EVENT STREAM INSPECTOR STATE
  // =========================================================
  readonly showKafkaInspector = signal<boolean>(false);
  readonly selectedKafkaEvent = signal<any | null>(null);
  readonly kafkaEvents = signal<any[]>([
    {
      eventId: 'evt-kfk-9042',
      eventType: 'PAYMENT_VERIFIED',
      topic: 'skillsphere.learning.payments',
      sourceService: 'LEARNING-SERVICE (Port 8082)',
      partition: 0,
      offset: 1042,
      timestamp: '2026-08-31T22:20:15Z',
      status: 'ACKNOWLEDGED',
      payload: {
        transactionRef: '#TXN-98421-WIRE',
        learnerId: '101',
        learnerName: 'Alex Vance',
        courseCode: 'CRS-SPRING-01',
        amountPaid: 450.00,
        currency: 'USD',
        status: 'PAID',
        verifiedBy: 'HR_MANAGER'
      }
    },
    {
      eventId: 'evt-kfk-9041',
      eventType: 'CERTIFICATE_ISSUED',
      topic: 'skillsphere.certification.events',
      sourceService: 'CERTIFICATION-MANAGEMENT-SERVICE (Port 8083)',
      partition: 1,
      offset: 874,
      timestamp: '2026-08-31T22:15:30Z',
      status: 'ACKNOWLEDGED',
      payload: {
        certificateId: 'CERT-94120-AWS',
        employeeId: 1,
        employeeName: 'Srijita',
        certificationName: 'AWS Certified Solutions Architect - Professional',
        issuingAuthority: 'Amazon Web Services',
        validityYears: 3,
        complianceImpact: '+4.2% Department Rating'
      }
    },
    {
      eventId: 'evt-kfk-9040',
      eventType: 'PROMOTION_NOMINATED',
      topic: 'skillsphere.career.nominations',
      sourceService: 'CAREER-SERVICE (Port 8087)',
      partition: 0,
      offset: 319,
      timestamp: '2026-08-31T22:05:10Z',
      status: 'ACKNOWLEDGED',
      payload: {
        nominationId: 'NOM-3012',
        candidateId: 1,
        candidateName: 'Srijita',
        targetRole: 'Staff Cloud & Microservices Architect',
        fitScorePercentage: 97,
        nominatedBy: 'HR_MANAGER',
        status: 'SUBMITTED_FOR_EXECUTIVE_APPROVAL'
      }
    },
    {
      eventId: 'evt-kfk-9039',
      eventType: 'SKILL_COMPETENCY_EVALUATED',
      topic: 'skillsphere.skill.assessments',
      sourceService: 'SKILL-MANAGEMENT-SERVICE (Port 8081)',
      partition: 2,
      offset: 2490,
      timestamp: '2026-08-31T21:50:00Z',
      status: 'ACKNOWLEDGED',
      payload: {
        assessmentId: 'ASM-882',
        employeeId: 101,
        employeeName: 'Alex Vance',
        competencyName: 'Cloud Native & Kubernetes',
        levelEvaluated: 5,
        benchmarkScore: 96,
        status: 'BENCHMARK_EXCEEDED'
      }
    },
    {
      eventId: 'evt-kfk-9038',
      eventType: 'LEARNING_PATH_ASSIGNED',
      topic: 'skillsphere.learning.assignments',
      sourceService: 'LEARNING-SERVICE (Port 8082)',
      partition: 1,
      offset: 615,
      timestamp: '2026-08-31T21:30:00Z',
      status: 'ACKNOWLEDGED',
      payload: {
        assignmentId: 'ASG-4401',
        pathCode: 'LP-ARCH-01',
        pathTitle: 'Full-Stack Enterprise Cloud Architecture Track',
        learnerId: '1',
        targetRole: 'Senior Backend Architect',
        assignedBy: 'HR_DIRECTOR'
      }
    }
  ]);

  openKafkaInspector(): void {
    this.selectedKafkaEvent.set(this.kafkaEvents()[0]);
    this.showKafkaInspector.set(true);
  }

  closeKafkaInspector(): void {
    this.showKafkaInspector.set(false);
  }

  selectKafkaEvent(evt: any): void {
    this.selectedKafkaEvent.set(evt);
  }
}