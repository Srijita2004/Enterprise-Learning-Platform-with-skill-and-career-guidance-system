import { Routes } from '@angular/router';

import { authGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then((m) => m.Login)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard)
  },
  {
    path: 'profile',
    canActivate: [authGuard, roleGuard('EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./features/employee-profile/employee-profile').then((m) => m.EmployeeProfile)
  },
  {
    path: 'employees/:id/profile',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/employee-profile/employee-profile').then((m) => m.EmployeeProfile)
  },
  {
    path: 'employee',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/employee/employee').then((m) => m.EmployeeFeature)
  },
  {
    path: 'skill',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/skill/skill').then((m) => m.SkillFeature)
  },
  {
    path: 'assessment',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/assessment/assessment').then((m) => m.AssessmentFeature)
  },
  {
    path: 'competency',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/competency/competency').then((m) => m.CompetencyFeature)
  },
  {
    path: 'certification',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/certification/certification').then((m) => m.CertificationFeature)
  },
  {
    path: 'admin/courses',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/admin/course-management/course-management').then((m) => m.CourseManagement)
  },
  {
    path: 'admin/content',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/admin/content-management/content-management').then((m) => m.ContentManagement)
  },
  {
    path: 'hr/payments',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/hr/payment-verification/payment-verification').then((m) => m.PaymentVerification)
  },
  {
    path: 'hr/learning-paths',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./features/hr/learning-path-management/learning-path-management').then((m) => m.LearningPathManagement)
  },
  {
    path: 'certification-hub',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./pages/m3-dashboard/dashboard').then((m) => m.DashboardPage)
  },
  {
    path: 'certification-registry',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./pages/m3-registry/registry').then((m) => m.RegistryPage)
  },
  {
    path: 'certification-renewals',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./pages/m3-renewals/renewals').then((m) => m.RenewalsPage)
  },
  {
    path: 'certification-compliance',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./pages/m3-compliance/compliance').then((m) => m.CompliancePage)
  },
  {
    path: 'certification-reports-audit',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR')],
    loadComponent: () => import('./pages/m3-reports-audit/reports-audit').then((m) => m.ReportsAuditPage)
  },
  {
    path: 'career-analytics',
    canActivate: [authGuard, roleGuard('ADMIN', 'HR', 'EMPLOYEE')],
    loadComponent: () => import('./pages/career-analytics/career-analytics').then((m) => m.CareerAnalyticsComponent)
  },
  {
    path: 'courses',
    canActivate: [authGuard, roleGuard('LEARNER', 'EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./learning/pages/course-list/course-list').then((m) => m.CourseList)
  },
  {
    path: 'learning-paths',
    canActivate: [authGuard, roleGuard('LEARNER', 'EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./learning/pages/learning-paths/learning-paths').then((m) => m.LearningPaths)
  },
  {
    path: 'course-certificates',
    canActivate: [authGuard],
    loadComponent: () => import('./learning/pages/course-certificates/course-certificates').then((m) => m.CourseCertificates)
  },
  {
    path: 'courses/:courseId/assessments/:contentId',
    canActivate: [authGuard, roleGuard('LEARNER', 'EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./learning/pages/course-assessment/course-assessment').then((m) => m.CourseAssessment)
  },
  {
    path: 'courses/:courseId/learn',
    canActivate: [authGuard, roleGuard('LEARNER', 'EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./learning/pages/course-learning/course-learning').then((m) => m.CourseLearning)
  },
  {
    path: 'courses/:courseId',
    canActivate: [authGuard, roleGuard('LEARNER', 'EMPLOYEE', 'ADMIN', 'HR')],
    loadComponent: () => import('./learning/pages/course-details/course-details').then((m) => m.CourseDetails)
  },
  { path: '**', redirectTo: 'dashboard' }
];
