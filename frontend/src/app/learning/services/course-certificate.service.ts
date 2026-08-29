import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  CertificateIssueRequest,
  CertificateRevocationRequest,
  CertificateValidityResponse,
  CourseCertificate
} from '../models/course-certificate.model';

@Injectable({
  providedIn: 'root'
})
export class CourseCertificateService {

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    environment.learningApiUrl;

  issueCertificate(
    completionId: string,
    request: CertificateIssueRequest
  ): Observable<CourseCertificate> {
    return this.http.post<CourseCertificate>(
      `${this.apiUrl}/course-completions/${completionId}/certificate`,
      request
    );
  }

  getCertificateById(
    certificateId: string
  ): Observable<CourseCertificate> {
    return this.http.get<CourseCertificate>(
      `${this.apiUrl}/certificates/${certificateId}`
    );
  }

  getCertificateByCompletion(
    completionId: string
  ): Observable<CourseCertificate> {
    return this.http.get<CourseCertificate>(
      `${this.apiUrl}/course-completions/${completionId}/certificate`
    );
  }

  getCertificateByNumber(
    certificateNumber: string
  ): Observable<CourseCertificate> {
    return this.http.get<CourseCertificate>(
      `${this.apiUrl}/certificates/number/${encodeURIComponent(
        certificateNumber
      )}`
    );
  }

  verifyCertificate(
    verificationCode: string
  ): Observable<CourseCertificate> {
    return this.http.get<CourseCertificate>(
      `${this.apiUrl}/certificates/verification/${verificationCode}`
    );
  }

  checkCertificateValidity(
    verificationCode: string
  ): Observable<CertificateValidityResponse> {
    return this.http.get<CertificateValidityResponse>(
      `${this.apiUrl}/certificates/verification/${verificationCode}/valid`
    );
  }

  getAllCertificates(): Observable<CourseCertificate[]> {
    return this.http.get<CourseCertificate[]>(
      `${this.apiUrl}/certificates`
    );
  }

  getCertificatesByLearner(
    learnerId: string
  ): Observable<CourseCertificate[]> {
    return this.http.get<CourseCertificate[]>(
      `${this.apiUrl}/learners/${learnerId}/certificates`
    );
  }

  getCertificatesByCourse(
    courseId: string
  ): Observable<CourseCertificate[]> {
    return this.http.get<CourseCertificate[]>(
      `${this.apiUrl}/courses/${courseId}/certificates`
    );
  }

  revokeCertificate(
    certificateId: string,
    request: CertificateRevocationRequest
  ): Observable<CourseCertificate> {
    return this.http.patch<CourseCertificate>(
      `${this.apiUrl}/certificates/${certificateId}/revoke`,
      request
    );
  }

  downloadCertificatePdf(
    certificateId: string
  ): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/certificates/${certificateId}/pdf`,
      {
        responseType: 'blob'
      }
    );
  }
}