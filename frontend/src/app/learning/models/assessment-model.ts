export type AssessmentStatus =
  | 'NOT_ATTEMPTED'
  | 'IN_PROGRESS'
  | 'SUBMITTED'
  | 'PASSED'
  | 'FAILED';

export interface AssessmentAttempt {
  attemptId: string;

  enrollmentId: string;
  learnerId: string;

  courseId: string;
  courseCode: string;
  courseTitle: string;
  passingScore: number;

  moduleId: string;
  moduleTitle: string;
  moduleOrder: number;

  contentId: string;
  contentTitle: string;
  contentType: string;
  contentOrder: number;

  attemptNumber: number;
  status: AssessmentStatus;

  score?: number | null;
  maximumScore?: number | null;
  scorePercentage?: number | null;

  gradedByUserId?: string | null;
  feedback?: string | null;

  startedAt?: string | null;
  submittedAt?: string | null;
  gradedAt?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AssessmentGradeRequest {
  score: number;
  maximumScore: number;
  gradedByUserId?: string | null;
  feedback?: string | null;
}