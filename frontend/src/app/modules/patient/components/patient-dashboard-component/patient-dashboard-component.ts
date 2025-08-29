import { Component, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import {
  DashboardConfig,
  DashboardLayoutComponent,
} from '../../../shared/components/layout/dashboard-layout-component/dashboard-layout-component';

@Component({
  selector: 'app-patient-dashboard-component',
  imports: [
    DashboardLayoutComponent,
    InputTextModule,
    ButtonModule,
    CardModule,
  ],
  templateUrl: './patient-dashboard-component.html',
  styleUrl: './patient-dashboard-component.scss',
})
export class PatientDashboardComponent {
  readonly searchQuery = signal('');
  readonly notificationCount = signal(3);

  readonly dashboardConfig: DashboardConfig = {
    title: 'Dashboard',
    showSearch: true,
    showNotifications: true,
    userDisplayName: 'Jan Kowalski',
    userRole: 'Patient',
  };

  readonly upcomingVisits = signal([
    { id: 1, time: '8:00 am', doctor: 'Kazimierz Nowak' },
    { id: 2, time: '10:00 am', doctor: 'Jan Kowalski' },
    { id: 3, time: '1:00 pm', doctor: 'Piotr Nowak' },
  ]);
}
