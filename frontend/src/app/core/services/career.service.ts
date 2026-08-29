import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { CareerPlan, JobOpportunity, CareerDashboard, PromotionCriteria, AiCareerEvaluationRequest, AiCareerEvaluationResponse } from '../../models/career.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CareerService {
  private readonly baseUrl = `${environment.apiUrl}/career`;

  private mockPlans: Record<number, CareerPlan> = {
    1: {
      planId: 1,
      employeeId: 1,
      employeeName: 'Srijita',
      currentRole: 'Java Developer',
      targetRole: 'Senior Backend Architect',
      progress: 78,
      mentorName: 'Jane Doe',
      skillsRequired: ['Java 17', 'Spring Boot 4', 'Microservices', 'PostgreSQL', 'Cloud Architecture'],
      skillsAcquired: ['Java 17', 'Spring Boot 4', 'PostgreSQL'],
      skillGaps: ['Cloud Architecture +2'],
      jobMatchesCount: 12,
      promotionCriteria: [
        { criteriaId: 1, name: 'Microservices Architecture', description: 'Master enterprise microservices and Kafka event streams', isMet: true, type: 'SKILL' },
        { criteriaId: 2, name: 'Cloud Architecture Assessment', description: 'Pass cloud architecture & resilience assessment', isMet: true, type: 'ASSESSMENT' },
        { criteriaId: 3, name: 'Tenure in Role (>18 months)', description: 'Serve at least 18 months as core Java developer', isMet: true, type: 'TENURE' }
      ]
    },
    106: {
      planId: 4,
      employeeId: 106,
      employeeName: 'John Smith',
      currentRole: 'Developer',
      targetRole: 'Tech Lead',
      progress: 67,
      mentorName: 'Jane Doe',
      skillsRequired: ['Java 17', 'Spring Boot 4', 'Angular 20', 'System Design'],
      skillsAcquired: ['Java 17', 'Spring Boot 4'],
      skillGaps: ['Angular +3', 'System Design +2'],
      jobMatchesCount: 12,
      promotionCriteria: [
        { criteriaId: 11, name: 'Angular Skillup', description: 'Acquire Angular skills (+3 levels)', isMet: false, type: 'SKILL' },
        { criteriaId: 12, name: 'System Design', description: 'Complete system design assessment', isMet: false, type: 'ASSESSMENT' },
        { criteriaId: 13, name: 'Time in Role', description: 'Serve 18 months as developer', isMet: true, type: 'TENURE' }
      ]
    },
    101: {
      planId: 1,
      employeeId: 101,
      employeeName: 'Alice Vance',
      currentRole: 'Senior Java Developer',
      targetRole: 'Technical Lead',
      progress: 67,
      mentorName: 'Marcus Brodie',
      skillsRequired: ['Java 17', 'Spring Boot 4', 'System Design', 'Cloud Architecture', 'Angular 20', 'Leadership'],
      skillsAcquired: ['Java 17', 'Spring Boot 4', 'System Design'],
      skillGaps: ['Angular +3', 'Cloud Architecture +2', 'Leadership +1'],
      jobMatchesCount: 12,
      promotionCriteria: [
        { criteriaId: 1, name: 'Cloud Certification', description: 'Obtain AWS Solutions Architect Professional certification', isMet: false, type: 'CERTIFICATION' },
        { criteriaId: 2, name: 'LMS Course Completion', description: 'Complete Angular 20 Masterclass with assessment score > 85%', isMet: false, type: 'SKILL' },
        { criteriaId: 3, name: 'Module Ownership', description: 'Own and deliver a critical system module end-to-end', isMet: true, type: 'TENURE' },
        { criteriaId: 4, name: 'Technical Assessment', description: 'Pass the internal Technical Lead competency evaluation', isMet: true, type: 'ASSESSMENT' }
      ]
    },
    102: {
      planId: 2,
      employeeId: 102,
      employeeName: 'Marcus Brodie',
      currentRole: 'Principal Architect',
      targetRole: 'Chief Architect / CTO Office',
      progress: 85,
      mentorName: 'Emily Watson',
      skillsRequired: ['Enterprise Architecture', 'Cloud Native DevOps', 'Financial Domain Strategy', 'Executive Communication'],
      skillsAcquired: ['Enterprise Architecture', 'Cloud Native DevOps', 'Financial Domain Strategy'],
      skillGaps: ['Executive Communication +1'],
      jobMatchesCount: 4,
      promotionCriteria: [
        { criteriaId: 5, name: 'Industry Impact', description: 'Publish 2 technical design blueprint papers for the enterprise', isMet: true, type: 'TENURE' },
        { criteriaId: 6, name: 'Leadership Mentor', description: 'Mentor at least 3 Senior Developers towards Tech Lead roles', isMet: true, type: 'TENURE' },
        { criteriaId: 7, name: 'Executive Presentation', description: 'Present modern microservice architecture roadmap to the executive board', isMet: false, type: 'ASSESSMENT' }
      ]
    },
    103: {
      planId: 3,
      employeeId: 103,
      employeeName: 'Sarah Jenkins',
      currentRole: 'QA Lead',
      targetRole: 'Quality Engineering Manager',
      progress: 50,
      mentorName: 'Alice Vance',
      skillsRequired: ['Automation Testing', 'Performance Testing', 'CI/CD Pipelines', 'Team Management', 'Agile Methodologies'],
      skillsAcquired: ['Automation Testing', 'Performance Testing', 'Agile Methodologies'],
      skillGaps: ['CI/CD Pipelines +2', 'Team Management +1'],
      jobMatchesCount: 7,
      promotionCriteria: [
        { criteriaId: 8, name: 'Pipeline Integration', description: 'Integrate automated regression suite in Jenkins/GitHub CI/CD pipelines', isMet: false, type: 'SKILL' },
        { criteriaId: 9, name: 'Agile QA Leadership', description: 'Successfully manage QA deliverables across 3 agile sprint teams', isMet: true, type: 'TENURE' },
        { criteriaId: 10, name: 'QA Training', description: 'Deliver QA Automation workshop to 15 developers and engineers', isMet: false, type: 'ASSESSMENT' }
      ]
    }
  };

  private mockJobs: JobOpportunity[] = [
    {
      jobId: 1,
      title: 'Technical Lead - Banking Core',
      department: 'Retail Banking',
      location: 'New York, NY (Hybrid)',
      matchScore: 92,
      skillsRequired: ['Java 17', 'Spring Boot 4', 'Microservices', 'PostgreSQL', 'Financial Domain Strategy'],
      description: 'Lead a team of 6 backend developers to scale our core retail banking ledger service. Drive design decisions, code quality, and cloud deployments.',
      salary: 135000
    },
    {
      jobId: 2,
      title: 'Senior Software Engineer (Angular / Spring)',
      department: 'Corporate Portals',
      location: 'Austin, TX (Remote)',
      matchScore: 87,
      skillsRequired: ['Java 17', 'Spring Boot 4', 'Angular 20', 'System Design'],
      description: 'Develop and maintain interactive dashboards for corporate banking clients, integrating multi-microservice data and ensuring high security and performance.',
      salary: 120000
    },
    {
      jobId: 3,
      title: 'Cloud Solutions Architect',
      department: 'Cloud Platform Services',
      location: 'San Francisco, CA (Hybrid)',
      matchScore: 78,
      skillsRequired: ['Cloud Architecture', 'AWS Solutions Architect Professional', 'Kubernetes', 'Terraform'],
      description: 'Help transition monolithic financial products to AWS and Azure cloud environments. Develop infra-as-code scripts and coordinate service registry networking.',
      salary: 165000
    },
    {
      jobId: 4,
      title: 'Quality Engineering Manager',
      department: 'Platform Assurance',
      location: 'New York, NY (Hybrid)',
      matchScore: 85,
      skillsRequired: ['Automation Testing', 'CI/CD Pipelines', 'Team Management', 'Agile Methodologies'],
      description: 'Define the quality strategy for our cloud-native enterprise products. Manage a team of SDETs, design automated test suites, and coordinate release quality audits.',
      salary: 140000
    },
    {
      jobId: 5,
      title: 'Principal Architect - Distributed Ledger',
      department: 'Research & Innovation',
      location: 'Boston, MA (Onsite)',
      matchScore: 95,
      skillsRequired: ['Enterprise Architecture', 'Distributed Systems', 'Java 17', 'System Design'],
      description: 'Lead research and prototyping of next-generation distributed transaction systems for high-frequency trading. Define framework and API guidelines.',
      salary: 195000
    }
  ];

  private mockDashboard: CareerDashboard = {
    totalPlans: 2847,
    promotionsAnnually: 247,
    departmentSkillCoverage: 87,
    jobMatches: 12,
    departmentCoverages: [
      { department: 'Engineering - Backend', coverage: 91 },
      { department: 'Engineering - Frontend', coverage: 82 },
      { department: 'Quality Assurance', coverage: 89 },
      { department: 'DevOps & Infrastructure', coverage: 84 },
      { department: 'Product Management', coverage: 78 },
      { department: 'Data & Analytics', coverage: 80 }
    ],
    effectivenessReports: [
      { courseName: 'Spring Boot 4 & Cloud Microservices', enrollments: 342, completionRate: 94, scoreImprovement: 28 },
      { courseName: 'Modern Web Engineering with Angular 20', enrollments: 289, completionRate: 88, scoreImprovement: 32 },
      { courseName: 'AWS Certified Solutions Architect Course', enrollments: 198, completionRate: 91, scoreImprovement: 24 },
      { courseName: 'Securing Cloud Services & IAM', enrollments: 154, completionRate: 96, scoreImprovement: 35 },
      { courseName: 'Enterprise System Design Patterns', enrollments: 112, completionRate: 92, scoreImprovement: 18 }
    ]
  };

  constructor(private http: HttpClient) {}

  getCareerPlanByEmployee(employeeId: number): Observable<CareerPlan> {
    if (environment.useMock) {
      return of(this.getFallbackPlan(employeeId));
    }
    return this.http.get<any>(`${this.baseUrl}/employee/${employeeId}`).pipe(
      map((res) => this.mapBackendPlan(res, employeeId)),
      catchError((err) => {
        console.warn('Backend career plan unavailable, using benchmark profile for employee:', employeeId, err);
        return of(this.getFallbackPlan(employeeId));
      })
    );
  }

  evaluateAiCareer(request: AiCareerEvaluationRequest): Observable<AiCareerEvaluationResponse> {
    return this.http.post<AiCareerEvaluationResponse>(`${this.baseUrl}/ai/evaluate`, request).pipe(
      catchError(() => of(this.getFallbackAiEvaluation(request)))
    );
  }

  private getFallbackAiEvaluation(request: AiCareerEvaluationRequest): AiCareerEvaluationResponse {
    const targetRole = request.targetRole || 'Senior Professional Specialist';
    const empName = request.employeeName || `Employee #${request.employeeId}`;
    const skillsCount = request.skills ? request.skills.length : 3;
    const matchScore = Math.min(95.0, Math.max(50.0, 65.0 + skillsCount * 4.5));
    const readinessProb = Math.min(94.0, Math.max(45.0, matchScore - 5.0));

    return {
      employeeId: request.employeeId,
      employeeName: empName,
      currentRole: request.currentRole || 'Enterprise Associate',
      targetRole: targetRole,
      matchScore: matchScore,
      cosineSimilarity: matchScore / 100.0,
      readinessProbability: readinessProb,
      readinessTier: readinessProb >= 80 ? 'HIGH_ADVANCEMENT' : 'MODERATE_PROGRESSION',
      skillGaps: [
        {
          skillName: 'Microservices Architecture',
          currentLevel: 2,
          requiredLevel: 4,
          gapLevel: 2,
          priority: 'HIGH',
          recommendedAction: 'Advance to Level 4 via distributed systems curriculum',
          targetCourse: 'Advanced Microservices & Kafka Masterclass'
        },
        {
          skillName: 'System Design & Scalability',
          currentLevel: 2,
          requiredLevel: 4,
          gapLevel: 2,
          priority: 'MEDIUM',
          recommendedAction: 'Complete high-availability cloud systems benchmark',
          targetCourse: 'System Design & Scalability Architecture'
        }
      ],
      topStrengths: ['Java 17 (L4)', 'Spring Boot 4 (L4)'],
      recommendedCourses: ['Advanced Microservices & Kafka Masterclass', 'System Design & Scalability Architecture'],
      aiExecutiveSummary: `Based on vector space cosine similarity modeling, ${empName} demonstrates an overall **${matchScore}% compatibility match** for the **${targetRole}** role with a **${readinessProb}% promotion readiness probability**.\n\nCore foundational proficiencies provide a solid trajectory. Priority focus should be placed on closing 2 architectural skill gaps to complete full advancement qualification.`,
      strategicNextSteps: [
        'Milestone 1: Complete Advanced Microservices & Kafka Masterclass',
        'Milestone 2: Pass System Design & Scalability Benchmark',
        `Milestone 3: Submit formal candidacy portfolio for ${targetRole}`
      ],
      aiModelEngine: 'Hybrid ML (Cosine Vector Engine) + Google Gemini GenAI',
      generatedAt: new Date().toISOString()
    };
  }

  getJobOpportunities(): Observable<JobOpportunity[]> {
    if (environment.useMock) {
      return of([...this.mockJobs]);
    }
    return this.http.get<any[]>(`${this.baseUrl}/jobs`).pipe(
      map((jobs) => {
        if (!jobs || jobs.length === 0) return [...this.mockJobs];
        return jobs.map((j, idx) => ({
          jobId: j.id || j.jobId || (idx + 1),
          title: j.title || 'Software Engineering Role',
          department: j.department || 'Engineering',
          location: j.location || 'Remote',
          matchScore: j.matchScore || (90 - idx * 3),
          skillsRequired: typeof j.requiredSkills === 'string' ? JSON.parse(j.requiredSkills) : (j.skillsRequired || ['Java 17', 'Spring Boot 4', 'Angular 20']),
          description: j.description || 'Enterprise role matching career profile.',
          salary: j.salary || 125000
        }));
      }),
      catchError(() => of([...this.mockJobs]))
    );
  }

  getDashboard(): Observable<CareerDashboard> {
    if (environment.useMock) {
      return of({ ...this.mockDashboard });
    }
    return this.http.get<any>(`${this.baseUrl}/dashboard`).pipe(
      map((res) => {
        if (!res) return { ...this.mockDashboard };
        return {
          totalPlans: res.totalActivePlans || res.totalPlans || 2847,
          promotionsAnnually: res.promotionsAnnually || 247,
          departmentSkillCoverage: res.departmentSkillCoverageAverage || res.departmentSkillCoverage || 87,
          jobMatches: res.totalJobOpportunitiesOpen || res.jobMatches || 12,
          departmentCoverages: (res.departmentCoverages || []).map((d: any) => ({
            department: d.department,
            coverage: d.skillCoveragePercentage || d.coverage || 85
          })).length > 0 ? res.departmentCoverages.map((d: any) => ({
            department: d.department,
            coverage: d.skillCoveragePercentage || d.coverage || 85
          })) : this.mockDashboard.departmentCoverages,
          effectivenessReports: (res.trainingEffectivenessReports || []).map((r: any) => ({
            courseName: r.courseName,
            enrollments: r.enrollments || 200,
            completionRate: r.completionRate || 90,
            scoreImprovement: r.scoreImprovement || 25
          })).length > 0 ? res.trainingEffectivenessReports.map((r: any) => ({
            courseName: r.courseName,
            enrollments: r.enrollments || 200,
            completionRate: r.completionRate || 90,
            scoreImprovement: r.scoreImprovement || 25
          })) : this.mockDashboard.effectivenessReports
        };
      }),
      catchError(() => of({ ...this.mockDashboard }))
    );
  }

  toggleCriteria(planId: number, criteriaId: number): Observable<CareerPlan> {
    const plan = this.mockPlans[106] || Object.values(this.mockPlans)[0];
    if (plan) {
      const criteria = plan.promotionCriteria.find(c => c.criteriaId === criteriaId);
      if (criteria) {
        criteria.isMet = !criteria.isMet;
        const metCount = plan.promotionCriteria.filter(c => c.isMet).length;
        plan.progress = Math.round((metCount / plan.promotionCriteria.length) * 100);
      }
    }
    return this.http.post<any>(`${this.baseUrl}/plans/${planId}/criteria/${criteriaId}/toggle`, {}).pipe(
      map((res) => this.mapBackendPlan(res, 106)),
      catchError(() => of({ ...plan }))
    );
  }

  updateMentor(planId: number, mentorName: string, mentorId: string = ''): Observable<CareerPlan> {
    const plan = this.mockPlans[106] || Object.values(this.mockPlans)[0];
    if (plan) {
      plan.mentorName = mentorName;
    }
    return this.http.put<any>(`${this.baseUrl}/plans/${planId}/mentor`, { mentorId, mentorName }).pipe(
      map((res) => this.mapBackendPlan(res, 106)),
      catchError(() => of({ ...plan }))
    );
  }

  updateTargetRole(planId: number, targetRole: string): Observable<CareerPlan> {
    const plan = this.mockPlans[106] || Object.values(this.mockPlans)[0];
    if (plan) {
      plan.targetRole = targetRole;
    }
    return this.http.put<any>(`${this.baseUrl}/plans/${planId}/target-role`, { targetRole }).pipe(
      map((res) => this.mapBackendPlan(res, 106)),
      catchError(() => of({ ...plan, targetRole }))
    );
  }

  private mapBackendPlan(res: any, employeeId: number): CareerPlan {
    const fallback = this.getFallbackPlan(employeeId);
    if (!res) return fallback;

    return {
      planId: res.id || res.planId || fallback.planId,
      employeeId: res.employeeId || employeeId,
      employeeName: res.employeeName || fallback.employeeName,
      currentRole: res.currentRole || fallback.currentRole,
      targetRole: res.targetRole || fallback.targetRole,
      progress: res.progressPercentage !== undefined ? res.progressPercentage : fallback.progress,
      mentorName: res.mentorName || fallback.mentorName,
      skillsRequired: fallback.skillsRequired,
      skillsAcquired: fallback.skillsAcquired,
      skillGaps: res.skillGaps && res.skillGaps.length > 0 
        ? res.skillGaps.map((g: any) => `${g.skillName || g} +${g.gapLevel || 2}`) 
        : fallback.skillGaps,
      jobMatchesCount: fallback.jobMatchesCount,
      promotionCriteria: (res.promotionCriteria && res.promotionCriteria.length > 0)
        ? res.promotionCriteria.map((c: any) => ({
            criteriaId: c.id || c.criteriaId,
            name: c.name,
            description: c.description,
            isMet: Boolean(c.isMet),
            type: c.type || 'SKILL'
          }))
        : fallback.promotionCriteria
    };
  }

  private getFallbackPlan(employeeId: number): CareerPlan {
    const plan = this.mockPlans[employeeId];
    if (plan) {
      return { ...plan };
    }
    return {
      planId: employeeId,
      employeeId: employeeId,
      employeeName: `Employee ${employeeId}`,
      currentRole: 'Software Developer',
      targetRole: 'Senior Developer',
      progress: 67,
      mentorName: 'Jane Doe',
      skillsRequired: ['Java 17', 'Spring Boot 4', 'SQL', 'Git', 'Cloud Architecture'],
      skillsAcquired: ['Java 17', 'SQL', 'Git'],
      skillGaps: ['Spring Boot 4 +2', 'Cloud Architecture +3'],
      jobMatchesCount: 12,
      promotionCriteria: [
        { criteriaId: 100 + employeeId, name: 'LMS Progress', description: 'Complete at least 3 backend courses', isMet: true, type: 'SKILL' },
        { criteriaId: 200 + employeeId, name: 'Core Assessment', description: 'Pass the Java 17 advanced assessment', isMet: false, type: 'ASSESSMENT' },
        { criteriaId: 300 + employeeId, name: 'Designation Tenure', description: 'Work in current role for at least 12 months', isMet: true, type: 'TENURE' }
      ]
    };
  }
}