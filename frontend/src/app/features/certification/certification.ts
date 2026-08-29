import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CertificationService } from '../../core/services/certification.service';
import { EmployeeService } from '../../core/services/employee.service';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { Certificate } from '../../learning/models/certification';
import { Employee } from '../../learning/models/employee';
import { NgClass } from '@angular/common';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-certification',
  imports: [NgClass, ReactiveFormsModule],
  templateUrl: './certification.html',
  styleUrl: './certification.css'
})
export class CertificationFeature implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly certService = inject(CertificationService);
  private readonly empService = inject(EmployeeService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  // States
  readonly certificates = signal<(Certificate & { employeeName?: string })[]>([]);
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // Form Modal States
  readonly showModal = signal(false);
  readonly isEditing = signal(false);
  certForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.certForm = this.fb.group({
      certid: [null],
      empid: [null, [Validators.required]],
      name: ['', [Validators.required, Validators.minLength(2)]],
      issuingOrganization: ['', [Validators.required]],
      issueDate: ['', [Validators.required]],
      expiry: ['', [Validators.required]]
    });
  }

  loadData(): void {
    this.loading.set(true);

    forkJoin({
      certs: this.certService.getAll(),
      employees: this.empService.getAll()
    }).subscribe({
      next: (res) => {
        this.employees.set(res.employees);

        // Map employee names to certificates
        const enriched = res.certs.map(c => {
          const emp = res.employees.find(e => e.employeeId.toString() === c.empid.toString());
          return {
            ...c,
            employeeName: emp?.employeeName || `Employee #${c.empid}`
          };
        });

        this.certificates.set(enriched);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Failed to sync certifications.');
        this.loading.set(false);
      }
    });
  }

  openAddModal(): void {
    this.isEditing.set(false);
    this.certForm.reset();
    this.showModal.set(true);
  }

  openEditModal(certificate: Certificate): void {
    this.isEditing.set(true);
    this.certForm.reset(certificate);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submitForm(): void {
    if (this.certForm.invalid) {
      this.certForm.markAllAsTouched();
      return;
    }

    const payload = this.certForm.value;

    if (this.isEditing()) {
      this.certService.update(payload.certid, payload).subscribe({
        next: () => {
          this.toast.showSuccess('Certification updated successfully.');
          this.loadData();
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to update certificate.')
      });
    } else {
      this.certService.create(payload).subscribe({
        next: () => {
          this.toast.showSuccess('Professional certification recorded.');
          this.loadData();
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to save certificate.')
      });
    }
  }

  onDelete(id: number): void {
    this.confirm.ask(
      'Remove Certificate?',
      'Are you sure you want to delete this professional certification record?'
    ).then(confirmed => {
      if (confirmed) {
        this.certService.delete(id).subscribe({
          next: () => {
            this.certificates.update(current => current.filter(c => c.certid !== id));
            this.toast.showSuccess('Certification removed.');
          },
          error: (err) => this.toast.showError(err.message || 'Failed to delete certification.')
        });
      }
    });
  }
}
