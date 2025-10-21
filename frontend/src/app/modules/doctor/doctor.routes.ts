import { Routes } from '@angular/router';
import { ReminderPage } from '../patient/components/reminder-page/reminder-page';
import { CurrentVisit } from './components/current-visit/current-visit';
import { DoctorDashboard } from './components/doctor-dashboard/doctor-dashboard';
import { DoctorPatientsPage } from './components/doctor-patients-page/doctor-patients-page';
import { DoctorSchedule } from './components/doctor-schedule/doctor-schedule';
import { DoctorVisits } from './components/doctor-visits/doctor-visits';

export const AUTH_ROUTES: Routes = [
  { path: '', component: DoctorDashboard },
  { path: 'schedule', component: DoctorSchedule },
  { path: 'visits', component: DoctorVisits },
  { path: 'patients', component: DoctorPatientsPage },
  { path: 'current-visit/:id', component: CurrentVisit },
  { path: 'reminders', component: ReminderPage },
];
