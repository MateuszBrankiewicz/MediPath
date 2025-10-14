import { Routes } from '@angular/router';
import { AddDoctorsPage } from './components/add-doctors-page/add-doctors-page';
import { AdminDashboard } from './components/admin-dashboard/admin-dashboard';
import { AdminInstitution } from './components/admin-institution/admin-institution';
import { CreateSchedule } from './components/create-schedule/create-schedule';
import { InstitutionSchedule } from './components/institution-schedule/institution-schedule';
import { InstitutionVisits } from './components/institution-visits/institution-visits';

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
  { path: 'schedule', component: InstitutionSchedule },
  { path: 'schedule/add-schedule', component: CreateSchedule },
  { path: 'visits', component: InstitutionVisits },
];
