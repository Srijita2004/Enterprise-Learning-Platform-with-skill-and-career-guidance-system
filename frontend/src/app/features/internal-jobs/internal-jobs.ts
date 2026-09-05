import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { CareerService, CandidateMatch } from '../../core/services/career.service';
import { AuthService } from '../../core/auth/auth.service';
import { ToastService } from '../../core/toast/toast.service';
import { JobOpportunity } from '../../models/career.models';

@Component({
  selector: 'app-internal-jobs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './internal-jobs.html',
  styleUrl: './internal-jobs.css'
})
export class InternalJobsComponent implements OnInit {
  private readonly careerService = inject(CareerService);
  readonly authService = inject(AuthService);
  private readonly toast = inject(ToastService);

  // Data signals
  readonly jobs = signal<JobOpportunity[]>([]);
  readonly loading = signal<boolean>(true);
  readonly error = signal<string>('');

  // Filter signals
  readonly searchQuery = signal<string>('');
  readonly selectedDepartment = signal<string>('ALL');
  readonly selectedStatus = signal<string>('ALL');
  readonly selectedExperience = signal<string>('ALL');

  // Modal / Drawer states
  readonly showJobModal = signal<boolean>(false);
  readonly isEditing = signal<boolean>(false);
  readonly editingJobId = signal<number | null>(null);

  readonly showCandidatesModal = signal<boolean>(false);
  readonly selectedJobForCandidates = signal<JobOpportunity | null>(null);
  readonly candidates = signal<CandidateMatch[]>([]);
  readonly loadingCandidates = signal<boolean>(false);

  readonly showDetailModal = signal<boolean>(false);
  readonly selectedJobDetail = signal<JobOpportunity | null>(null);

  readonly showDeleteModal = signal<boolean>(false);
  readonly jobToDelete = signal<JobOpportunity | null>(null);
  readonly deletingJob = signal<boolean>(false);

  // Form Model
  jobForm = {
    title: '',
    department: 'Engineering',
    location: '',
    salary: 120000,
    requiredExperienceYears: 3,
    status: 'OPEN',
    skillsInput: '',
    description: ''
  };

  readonly savingJob = signal<boolean>(false);

  // Computed departments from active jobs
  readonly departments = computed(() => {
    const list = this.jobs();
    const set = new Set<string>();
    list.forEach(j => {
      if (j.department) set.add(j.department);
    });
    return Array.from(set).sort();
  });

  // Filtered jobs list
  readonly filteredJobs = computed(() => {
    let result = this.jobs();
    const query = this.searchQuery().toLowerCase().trim();
    const dept = this.selectedDepartment();
    const status = this.selectedStatus();
    const exp = this.selectedExperience();

    if (dept !== 'ALL') {
      result = result.filter(j => j.department === dept);
    }

    if (status !== 'ALL') {
      result = result.filter(j => (j.status || 'OPEN').toUpperCase() === status);
    }

    if (exp === 'ENTRY') {
      result = result.filter(j => (j.requiredExperienceYears || 0) <= 2);
    } else if (exp === 'MID') {
      result = result.filter(j => (j.requiredExperienceYears || 0) >= 3 && (j.requiredExperienceYears || 0) <= 5);
    } else if (exp === 'SENIOR') {
      result = result.filter(j => (j.requiredExperienceYears || 0) >= 6);
    }

    if (query) {
      result = result.filter(j =>
        (j.title && j.title.toLowerCase().includes(query)) ||
        (j.department && j.department.toLowerCase().includes(query)) ||
        (j.location && j.location.toLowerCase().includes(query)) ||
        (j.description && j.description.toLowerCase().includes(query)) ||
        (j.skillsRequired && j.skillsRequired.some(s => s.toLowerCase().includes(query)))
      );
    }

    return result;
  });

  get totalOpenCount(): number {
    return this.jobs().filter(j => (j.status || 'OPEN').toUpperCase() === 'OPEN').length;
  }

  get totalClosedCount(): number {
    return this.jobs().filter(j => (j.status || 'OPEN').toUpperCase() === 'CLOSED').length;
  }

  isAdmin(): boolean {
    return this.authService.hasRole('ADMIN');
  }

  isHR(): boolean {
    return this.authService.hasRole('HR', 'ADMIN');
  }

  ngOnInit(): void {
    this.loadJobs();
  }

