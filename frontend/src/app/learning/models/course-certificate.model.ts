export type CertificateStatus =
  | 'ISSUED'
  | 'REVOKED';

export interface CourseCertificate {
  certificateId: string;

  completionId: string;
  enrollmentId: string;
  learnerId: string;

  courseId: string;
  courseCode: string;
  courseTitle: string;

  recipientName: string;

  certificateNumber: string;
  verificationCode: string;

  status: CertificateStatus;
  valid: boolean;

  issuedByUserId?: string | null;
  revokedByUserId?: string | null;

  fileUrl?: string | null;

  courseCompletedAt?: string | null;
  issuedAt?: string | null;
  revokedAt?: string | null;

  revocationReason?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CertificateIssueRequest {
  recipientName: string;
  issuedByUserId?: string | null;
  fileUrl?: string | null;
}

export interface CertificateRevocationRequest {
  revokedByUserId: string;
  revocationReason: string;
}

export interface CertificateValidityResponse {
  verificationCode: string;
  valid: boolean;
}