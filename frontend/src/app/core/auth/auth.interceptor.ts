import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from './auth.service';

/** Attaches the current user's role/id to every outgoing API request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const user = auth.currentUser();

  if (!user) {
    return next(req);
  }

  const headers: Record<string, string> = {
    'X-User-Role': user.role,
    'X-User-Id': user.userId
  };

  if (user.token) {
    headers['Authorization'] = `Bearer ${user.token}`;
  }

  const cloned = req.clone({
    setHeaders: headers
  });

  return next(cloned);
};
