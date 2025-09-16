import { Routes } from '@angular/router';
import { PatientDashboardComponent } from './components/patient-dashboard-component/patient-dashboard-component';
import { DoctorPage } from './components/doctor-page/doctor-page';
import { InstitutionPage } from './components/institution-page/institution-page';
import { VisitPage } from './components/visit-page/visit-page';
import { RefferalsPage } from './components/refferals-page/refferals-page';
import { PrescriptionPage } from './components/prescription-page/prescription-page';
import { MedicalHistoryPage } from './components/medical-history-page/medical-history-page';

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
  {
    path: 'visits',
    component: VisitPage,
  },
  {
    path: 'referrals',
    component: RefferalsPage,
  },
  {
    path: 'prescriptions',
    component: PrescriptionPage,
  },
  {
    path: 'medical-history',
    component: MedicalHistoryPage,
  },
];
