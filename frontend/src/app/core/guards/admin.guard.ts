import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login'], {
      queryParams: { returnUrl: state.url }
    });
  }

  const user = authService.currentUser();
  const roles = user?.roles;

  let isAdmin = false;
  if (Array.isArray(roles)) {
    isAdmin = roles.some(role => {
      const r = String(role).toUpperCase();
      return r === 'ADMIN' || r === 'ROLE_ADMIN';
    });
  } else if (typeof roles === 'string') {
    const r = String(roles).toUpperCase();
    isAdmin = r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  if (isAdmin) {
    return true;
  }

  return router.createUrlTree(['/products']);
};
