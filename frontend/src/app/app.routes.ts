import { Routes } from '@angular/router';
import { authBootstrapGuard } from './core/guards/auth-bootstrap.guard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { UserRoles } from './core/services/authentication/authentication.model';
import { AccountSettings } from './modules/shared/components/account-settings/account-settings';
import { EditUserProfile } from './modules/shared/components/edit-user-profile/edit-user-profile';
import { SearchResultComponent } from './modules/shared/components/ui/search-result.component/search-result.component';

export const routes: Routes = [
  {
    path: '',
    canMatch: [authBootstrapGuard],
    children: [
      { path: 'search', component: SearchResultComponent },
      { path: 'profile', component: EditUserProfile },
      { path: 'preferences', component: AccountSettings },
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
          import('./modules/patient/patient.routes').then(
            (m) => m.PATIENT_ROUTES,
          ),
      },
      {
        path: 'doctor',
        canActivate: [authGuard, roleGuard([UserRoles.DOCTOR])],
        loadChildren: () =>
          import('./modules/doctor/doctor.routes').then((m) => m.AUTH_ROUTES),
      },
      { path: '**', redirectTo: '/auth/login' },
    ],
  },
];
