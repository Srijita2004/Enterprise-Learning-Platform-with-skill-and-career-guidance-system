import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AssessmentService } from '../../core/services/assessment.service';
import { EmployeeService } from '../../core/services/employee.service';
import { SkillService } from '../../core/services/skill.service';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { Assessment } from '../../learning/models/assessment';
import { Employee } from '../../learning/models/employee';
import { Skill } from '../../learning/models/skill';
import { NgClass } from '@angular/common';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-assessment',
  imports: [NgClass, ReactiveFormsModule],
  templateUrl: './assessment.html',
  styleUrl: './assessment.css'
})
export class AssessmentFeature implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly assessmentService = inject(AssessmentService);
  private readonly empService = inject(EmployeeService);
  private readonly skillService = inject(SkillService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  // Lists
  readonly assessments = signal<(Assessment & { employeeName?: string; skillName?: string })[]>([]);
  readonly employees = signal<Employee[]>([]);
  readonly skills = signal<Skill[]>([]);
  
  // Loading & Error
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // Forms and Modals
  readonly showModal = signal(false);
  readonly isEditing = signal(false);
  assessmentForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.assessmentForm = this.fb.group({
      assessmentId: [null],
      employeeId: [null, [Validators.required]],
      skillId: [null, [Validators.required]],
      score: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
      verified: [false]
    });
  }

  loadData(): void {
    this.loading.set(true);

    forkJoin({
      assessments: this.assessmentService.getAll(),
      employees: this.empService.getAll(),
      skills: this.skillService.getAll()
    }).subscribe({
      next: (res) => {
        this.employees.set(res.employees);
        this.skills.set(res.skills);

        // Map and enrich assessments with display names
        const enriched = res.assessments.map(a => {
          const emp = res.employees.find(e => e.employeeId === a.employeeId);
          const sk = res.skills.find(s => s.skillId === a.skillId);
          return {
            ...a,
            employeeName: emp?.employeeName || `Employee #${a.employeeId}`,
            skillName: sk?.skillName || `Skill #${a.skillId}`
          };
        });

        this.assessments.set(enriched);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Failed to sync assessment data.');
        this.loading.set(false);
      }
    });
  }

  openAddModal(): void {
    this.isEditing.set(false);
    this.assessmentForm.reset({ score: 0, verified: false });
    this.showModal.set(true);
  }

  openEditModal(assessment: Assessment): void {
    this.isEditing.set(true);
    this.assessmentForm.reset(assessment);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submitForm(): void {
    if (this.assessmentForm.invalid) {
      this.assessmentForm.markAllAsTouched();
      return;
    }

    const payload = this.assessmentForm.value;
    payload.employeeId = Number(payload.employeeId);
    payload.skillId = Number(payload.skillId);

    if (this.isEditing()) {
      this.assessmentService.update(payload.assessmentId, payload).subscribe({
        next: (updated) => {
          this.toast.showSuccess('Assessment score updated.');
          this.loadData(); // Reload to re-enrich
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to update assessment.')
      });
    } else {
      this.assessmentService.create(payload).subscribe({
        next: () => {
          this.toast.showSuccess('Assessment record registered.');
          this.loadData();
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to record assessment.')
      });
    }
  }

  toggleVerify(assessment: Assessment): void {
    const nextStatus = !assessment.verified;
    const updatedPayload = { ...assessment, verified: nextStatus };

    this.assessmentService.update(assessment.assessmentId, updatedPayload).subscribe({
      next: () => {
        this.toast.showSuccess(`Assessment verification ${nextStatus ? 'approved' : 'revoked'}.`);
        this.loadData();
      },
      error: (err) => this.toast.showError(err.message || 'Failed to change verification status.')
    });
  }

  onDelete(id: number): void {
    this.confirm.ask(
      'Remove Assessment Score?',
      'Are you sure you want to permanently delete this employee assessment record?'
    ).then(confirmed => {
      if (confirmed) {
        this.assessmentService.delete(id).subscribe({
          next: () => {
            this.assessments.update(current => current.filter(a => a.assessmentId !== id));
            this.toast.showSuccess('Assessment record removed successfully.');
          },
          error: (err) => this.toast.showError(err.message || 'Failed to remove assessment.')
        });
      }
    });
  }
}
