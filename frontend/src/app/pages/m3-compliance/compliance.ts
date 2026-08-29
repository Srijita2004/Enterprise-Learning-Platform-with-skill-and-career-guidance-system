import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  finalize,
  forkJoin
} from 'rxjs';

import {
  CertificationApiService
} from '../../core/api/certification-api.service';

import {
  apiErrorMessage
} from '../../core/http-error';

import {
  ToastService
} from '../../core/toast/toast.service';

import {
  Certification,
  ComplianceStatus,
  ComplianceVerification
} from '../../models/certification.models';

import {
  humanizeStatus
} from '../../shared/status-label';


@Component({
  selector: 'app-compliance-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './compliance.html',
  styleUrl: './compliance.css'
})
export class CompliancePage {

  private readonly api =
    inject(CertificationApiService);

  private readonly toast =
    inject(ToastService);

  private readonly fb =
    inject(FormBuilder);


  readonly certifications =
    signal<Certification[]>([]);

  readonly recent =
    signal<ComplianceVerification[]>([]);

  readonly loading =
    signal(true);

  readonly modalOpen =
    signal(false);

  readonly selected =
    signal<Certification | null>(null);

  readonly saving =
    signal(false);

  readonly humanize =
    humanizeStatus;


  readonly compliantCount =
    computed(() =>
      this.certifications().filter(
        certification =>
          certification.complianceStatus === 'COMPLIANT'
      ).length
    );


  readonly nonCompliantCount =
    computed(() =>
      this.certifications().filter(
        certification =>
          certification.complianceStatus === 'NON_COMPLIANT'
      ).length
    );


  readonly pendingCount =
    computed(() =>
      this.certifications().filter(
        certification =>
          certification.complianceStatus === 'PENDING'
      ).length
    );


  readonly compliancePercent =
    computed(() => {

      const total =
        this.certifications().length;

      if (total === 0) {
        return 0;
      }

      return Math.round(
        (
          this.compliantCount() /
          total
        ) * 100
      );

    });


  readonly form =
    this.fb.group({

      result: [
        'COMPLIANT' as ComplianceStatus,
        Validators.required
      ],

      policyReference: [
        '',
        [
          Validators.required,
          Validators.minLength(2)
        ]
      ],

      evidenceReference: [
        ''
      ],

      notes: [
        ''
      ]

    });


  constructor() {
    this.load();
  }


  load(): void {

    this.loading.set(true);

    forkJoin({

      certs:
        this.api.search({
          page: 0,
          size: 100,
          sort: 'employeeName,asc'
        }),

      history:
        this.api.complianceRecent()

    })
      .pipe(
        finalize(
          () => this.loading.set(false)
        )
      )
      .subscribe({

        next: result => {

          this.certifications.set(
            result.certs.content.filter(
              certification =>
                certification.active
            )
          );

          this.recent.set(
            result.history
          );

        },

        error: error =>
          this.toast.error(
            apiErrorMessage(
              error,
              'Unable to load compliance workspace.'
            )
          )

      });

  }


  complianceRingBackground(): string {

    const percentage =
      this.compliancePercent();

    return `
      conic-gradient(
        var(--accent-primary) 0% ${percentage}%,
        var(--surface-soft) ${percentage}% 100%
      )
    `;

  }


  open(
    certification: Certification
  ): void {

    this.selected.set(
      certification
    );

    this.form.reset({

      result:
        certification.complianceStatus ===
        'NON_COMPLIANT'
          ? 'NON_COMPLIANT'
          : 'COMPLIANT',

      policyReference: '',

      evidenceReference: '',

      notes: ''

    });

    this.modalOpen.set(true);

  }


  submit(): void {

    if (
      this.form.invalid ||
      !this.selected()
    ) {

      this.form.markAllAsTouched();
      return;

    }

    const value =
      this.form.getRawValue();

    this.saving.set(true);

    this.api
      .verifyCompliance(
        this.selected()!.certificationId,
        value.result!,
        value.policyReference!.trim(),
        value.evidenceReference?.trim() || '',
        value.notes?.trim() || ''
      )
      .pipe(
        finalize(
          () => this.saving.set(false)
        )
      )
      .subscribe({

        next: () => {

          this.toast.success(
            'Compliance verification recorded and added to the audit trail.'
          );

          this.modalOpen.set(false);

          this.load();

        },

        error: error =>
          this.toast.error(
            apiErrorMessage(
              error,
              'Compliance verification failed.'
            )
          )

      });

  }


  badge(value: string): string {

    return (
      'badge badge-' +
      value
        .toLowerCase()
        .replaceAll('_', '-')
    );

  }

}