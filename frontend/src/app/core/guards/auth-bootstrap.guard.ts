import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthenticationService } from '../services/authentication/authentication';

export const authBootstrapGuard: CanMatchFn = () => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  if (router.url.startsWith('/auth')) {
    return of(true);
  }

  return authService.checkAuthStatus().pipe(
    map(() => true),
    catchError(() => {
      router.navigate(['/auth/login']);
      return of(false);
    }),
  );
};
