export type ContentProgressStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'COMPLETED';

export interface CourseModule {
  moduleId: string;
  courseId: string;
  courseCode: string;
  courseTitle: string;

  title: string;
  description?: string | null;

  moduleOrder: number;
  published: boolean;

  contentCount?: number | null;
  publishedContentCount?: number | null;
  totalDurationMinutes?: number | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CourseContent {
  contentId: string;

  moduleId: string;
  moduleTitle: string;

  courseId: string;
  courseCode: string;
  courseTitle: string;

  title: string;
  description?: string | null;

  contentType: string;

  contentUrl?: string | null;
  textContent?: string | null;

  durationMinutes?: number | null;
  contentOrder: number;

  mandatory: boolean;
  previewAvailable: boolean;
  published: boolean;

  availableFrom?: string | null;
  availableUntil?: string | null;

  currentlyAvailable: boolean;

  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface ContentProgress {
  progressId: string;

  enrollmentId: string;
  learnerId: string;

  courseId: string;
  courseCode: string;
  courseTitle: string;

  moduleId: string;
  moduleTitle: string;
  moduleOrder: number;

  contentId: string;
  contentTitle: string;
  contentType: string;
  contentOrder: number;

  durationMinutes?: number | null;
  mandatory: boolean;

  status: ContentProgressStatus;
  progressPercentage: number;

  lastPositionSeconds?: number | null;
  timeSpentSeconds?: number | null;

  startedAt?: string | null;
  completedAt?: string | null;
  lastAccessedAt?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CourseProgressSummary {
  enrollmentId: string;
  learnerId: string;

  courseId: string;
  courseCode: string;
  courseTitle: string;

  enrollmentStatus: string;

  totalPublishedContents: number;
  completedContents: number;
  inProgressContents: number;
  notStartedContents: number;

  totalMandatoryContents: number;
  completedMandatoryContents: number;

  overallProgressPercentage: number;
  mandatoryProgressPercentage: number;

  totalTimeSpentSeconds: number;

  lastAccessedAt?: string | null;

  allMandatoryContentsCompleted: boolean;
}

export interface ContentProgressUpdateRequest {
  progressPercentage: number;
  lastPositionSeconds?: number | null;
  additionalTimeSpentSeconds?: number | null;
}

export interface LearningContentView
  extends CourseContent {

  progress?: ContentProgress | null;
}

export interface LearningModuleView {
  module: CourseModule;
  contents: LearningContentView[];
}