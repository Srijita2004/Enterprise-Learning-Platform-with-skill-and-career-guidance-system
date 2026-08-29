import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuditLog, BulkEvaluationResult, Certification, CertificationCreateRequest, CertificationStatus,
  ComplianceStatus, ComplianceVerification, Dashboard, LegacySyncResult, NotificationStatus,
  PagedResponse, RenewalNotification, RenewalRequest, ReportSummary, VerificationStatus
} from '../../models/certification.models';

export interface CertificationQuery {
  query?: string; status?: CertificationStatus | ''; verification?: VerificationStatus | '';
  compliance?: ComplianceStatus | ''; expiryBucket?: string; page?: number; size?: number; sort?: string;
}

@Injectable({ providedIn: 'root' })
export class CertificationApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.certificationApiUrl;

  dashboard(): Observable<Dashboard> { return this.http.get<Dashboard>(`${this.base}/dashboard`); }
  search(query: CertificationQuery): Observable<PagedResponse<Certification>> {
    let params = new HttpParams();
    Object.entries(query).forEach(([key, value]) => { if (value !== undefined && value !== null && value !== '') params = params.set(key, String(value)); });
    return this.http.get<PagedResponse<Certification>>(`${this.base}/certifications`, { params });
  }
  get(id: string): Observable<Certification> { return this.http.get<Certification>(`${this.base}/certifications/${id}`); }
  create(request: CertificationCreateRequest): Observable<Certification> { return this.http.post<Certification>(`${this.base}/certifications`, request); }
  update(id: string, request: Omit<CertificationCreateRequest, 'employeeId'>): Observable<Certification> { return this.http.put<Certification>(`${this.base}/certifications/${id}`, request); }
  updateVerification(id: string, status: VerificationStatus): Observable<Certification> { return this.http.patch<Certification>(`${this.base}/certifications/${id}/verification`, { status }); }
  revoke(id: string, reason: string): Observable<Certification> { return this.http.patch<Certification>(`${this.base}/certifications/${id}/revoke`, { reason }); }
  syncM1(): Observable<LegacySyncResult> { return this.http.post<LegacySyncResult>(`${this.base}/certifications/sync/m1`, {}); }
  evaluateLifecycle(): Observable<BulkEvaluationResult> { return this.http.post<BulkEvaluationResult>(`${this.base}/expiry/evaluate`, {}); }
  expiring(days = 30): Observable<Certification[]> { return this.http.get<Certification[]>(`${this.base}/expiry/expiring`, { params: { days } }); }
  renewals(): Observable<RenewalRequest[]> { return this.http.get<RenewalRequest[]>(`${this.base}/renewals`); }
  createRenewal(certificationId: string, proposedExpiryDate: string, justification: string): Observable<RenewalRequest> {
    return this.http.post<RenewalRequest>(`${this.base}/renewals`, { certificationId, proposedExpiryDate, justification });
  }
  approveRenewal(id: string, decisionNote = ''): Observable<RenewalRequest> { return this.http.post<RenewalRequest>(`${this.base}/renewals/${id}/approve`, { decisionNote }); }
  rejectRenewal(id: string, decisionNote = ''): Observable<RenewalRequest> { return this.http.post<RenewalRequest>(`${this.base}/renewals/${id}/reject`, { decisionNote }); }
  notifications(status?: NotificationStatus): Observable<RenewalNotification[]> {
    return this.http.get<RenewalNotification[]>(`${this.base}/notifications`, { params: status ? { status } : {} });
  }
  generateNotifications(): Observable<{ notificationsCreated: number }> { return this.http.post<{ notificationsCreated: number }>(`${this.base}/notifications/generate`, {}); }
  acknowledgeNotification(id: string): Observable<RenewalNotification> { return this.http.patch<RenewalNotification>(`${this.base}/notifications/${id}/acknowledge`, {}); }
  complianceRecent(): Observable<ComplianceVerification[]> { return this.http.get<ComplianceVerification[]>(`${this.base}/compliance`); }
  complianceHistory(certificationId: string): Observable<ComplianceVerification[]> { return this.http.get<ComplianceVerification[]>(`${this.base}/compliance/${certificationId}/history`); }
  verifyCompliance(certificationId: string, result: ComplianceStatus, policyReference: string, evidenceReference: string, notes: string): Observable<ComplianceVerification> {
    return this.http.post<ComplianceVerification>(`${this.base}/compliance/${certificationId}/verify`, { result, policyReference, evidenceReference: evidenceReference || null, notes: notes || null });
  }
  reportSummary(): Observable<ReportSummary> { return this.http.get<ReportSummary>(`${this.base}/reports/summary`); }
  downloadCertificationCsv(): Observable<HttpResponse<Blob>> { return this.http.get(`${this.base}/reports/certifications.csv`, { observe: 'response', responseType: 'blob' }); }
  downloadExpiringCsv(days: number): Observable<HttpResponse<Blob>> { return this.http.get(`${this.base}/reports/expiring.csv`, { params: { days }, observe: 'response', responseType: 'blob' }); }
  audit(): Observable<AuditLog[]> { return this.http.get<AuditLog[]>(`${this.base}/audit`); }
  auditFor(certificationId: string): Observable<AuditLog[]> { return this.http.get<AuditLog[]>(`${this.base}/audit/certifications/${certificationId}`); }
}
