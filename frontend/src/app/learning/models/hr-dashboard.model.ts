export interface HrDashboard {
  totalLearners: number;
  totalCourses: number;
  publishedCourses: number;
  totalEnrollments: number;
  activeEnrollments: number;
  completedEnrollments: number;
  pendingPayments: number;
  issuedCertificates: number;
  totalLearningPaths: number;
  publishedLearningPaths: number;
  generatedAt?: string | null;
}
