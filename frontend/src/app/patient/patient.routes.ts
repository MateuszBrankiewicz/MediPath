import { Routes } from '@angular/router';
import { PatientDashboardComponent } from './patient-dashboard-component/patient-dashboard-component';

export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    component: PatientDashboardComponent,
  },
];
