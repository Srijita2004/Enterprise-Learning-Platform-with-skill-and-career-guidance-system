import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { SkillService } from '../../core/services/skill.service';
import { ToastService } from '../../shared/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { Skill } from '../../learning/models/skill';
import { NgClass } from '@angular/common';

@Component({
  selector: 'app-skill',
  imports: [NgClass, ReactiveFormsModule],
  templateUrl: './skill.html',
  styleUrl: './skill.css'
})
export class SkillFeature implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly skillService = inject(SkillService);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmService);

  // States
  readonly skills = signal<Skill[]>([]);
  readonly filteredSkills = signal<Skill[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly activeFilter = signal<'ALL' | 'TECHNICAL' | 'DOMAIN' | 'SOFT'>('ALL');

  // Modal / Form States
  readonly showModal = signal(false);
  readonly isEditing = signal(false);
  skillForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  private initForm(): void {
    this.skillForm = this.fb.group({
      skillId: [null],
      skillName: ['', [Validators.required, Validators.minLength(2)]],
      category: ['TECHNICAL', [Validators.required]],
      description: ['', [Validators.required]]
    });
  }

  loadData(): void {
    this.loading.set(true);
    this.skillService.getAll().subscribe({
      next: (data) => {
        this.skills.set(data);
        this.applyFilter();
        this.error.set(null);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.message || 'Could not load skills catalog.');
        this.loading.set(false);
      }
    });
  }

  setFilter(filter: 'ALL' | 'TECHNICAL' | 'DOMAIN' | 'SOFT'): void {
    this.activeFilter.set(filter);
    this.applyFilter();
  }

  private applyFilter(): void {
    const filter = this.activeFilter();
    const all = this.skills();
    if (filter === 'ALL') {
      this.filteredSkills.set(all);
    } else {
      this.filteredSkills.set(all.filter(s => s.category === filter));
    }
  }

  openAddModal(): void {
    this.isEditing.set(false);
    this.skillForm.reset({ category: 'TECHNICAL' });
    this.showModal.set(true);
  }

  openEditModal(skill: Skill): void {
    this.isEditing.set(true);
    this.skillForm.reset(skill);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submitForm(): void {
    if (this.skillForm.invalid) {
      this.skillForm.markAllAsTouched();
      return;
    }

    const payload = this.skillForm.value;

    if (this.isEditing()) {
      this.skillService.update(payload.skillId, payload).subscribe({
        next: (updated) => {
          this.skills.update(current => current.map(s => s.skillId === updated.skillId ? updated : s));
          this.applyFilter();
          this.toast.showSuccess(`Skill ${updated.skillName} updated successfully.`);
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to update skill.')
      });
    } else {
      this.skillService.create(payload).subscribe({
        next: (created) => {
          this.skills.update(current => [...current, created]);
          this.applyFilter();
          this.toast.showSuccess(`Skill ${created.skillName} added to catalog.`);
          this.closeModal();
        },
        error: (err) => this.toast.showError(err.message || 'Failed to create skill.')
      });
    }
  }

  onDelete(id: number): void {
    const sk = this.skills().find(s => s.skillId === id);
    if (!sk) return;

    this.confirm.ask(
      'Remove Skill?',
      `Are you sure you want to delete "${sk.skillName}" from the catalog? This will break any existing employee mappings.`
    ).then(confirmed => {
      if (confirmed) {
        this.skillService.delete(id).subscribe({
          next: () => {
            this.skills.update(current => current.filter(s => s.skillId !== id));
            this.applyFilter();
            this.toast.showSuccess(`Skill "${sk.skillName}" removed successfully.`);
          },
          error: (err) => this.toast.showError(err.message || 'Failed to remove skill.')
        });
      }
    });
  }
}
