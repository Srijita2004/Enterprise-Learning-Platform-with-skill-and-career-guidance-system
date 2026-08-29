import { Injectable, inject } from '@angular/core';
import { ToastService as SharedToastService } from '../../shared/services/toast.service';

/** M3 adapter that reuses the existing SkillSphere global toast system. */
@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly shared = inject(SharedToastService);

  success(message: string): void {
    this.shared.showSuccess(message);
  }

  error(message: string): void {
    this.shared.showError(message);
  }

  info(message: string): void {
    this.shared.showInfo(message);
  }
}
