import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import { RouterLink } from '@angular/router';

import {
  CourseCertificate
} from '../../models/course-certificate.model';

import {
  CourseCertificateService
} from '../../services/course-certificate.service';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-course-certificates',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './course-certificates.html',
  styleUrls: ['./course-certificates.css']
})
export class CourseCertificates implements OnInit {

  private readonly certificateService =
    inject(CourseCertificateService);

  private readonly changeDetector =
    inject(ChangeDetectorRef);

  readonly authService = inject(AuthService);

  get learnerId(): string {
    return this.authService.getLearnerId();
  }

  certificates: CourseCertificate[] = [];

  isLoading = true;

  verifyingCertificateId: string | null = null;

  viewingCertificateId: string | null = null;

  downloadingCertificateId: string | null = null;

  errorMessage = '';

  actionMessage = '';

  ngOnInit(): void {
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    const certificateRequest = this.authService.hasRole('ADMIN', 'HR')
      ? this.certificateService.getAllCertificates()
      : this.certificateService.getCertificatesByLearner(this.learnerId);

    certificateRequest.subscribe({
        next: (certificates) => {
          this.certificates =
            [...certificates].sort(
              (first, second) =>
                new Date(
                  second.issuedAt ??
                  second.createdAt ??
                  0
                ).getTime() -
                new Date(
                  first.issuedAt ??
                  first.createdAt ??
                  0
                ).getTime()
            );

          this.isLoading = false;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Certificate list loading failed:',
            error
          );

          this.isLoading = false;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Certificates could not be loaded.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  verifyCertificate(
    certificate: CourseCertificate
  ): void {
    if (
      !certificate.verificationCode ||
      this.verifyingCertificateId !== null
    ) {
      return;
    }

    this.verifyingCertificateId =
      certificate.certificateId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.certificateService
      .verifyCertificate(
        certificate.verificationCode
      )
      .subscribe({
        next: (verifiedCertificate) => {
          this.replaceCertificate(
            verifiedCertificate
          );

          this.verifyingCertificateId = null;

          this.actionMessage =
            verifiedCertificate.valid
              ? `Certificate ${verifiedCertificate.certificateNumber} is valid.`
              : `Certificate ${verifiedCertificate.certificateNumber} is not valid.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Certificate verification failed:',
            error
          );

          this.verifyingCertificateId = null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Certificate verification failed.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  viewCertificatePdf(
    certificate: CourseCertificate
  ): void {
    if (this.viewingCertificateId !== null) {
      return;
    }

    this.viewingCertificateId =
      certificate.certificateId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.certificateService
      .downloadCertificatePdf(
        certificate.certificateId
      )
      .subscribe({
        next: (pdfBlob) => {
          const pdfUrl =
            window.URL.createObjectURL(
              pdfBlob
            );

          window.open(
            pdfUrl,
            '_blank',
            'noopener,noreferrer'
          );

          window.setTimeout(
            () =>
              window.URL.revokeObjectURL(
                pdfUrl
              ),
            60000
          );

          this.viewingCertificateId = null;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Certificate PDF view failed:',
            error
          );

          this.viewingCertificateId = null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Certificate PDF could not be opened.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  downloadCertificatePdf(
    certificate: CourseCertificate
  ): void {
    if (
      this.downloadingCertificateId !== null
    ) {
      return;
    }

    this.downloadingCertificateId =
      certificate.certificateId;

    this.errorMessage = '';
    this.actionMessage = '';

    this.changeDetector.detectChanges();

    this.certificateService
      .downloadCertificatePdf(
        certificate.certificateId
      )
      .subscribe({
        next: (pdfBlob) => {
          const pdfUrl =
            window.URL.createObjectURL(
              pdfBlob
            );

          const downloadLink =
            document.createElement('a');

          downloadLink.href = pdfUrl;

          downloadLink.download =
            `Enterprise-Certificate-${certificate.certificateNumber}.pdf`;

          document.body.appendChild(
            downloadLink
          );

          downloadLink.click();
          downloadLink.remove();

          window.URL.revokeObjectURL(
            pdfUrl
          );

          this.downloadingCertificateId =
            null;

          this.actionMessage =
            `Certificate ${certificate.certificateNumber} downloaded successfully.`;

          this.changeDetector.detectChanges();
        },

        error: (error: HttpErrorResponse) => {
          console.error(
            'Certificate PDF download failed:',
            error
          );

          this.downloadingCertificateId =
            null;

          this.errorMessage =
            this.getBackendErrorMessage(
              error,
              'Certificate PDF could not be downloaded.'
            );

          this.changeDetector.detectChanges();
        }
      });
  }

  replaceCertificate(
    updatedCertificate: CourseCertificate
  ): void {
    this.certificates =
      this.certificates.map(
        (certificate) =>
          certificate.certificateId ===
          updatedCertificate.certificateId
            ? updatedCertificate
            : certificate
      );
  }

  trackCertificateById(
    index: number,
    certificate: CourseCertificate
  ): string {
    return certificate.certificateId;
  }

  private getBackendErrorMessage(
    error: HttpErrorResponse,
    fallbackMessage: string
  ): string {
    const backendMessage =
      error.error?.message ||
      error.error?.error ||
      error.error?.details;

    if (
      typeof backendMessage === 'string' &&
      backendMessage.trim().length > 0
    ) {
      return backendMessage;
    }

    return fallbackMessage;
  }
}