  loadJobs(): void {
    this.loading.set(true);
    this.error.set('');
    // Admins and HR see all (open and closed); learners/employees see all open jobs
    const seeAll = this.isHR();
    this.careerService.getJobOpportunities(seeAll).subscribe({
      next: (data) => {
        this.jobs.set(data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error fetching internal jobs:', err);
        this.error.set('Failed to load job opportunities from the backend service.');
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.isEditing.set(false);
    this.editingJobId.set(null);
    this.jobForm = {
      title: '',
      department: 'Engineering',
      location: 'Hybrid (New York, NY)',
      salary: 130000,
      requiredExperienceYears: 4,
      status: 'OPEN',
      skillsInput: 'Java 17, Spring Boot 4, Microservices, System Design',
      description: ''
    };
    this.showJobModal.set(true);
  }

  openEditModal(job: JobOpportunity): void {
    this.isEditing.set(true);
    this.editingJobId.set(job.jobId);
    this.jobForm = {
      title: job.title,
      department: job.department,
      location: job.location,
      salary: job.salary,
      requiredExperienceYears: job.requiredExperienceYears || 3,
      status: job.status || 'OPEN',
      skillsInput: (job.skillsRequired || []).join(', '),
      description: job.description
    };
    this.showJobModal.set(true);
  }

  saveJob(): void {
    if (!this.jobForm.title.trim() || !this.jobForm.department.trim()) {
      this.toast.error('Job Title and Department are required.');
      return;
    }

    this.savingJob.set(true);
    const parsedSkills = this.jobForm.skillsInput
      .split(',')
      .map(s => s.trim())
      .filter(Boolean);

    const payload: Partial<JobOpportunity> = {
      title: this.jobForm.title.trim(),
      department: this.jobForm.department.trim(),
      location: this.jobForm.location.trim() || 'Hybrid',
      salary: Number(this.jobForm.salary) || 100000,
      requiredExperienceYears: Number(this.jobForm.requiredExperienceYears) || 2,
      status: this.jobForm.status,
      skillsRequired: parsedSkills,
      description: this.jobForm.description.trim()
    };

    if (this.isEditing() && this.editingJobId()) {
      this.careerService.updateJob(this.editingJobId()!, payload).subscribe({
        next: () => {
          this.toast.success(`Job "${payload.title}" updated successfully!`);
          this.savingJob.set(false);
          this.showJobModal.set(false);
          this.loadJobs();
        },
        error: (err) => {
          console.error('Error updating job:', err);
          this.toast.error('Failed to update job vacancy.');
          this.savingJob.set(false);
        }
      });
    } else {
      this.careerService.createJob(payload).subscribe({
        next: () => {
          this.toast.success(`New job "${payload.title}" posted successfully!`);
          this.savingJob.set(false);
          this.showJobModal.set(false);
          this.loadJobs();
        },
        error: (err) => {
          console.error('Error creating job:', err);
          this.toast.error('Failed to post new job vacancy.');
          this.savingJob.set(false);
        }
      });
    }
  }

  toggleStatus(job: JobOpportunity): void {
    const newStatus = (job.status || 'OPEN').toUpperCase() === 'OPEN' ? 'CLOSED' : 'OPEN';
    this.careerService.toggleJobStatus(job.jobId, newStatus).subscribe({
      next: () => {
        this.toast.success(`Job status changed to ${newStatus}.`);
        this.loadJobs();
      },
      error: (err) => {
        console.error('Error toggling job status:', err);
        this.toast.error('Failed to update job status.');
      }
    });
  }

  confirmDelete(job: JobOpportunity): void {
    this.jobToDelete.set(job);
    this.showDeleteModal.set(true);
  }

  executeDelete(): void {
    const job = this.jobToDelete();
    if (!job) return;

    this.deletingJob.set(true);
    this.careerService.deleteJob(job.jobId).subscribe({
      next: () => {
        this.toast.success(`Job "${job.title}" removed from marketplace.`);
        this.deletingJob.set(false);
        this.showDeleteModal.set(false);
        this.jobToDelete.set(null);
        this.loadJobs();
      },
      error: (err) => {
        console.error('Error deleting job:', err);
        this.toast.error('Failed to delete job.');
        this.deletingJob.set(false);
      }
    });
  }

  openCandidatesModal(job: JobOpportunity): void {
    this.selectedJobForCandidates.set(job);
    this.showCandidatesModal.set(true);
    this.loadingCandidates.set(true);

    this.careerService.getCandidatesForJob(job.jobId).subscribe({
      next: (data) => {
        this.candidates.set(data || []);
        this.loadingCandidates.set(false);
      },
      error: (err) => {
        console.error('Error finding candidate matches:', err);
        this.candidates.set([]);
        this.loadingCandidates.set(false);
      }
    });
  }

  openJobDetail(job: JobOpportunity): void {
    this.selectedJobDetail.set(job);
    this.showDetailModal.set(true);
  }
}
