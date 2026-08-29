import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeSkillService } from '../../core/services/employee-skill.service';
import { SkillService } from '../../core/services/skill.service';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { Employee } from '../../learning/models/employee';
import { EmployeeSkill } from '../../learning/models/employee-skill';
import { Skill } from '../../learning/models/skill';

@Component({
  selector: 'app-employee',
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './employee.html',
  styleUrl: './employee.css'
})
export class EmployeeFeature implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empService = inject(EmployeeService);
  private readonly empSkillService = inject(EmployeeSkillService);
  private readonly skillService = inject(SkillService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  // States
  readonly employees = signal<Employee[]>([]);
  readonly skills = signal<Skill[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  // Modal / Form States
  readonly showModal = signal(false);
  readonly isEditing = signal(false);
  employeeForm!: FormGroup;

  // Skill Mapping Modal States
  readonly showMappingModal = signal(false);
  readonly selectedEmployee = signal<Employee | null>(null);
  readonly currentEmployeeSkills = signal<(EmployeeSkill & { skillName?: string; category?: string })[]>([]);
  mappingForm!: FormGroup;

  ngOnInit(): void {
    this.initForms();
    this.loadData();
  }

  private initForms(): void {
    this.employeeForm = this.fb.group({
      employeeId: [null, [Validators.required, Validators.min(1)]],
      employeeName: ['', [Validators.required, Validators.minLength(2)]],
      designation: ['', [Validators.required]],
      salary: [0, [Validators.required, Validators.min(0)]]
    });

    this.mappingForm = this.fb.group({
      skillId: [null, [Validators.required]],
      proficiencyLevel: [3, [Validators.required, Validators.min(1), Validators.max(5)]],
      yearsOfExperience: [1, [Validators.required, Validators.min(0)]]
    });
  }

  loadData(): void {
    this.loading.set(true);
    this.empService.getAll().subscribe({
      next: (data) => {
        this.employees.set(data);
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Could not load employees.');
        this.loading.set(false);
      }
    });

    this.skillService.getAll().subscribe({
      next: (data) => this.skills.set(data),
      error: (err) => console.error('Failed to load skills library', err)
    });
  }

  openAddModal(): void {
    this.isEditing.set(false);
    this.employeeForm.reset({ salary: 0 });
    this.employeeForm.get('employeeId')?.enable();
    this.showModal.set(true);
  }

  openEditModal(employee: Employee): void {
    this.isEditing.set(true);
    this.employeeForm.reset(employee);
    // ID should not be modifiable for existing employee
    this.employeeForm.get('employeeId')?.disable();
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submitForm(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    const payload = this.employeeForm.getRawValue();

    if (this.isEditing()) {
      this.empService.update(payload.employeeId, payload).subscribe({
        next: (updated) => {
          this.employees.update(current => current.map(e => e.employeeId === updated.employeeId ? updated : e));
          this.toast.showSuccess(`Employee ${updated.employeeName} updated successfully.`);
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to update employee.')
      });
    } else {
      this.empService.create(payload).subscribe({
        next: (created) => {
          this.employees.update(current => [...current, created]);
          this.toast.showSuccess(`Employee ${created.employeeName} registered successfully.`);
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to register employee.')
      });
    }
  }

  onDelete(id: number): void {
    const emp = this.employees().find(e => e.employeeId === id);
    if (!emp) return;

    this.confirm.ask(
      'Remove Employee?',
      `Are you sure you want to delete ${emp.employeeName} from the database?`
    ).then(confirmed => {
      if (confirmed) {
        this.empService.delete(id).subscribe({
          next: () => {
            this.employees.update(current => current.filter(e => e.employeeId !== id));
            this.toast.showSuccess(`Employee ${emp.employeeName} deleted successfully.`);
          },
          error: (err) => this.toast.showError(err.message || 'Failed to delete employee.')
        });
      }
    });
  }

  // --- Skill Mapping Logic ---
  openMappingModal(employee: Employee): void {
    this.selectedEmployee.set(employee);
    this.mappingForm.reset({ proficiencyLevel: 3, yearsOfExperience: 1 });
    this.loadEmployeeSkills(employee.employeeId);
    this.showMappingModal.set(true);
  }

  closeMappingModal(): void {
    this.showMappingModal.set(false);
    this.selectedEmployee.set(null);
  }

  loadEmployeeSkills(employeeId: number): void {
    this.empSkillService.getByEmployeeId(employeeId).subscribe({
      next: (mappings) => {
        const enhanced = mappings.map(m => {
          const sk = this.skills().find(s => s.skillId === m.skillId);
          return {
            ...m,
            skillName: sk?.skillName || `Unknown Skill #${m.skillId}`,
            category: sk?.category || 'TECHNICAL'
          };
        });
        this.currentEmployeeSkills.set(enhanced);
      },
      error: (err) => console.error('Failed to load employee skills', err)
    });
  }

  addSkillMapping(): void {
    if (this.mappingForm.invalid || !this.selectedEmployee()) {
      this.mappingForm.markAllAsTouched();
      return;
    }

    const value = this.mappingForm.value;
    const mapping: EmployeeSkill = {
      employeeSkillId: 0,
      employeeId: this.selectedEmployee()!.employeeId,
      skillId: Number(value.skillId),
      proficiencyLevel: Number(value.proficiencyLevel),
      yearsOfExperience: Number(value.yearsOfExperience)
    };

    this.empSkillService.create(mapping).subscribe({
      next: () => {
        this.toast.showSuccess('Skill mapping added successfully.');
        this.loadEmployeeSkills(this.selectedEmployee()!.employeeId);
        this.mappingForm.reset({ proficiencyLevel: 3, yearsOfExperience: 1 });
      },
      error: (err) => this.toast.showError(err.message || 'Failed to map skill.')
    });
  }

  removeSkillMapping(mappingId: number): void {
    this.confirm.ask(
      'Remove Skill Mapping?',
      'Are you sure you want to unmap this skill from the employee?'
    ).then(confirmed => {
      if (confirmed) {
        this.empSkillService.delete(mappingId).subscribe({
          next: () => {
            this.toast.showSuccess('Skill unmapped successfully.');
            if (this.selectedEmployee()) {
              this.loadEmployeeSkills(this.selectedEmployee()!.employeeId);
            }
          },
          error: (err) => this.toast.showError(err.message || 'Failed to unmap skill.')
        });
      }
    });
  }
}
