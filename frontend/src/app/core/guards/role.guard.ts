import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../services/authentication/authentication';
import { UserRoles } from '../services/authentication/authentication.model';

export const roleGuard = (allowedRoles: UserRoles[]): CanActivateFn => {
  return () => {
    const authService = inject(AuthenticationService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/auth/login']);
      return false;
    }

    const userRole = authService.getUser()?.roleCode;

    if (userRole && allowedRoles.includes(userRole as UserRoles)) {
      return true;
    }

    const dashboardRoute = authService.getDashboardRoute();
    router.navigate([dashboardRoute]);
    return false;
  };
};
