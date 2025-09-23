import { inject } from '@angular/core';
import { CanMatchFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthenticationService } from '../services/authentication/authentication';

// Ensures user auth status is resolved BEFORE routes are matched.
// This prevents the login route from activating when the user is actually authenticated
// (avoids showing auth pages inside the dashboard layout after refresh).
export const authBootstrapGuard: CanMatchFn = () => {
  const authService = inject(AuthenticationService);
  return authService.checkAuthStatus().pipe(
    map(() => true),
    catchError(() => of(true)),
  );
};
