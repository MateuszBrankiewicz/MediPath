import { Routes } from '@angular/router';
import { PatientDashboardComponent } from './components/patient-dashboard-component/patient-dashboard-component';
import { DoctorPage } from './components/doctor-page/doctor-page';
import { InstitutionPage } from './components/institution-page/institution-page';

export const PATIENT_ROUTES: Routes = [
  {
    path: '',
    component: PatientDashboardComponent,
  },
  {
    path: 'doctor/:id',
    component: DoctorPage,
  },
  {
    path: 'institution/:id',
    component: InstitutionPage,
  },
];
