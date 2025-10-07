import { Routes } from '@angular/router';
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminDashboard,
  },
];
