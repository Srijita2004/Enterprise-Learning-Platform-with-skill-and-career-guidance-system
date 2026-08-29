export interface LearnerDashboard {
  learnerId: string;

  totalEnrollments: number;
  activeEnrollments: number;
  completedCourses: number;
  pendingEnrollments: number;

  averageCourseProgress: number;

  totalAssessmentAttempts: number;
  passedAssessments: number;
  failedAssessments: number;

  certificatesEarned: number;

  assignedLearningPaths: number;
  inProgressLearningPaths: number;
  completedLearningPaths: number;

  generatedAt?: string | null;
}