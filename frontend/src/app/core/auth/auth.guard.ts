import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService, UserRole } from './auth.service';

/** Blocks any route unless the user has logged in. */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn()) {
    return true;
  }

  return router.parseUrl('/login');
};

/** Factory for a guard that additionally restricts a route to specific roles. */
export function roleGuard(...allowedRoles: UserRole[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.isLoggedIn()) {
      return router.parseUrl('/login');
    }

    if (auth.hasRole(...allowedRoles)) {
      return true;
    }

    return router.parseUrl('/dashboard');
  };
}
