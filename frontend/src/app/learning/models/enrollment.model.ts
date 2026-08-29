export type EnrollmentStatus =
  | 'PENDING'
  | 'ACTIVE'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'EXPIRED';

export type PaymentStatus =
  | 'NOT_REQUIRED'
  | 'PENDING'
  | 'PAID'
  | 'FAILED'
  | 'REJECTED'
  | 'REFUNDED';

export type EnrollmentSource =
  | 'SELF_ENROLLED'
  | 'ADMIN_ASSIGNED'
  | 'MANAGER_ASSIGNED'
  | 'LEARNING_PATH_ASSIGNED'
  | 'BULK_IMPORTED';

export interface EnrollmentCreateRequest {
  learnerId: string;
  enrollmentSource: EnrollmentSource;
  assignedByUserId?: string | null;
  paymentReference?: string | null;
  accessExpiresAt?: string | null;
}

export interface Enrollment {
  enrollmentId: string;
  learnerId: string;
  learnerName?: string | null;

  courseId: string;
  courseCode: string;
  courseTitle: string;
  courseType: string;
  courseStatus: string;
  coursePricingType: string;

  status: EnrollmentStatus;
  paymentStatus: PaymentStatus;
  paymentReference?: string | null;
  paymentSubmittedAt?: string | null;
  paymentVerifiedAt?: string | null;
  paymentVerifiedByUserId?: string | null;
  paymentRejectionReason?: string | null;
  enrollmentSource: EnrollmentSource;

  assignedByUserId?: string | null;
  priceAtEnrollment?: number | null;
  currencyCode?: string | null;
  accessAllowed: boolean;

  enrolledAt?: string | null;
  activatedAt?: string | null;
  completedAt?: string | null;
  cancelledAt?: string | null;
  accessExpiresAt?: string | null;
  lastAccessedAt?: string | null;
  cancellationReason?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}
