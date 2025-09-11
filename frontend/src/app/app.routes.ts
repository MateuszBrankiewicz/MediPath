import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { UserRoles } from './core/services/authentication/authentication.model';
import { roleGuard } from './core/guards/role.guard';
import { SearchResultComponent } from './modules/shared/components/ui/search-result.component/search-result.component';

export const routes: Routes = [
  { path: 'search/:type/:query', component: SearchResultComponent },

  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./modules/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: '',
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./modules/guest/guest.routes').then((m) => m.GUEST_ROUTES),
  },
  {
    path: 'patient',
    canActivate: [authGuard, roleGuard([UserRoles.PATIENT])],
    loadChildren: () =>
      import('./modules/patient/patient.routes').then((m) => m.PATIENT_ROUTES),
  },
  {
    path: '**',
    redirectTo: '/auth/login',
  },
];
