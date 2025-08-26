import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    loadChildren: () =>
      import('./guest/guest.routes').then((m) => m.GUEST_ROUTES),
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./guest/guest.routes').then((m) => m.GUEST_ROUTES),
  },
  {
    path: 'patient',
    loadChildren: () =>
      import('./patient/patient.routes').then((m) => m.PATIENT_ROUTES),
  },
];
