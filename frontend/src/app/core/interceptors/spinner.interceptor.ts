import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { SpinnerService } from '../services/spinner/spinner.service';

export const spinnerInterceptor: HttpInterceptorFn = (req, next) => {
  const spinnerService = inject(SpinnerService);
  if (req.url.includes('/auth') || req.url.includes('/ws/')) {
    return next(req);
  }
  spinnerService.show();
  return next(req).pipe(finalize(() => spinnerService.hide()));
};
