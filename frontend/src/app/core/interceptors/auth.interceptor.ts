import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  console.log('Error Interceptor');
  return next(req).pipe(
    catchError((err) => {
      if (err.status === 401) {
        console.log('Redirecting to login due to 401 error');
        router.navigate(['/auth/login']);
      }
      return throwError(() => err);
    }),
  );
};
