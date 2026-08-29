import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CompetencyService } from '../../core/services/competency.service';
import { EmployeeService } from '../../core/services/employee.service';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { Competency, CompetencyFramework, EmployeeCompetency, GapResult } from '../../learning/models/competency';
import { Employee } from '../../learning/models/employee';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-competency',
  imports: [NgClass, ReactiveFormsModule],
  templateUrl: './competency.html',
  styleUrl: './competency.css'
})
export class CompetencyFeature implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly compService = inject(CompetencyService);
  private readonly empService = inject(EmployeeService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  // View States
  readonly activeView = signal<'CATALOG' | 'FRAMEWORKS' | 'GAP_ANALYSIS'>('CATALOG');
  readonly loading = signal(true);

  // Catalog Lists
  readonly competencies = signal<Competency[]>([]);
  readonly frameworks = signal<CompetencyFramework[]>([]);
  readonly employees = signal<Employee[]>([]);

  // Gap Analysis states
  readonly selectedEmployeeId = signal<string>('');
  readonly selectedRole = signal<string>('');
  readonly gapResults = signal<GapResult[]>([]);
  readonly gapSearched = signal(false);

  // Forms and Modals
  readonly showCompModal = signal(false);
  readonly isEditingComp = signal(false);
  competencyForm!: FormGroup;

  readonly showFrameworkModal = signal(false);
  frameworkForm!: FormGroup;

  readonly showRecordModal = signal(false);
  recordForm!: FormGroup;

  // Available roles for frameworks
  readonly availableRoles = ['Senior Java Developer', 'Principal Architect', 'DevOps Engineer', 'QA Lead', 'Product Specialist'];

  ngOnInit(): void {
    this.initForms();
    this.loadData();
  }

  private initForms(): void {
    this.competencyForm = this.fb.group({
      competencyId: [null],
      name: ['', [Validators.required, Validators.minLength(2)]],
      category: ['TECHNICAL', [Validators.required]],
      description: ['', [Validators.required]],
      maxLevel: [5, [Validators.required, Validators.min(1), Validators.max(10)]]
    });

    this.frameworkForm = this.fb.group({
      role: ['Senior Java Developer', [Validators.required]],
      competencyId: [null, [Validators.required]],
      requiredLevel: [3, [Validators.required, Validators.min(1), Validators.max(10)]]
    });

    this.recordForm = this.fb.group({
      employeeId: [null, [Validators.required]],
      competencyId: [null, [Validators.required]],
      currentLevel: [3, [Validators.required, Validators.min(1), Validators.max(10)]]
    });
  }

  loadData(): void {
    this.loading.set(true);
    this.compService.getAll().subscribe({
      next: (comps) => {
        this.competencies.set(comps);
        this.loading.set(false);
      },
      error: (err) => {
        this.toast.showError('Could not sync competency catalog.');
        this.loading.set(false);
      }
    });

    this.empService.getAll().subscribe({
      next: (emps) => this.employees.set(emps),
      error: (err) => console.error('Failed to load employee list', err)
    });
  }

  setView(view: 'CATALOG' | 'FRAMEWORKS' | 'GAP_ANALYSIS'): void {
    this.activeView.set(view);
    if (view === 'FRAMEWORKS') {
      this.loadFrameworks();
    }
  }

  loadFrameworks(): void {
  console.log("Loading frameworks...");

  this.frameworks.set([]);

  this.availableRoles.forEach(role => {
    console.log("Requesting role:", role);

    this.compService.getFrameworkForRole(role).subscribe({
      next: (list) => {
        console.log(role, list);

        this.frameworks.update(current => [...current, ...list]);

        console.log("Current frameworks:", this.frameworks());
      },
      error: (err) => {
        console.error(role, err);
      }
    });
  });
}

  // --- Catalog Actions ---
  openAddCompModal(): void {
    this.isEditingComp.set(false);
    this.competencyForm.reset({ category: 'TECHNICAL', maxLevel: 5 });
    this.showCompModal.set(true);
  }

  openEditCompModal(comp: Competency): void {
    this.isEditingComp.set(true);
    this.competencyForm.reset(comp);
    this.showCompModal.set(true);
  }

  closeCompModal(): void {
    this.showCompModal.set(false);
  }

  submitCompForm(): void {
    if (this.competencyForm.invalid) {
      this.competencyForm.markAllAsTouched();
      return;
    }

    const payload = this.competencyForm.value;

    if (this.isEditingComp()) {
      this.compService.update(payload.competencyId, payload).subscribe({
        next: () => {
          this.toast.showSuccess(`Competency "${payload.name}" updated.`);
          this.loadData();
          this.closeCompModal();
        },
        error: (err) => this.toast.showError(err.message)
      });
    } else {
      this.compService.create(payload).subscribe({
        next: () => {
          this.toast.showSuccess(`Competency "${payload.name}" added to library.`);
          this.loadData();
          this.closeCompModal();
        },
        error: (err) => this.toast.showError(err.message)
      });
    }
  }

  onDeleteComp(id: number): void {
    this.confirm.ask(
      'Remove Competency?',
      'Are you sure you want to delete this competency from the global registry?'
    ).then(confirmed => {
      if (confirmed) {
        this.compService.delete(id).subscribe({
          next: () => {
            this.toast.showSuccess('Competency removed.');
            this.loadData();
          },
          error: (err) => this.toast.showError(err.message)
        });
      }
    });
  }

  // --- Framework Actions ---
  openAddFrameworkModal(): void {
    this.frameworkForm.reset({ role: 'Senior Java Developer', requiredLevel: 3 });
    this.showFrameworkModal.set(true);
  }

  closeFrameworkModal(): void {
    this.showFrameworkModal.set(false);
  }

  submitFrameworkForm(): void {
    if (this.frameworkForm.invalid) {
      this.frameworkForm.markAllAsTouched();
      return;
    }
    const value = this.frameworkForm.value;

    const selectedComp = this.competencies().find(
      c => c.competencyId === Number(value.competencyId)
    );

    if (!selectedComp) {
      return;
    }

    const duplicate = this.frameworks().some(f =>
      f.role === value.role &&
      f.competency.competencyId === Number(value.competencyId)
    );

    if (duplicate) {
      this.closeFrameworkModal();
      this.toast.showWarning("This competency is already defined for this role.");
      return;
    }

    const fw: CompetencyFramework = {
      role: value.role,
      competency: selectedComp,
      requiredLevel: Number(value.requiredLevel)
    };

    this.compService.defineFrameworkRequirement(fw).subscribe({
      next: (res) => {
        this.loadFrameworks();
        this.closeFrameworkModal();
      },
      error: (err) => {
        console.log("9. Error", err);
      }
    });
  }   

  // --- Record Competency Actions ---
  openRecordModal(): void {
    this.recordForm.reset({ currentLevel: 3 });
    this.showRecordModal.set(true);
  }

  closeRecordModal(): void {
    this.showRecordModal.set(false);
  }

  submitRecordForm(): void {
    if (this.recordForm.invalid) {
      this.recordForm.markAllAsTouched();
      return;
    }

    const value = this.recordForm.value;
    const selectedComp = this.competencies().find(c => c.competencyId === value.competencyId);

    if (!selectedComp) return;

    const ec: EmployeeCompetency = {
      employeeId: value.employeeId,
      competency: selectedComp,
      currentLevel: Number(value.currentLevel)
    };

    this.compService.recordEmployeeLevel(ec).subscribe({
      next: () => {
        this.toast.showSuccess('Competency level recorded.');
        if (this.gapSearched()) {
          this.runGapAnalysis(); // Recalculate gap graph
        }
        this.closeRecordModal();
      },
      error: (err) => this.toast.showError(err.message)
    });
  }

  // --- Gap Analysis Actions ---
  runGapAnalysis(): void {
    const empId = this.selectedEmployeeId();
    const role = this.selectedRole();

    if (!empId || !role) {
      this.toast.showWarning('Select an employee and target role to run gap calculations.');
      return;
    }

    this.compService.analyzeGap(empId, role).subscribe({
      next: (results) => {
        console.log(results);
        this.gapResults.set(results);
        this.gapSearched.set(true);
      },
      error: (err) => this.toast.showError(err.message)
    });
  }
}
