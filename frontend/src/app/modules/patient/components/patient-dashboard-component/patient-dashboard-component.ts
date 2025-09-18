import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { DashboardConfig } from '../../../shared/components/layout/dashboard-layout-component/dashboard-layout-component';
import { DashboardService } from './service/dashboard-service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-patient-dashboard-component',
  imports: [InputTextModule, ButtonModule, CardModule],
  templateUrl: './patient-dashboard-component.html',
  styleUrl: './patient-dashboard-component.scss',
})
export class PatientDashboardComponent implements OnInit {
  readonly searchQuery = signal('');
  readonly notificationCount = signal(3);
  private dashboardService = inject(DashboardService);
  private destroyRef = inject(DestroyRef);
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
  protected readonly refferals = signal([
    { id: 1, doctorName: 'Lech wales', pin: '1234' },
    { id: 2, doctorName: 'Lech wales', pin: '1234' },
    { id: 3, doctorName: 'Lech wales', pin: '1234' },
  ]);

  ngOnInit(): void {
    this.dashboardService
      .getPrescriptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((prescriptions) => {
        console.log(prescriptions);
      });
  }
}
