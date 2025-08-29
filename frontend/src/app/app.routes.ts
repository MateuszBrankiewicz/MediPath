import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    loadChildren: () =>
      import('./modules/guest/guest.routes').then((m) => m.GUEST_ROUTES),
  },
  {
    path: 'admin',
    loadChildren: () =>
      import('./modules/guest/guest.routes').then((m) => m.GUEST_ROUTES),
  },
  {
    path: 'patient',
    loadChildren: () =>
      import('./modules/patient/patient.routes').then((m) => m.PATIENT_ROUTES),
  },
];
