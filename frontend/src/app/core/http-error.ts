import { HttpErrorResponse } from '@angular/common/http';
export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    const message = error.error?.message;
    if (typeof message === 'string' && message.trim()) return message;
    if (error.status === 0) return 'The backend is unavailable. Check PostgreSQL, Eureka, API Gateway and Certification Management Service.';
  }
  return fallback;
}
