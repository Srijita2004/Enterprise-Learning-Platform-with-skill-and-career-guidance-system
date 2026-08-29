import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfirmService {
  readonly isOpen = signal(false);
  readonly title = signal('Are you sure?');
  readonly message = signal('This action cannot be undone.');
  
  private resolveFn: ((value: boolean) => void) | null = null;

  ask(title: string, message: string): Promise<boolean> {
    this.title.set(title);
    this.message.set(message);
    this.isOpen.set(true);
    
    return new Promise<boolean>((resolve) => {
      this.resolveFn = resolve;
    });
  }

  confirm(): void {
    this.isOpen.set(false);
    if (this.resolveFn) {
      this.resolveFn(true);
      this.resolveFn = null;
    }
  }

  cancel(): void {
    this.isOpen.set(false);
    if (this.resolveFn) {
      this.resolveFn(false);
      this.resolveFn = null;
    }
  }
}
