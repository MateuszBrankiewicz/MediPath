import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication/authentication';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    const dashboardRoute = authService.getDashboardRoute();
    router.navigate([dashboardRoute]);
    return false;
  }

  return true;
};
