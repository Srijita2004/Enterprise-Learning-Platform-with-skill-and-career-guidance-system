export type CertificationStatus = 'VALID' | 'EXPIRING_SOON' | 'EXPIRED' | 'NO_EXPIRY' | 'REVOKED';
export type RenewalStatus = 'NOT_REQUIRED' | 'UPCOMING' | 'DUE' | 'PENDING' | 'COMPLETED' | 'REJECTED' | 'OVERDUE';
export type RenewalRequestStatus = 'PENDING' | 'COMPLETED' | 'REJECTED';
export type VerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type ComplianceStatus = 'PENDING' | 'COMPLIANT' | 'NON_COMPLIANT';
export type NotificationStatus = 'SENT' | 'ACKNOWLEDGED';
export type NotificationType = 'EXPIRING_WITHIN_30_DAYS' | 'EXPIRING_WITHIN_7_DAYS' | 'CERTIFICATION_EXPIRED' | 'RENEWAL_DUE';

export interface Employee { employeeId: number; employeeName: string; designation: string; salary: number; }
export interface Certification {
  certificationId: string; employeeId: number; employeeName: string; certificationName: string;
  issuingOrganization: string; credentialNumber: string | null; issueDate: string; expiryDate: string | null;
  daysRemaining: number | null; status: CertificationStatus; renewalStatus: RenewalStatus;
  verificationStatus: VerificationStatus; complianceStatus: ComplianceStatus; active: boolean;
  warningWindowDays: number; renewalDueDate: string | null; legacyCertificateId: number | null;
  sourceSystem: string; lastEvaluatedAt: string | null; createdAt: string; updatedAt: string;
}
export interface CertificationCreateRequest { employeeId: number; certificationName: string; issuingOrganization: string; credentialNumber?: string | null; issueDate: string; expiryDate?: string | null; warningWindowDays?: number; }
export interface CertificationUpdateRequest { certificationName: string; issuingOrganization: string; credentialNumber?: string | null; issueDate: string; expiryDate?: string | null; warningWindowDays?: number; }
export interface PagedResponse<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean; }
export interface ExpiryBucket { key: string; label: string; count: number; }
export interface Dashboard {
  totalCertifications: number; activeCertifications: number; validCertifications: number; expiringWithin30Days: number;
  expiredCertifications: number; renewalDue: number; pendingRenewals: number; verifiedCertifications: number;
  compliantCertifications: number; renewalRate: number; expiryDistribution: ExpiryBucket[]; upcomingExpirations: Certification[];
}
export interface RenewalRequest {
  renewalRequestId: string; certificationId: string; certificationName: string; employeeId: number; employeeName: string;
  currentExpiryDate: string | null; proposedExpiryDate: string; justification: string; status: RenewalRequestStatus;
  requestedByUserId: string; requestedByRole: string; requestedAt: string; decisionByUserId: string | null;
  decisionByRole: string | null; decisionNote: string | null; decidedAt: string | null; onTime: boolean | null;
}
export interface RenewalNotification {
  notificationId: string; certificationId: string; certificationName: string; employeeName: string;
  type: NotificationType; status: NotificationStatus; message: string; dueDate: string | null; sentAt: string; acknowledgedAt: string | null;
}
export interface ComplianceVerification {
  complianceVerificationId: string; certificationId: string; certificationName: string; employeeId: number; employeeName: string;
  result: ComplianceStatus; policyReference: string; evidenceReference: string | null; notes: string | null;
  verifiedByUserId: string; verifiedByRole: string; verifiedAt: string;
}
export interface ReportSummary {
  total: number; active: number; valid: number; expiringSoon: number; expired: number; verified: number;
  compliant: number; renewalDue: number; completedRenewals: number; rejectedRenewals: number; renewalRate: number; generatedAt: string;
}
export interface AuditLog {
  sequenceNumber: number; auditId: string; certificationId: string | null; action: string; actorUserId: string;
  actorRole: string; details: string; previousHash: string | null; eventHash: string; createdAt: string;
}
export interface LegacySyncResult { discovered: number; imported: number; skippedExisting: number; failed: number; synchronizedAt: string; }
export interface BulkEvaluationResult { processedRecords: number; changedRecords: number; notificationsCreated: number; evaluatedAt: string; }
