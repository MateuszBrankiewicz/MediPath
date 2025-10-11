import { Routes } from '@angular/router';
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard';
import { AdminInstitution } from './components/admin-institution/admin-institution';
import { AddDoctorsPage } from './components/add-doctors-page/add-doctors-page';
import { CreateSchedule } from './components/create-schedule/create-schedule';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    component: AdminDashboard,
  },
  {
    path: 'institutions',
    component: AdminInstitution,
  },
  { path: 'add-doctor', component: AddDoctorsPage },
  { path: 'add-schedule', component: CreateSchedule },
];
