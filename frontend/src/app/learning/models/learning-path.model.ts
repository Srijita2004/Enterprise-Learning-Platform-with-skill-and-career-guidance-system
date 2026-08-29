export type CourseLevel =
  | 'BEGINNER'
  | 'INTERMEDIATE'
  | 'ADVANCED';

export type LearningPathStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'ARCHIVED';

export type LearningPathAssignmentStatus =
  | 'ASSIGNED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED';

export type LearningPathAssignmentSource =
  | 'SELF_ASSIGNED'
  | 'ADMIN_ASSIGNED'
  | 'MANAGER_ASSIGNED'
  | 'ROLE_BASED_ASSIGNED'
  | 'SYSTEM_RECOMMENDED'
  | 'BULK_IMPORTED';

export interface LearningPath {
  pathId: string;
  pathCode: string;
  title: string;
  description?: string | null;
  category: string;
  targetRole?: string | null;
  level: CourseLevel;
  status: LearningPathStatus;
  estimatedDurationHours?: number | null;
  totalCourses: number;
  requiredCourses: number;
  totalAssignments: number;
  createdByUserId?: string | null;
  publishedAt?: string | null;
  archivedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface LearningPathCreateRequest {
  pathCode: string;
  title: string;
  description?: string | null;
  category: string;
  targetRole?: string | null;
  level: CourseLevel;
  estimatedDurationHours?: number | null;
  createdByUserId?: string | null;
}

export interface LearningPathUpdateRequest {
  title: string;
  description?: string | null;
  category: string;
  targetRole?: string | null;
  level: CourseLevel;
  estimatedDurationHours?: number | null;
}

export interface LearningPathCourse {
  pathCourseId: string;
  pathId: string;
  courseId: string;
  courseCode: string;
  courseTitle: string;
  courseOrder: number;
  requiredForCompletion: boolean;
  unlockAfterPrevious: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface LearningPathCourseRequest {
  courseId: string;
  courseOrder: number;
  requiredForCompletion?: boolean;
  unlockAfterPrevious?: boolean;
}

export interface LearningPathAssignment {
  assignmentId: string;
  pathId: string;
  pathCode: string;
  pathTitle: string;
  learnerId: string;
  status: LearningPathAssignmentStatus;
  assignmentSource: LearningPathAssignmentSource;
  assignedByUserId?: string | null;
  progressPercentage: number;
  currentCourseOrder?: number | null;
  totalCourses: number;
  completedCourses: number;
  overdue: boolean;
  assignedAt?: string | null;
  startedAt?: string | null;
  completedAt?: string | null;
  cancelledAt?: string | null;
  dueAt?: string | null;
  lastAccessedAt?: string | null;
  cancellationReason?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface LearningPathAssignmentRequest {
  learnerId: string;
  assignmentSource?: LearningPathAssignmentSource;
  assignedByUserId?: string | null;
  dueAt?: string | null;
}