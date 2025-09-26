import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, map, of } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { DashboardConfig } from '../../../shared/components/layout/dashboard-layout-component/dashboard-layout-component';
import { Refferal } from '../../models/refferal-page.model';
import { PatientCodesService } from '../../services/patient-codes.service';
import { PatientVisitsService } from '../../services/patient-visits.service';

@Component({
  selector: 'app-patient-dashboard-component',
  imports: [InputTextModule, ButtonModule, CardModule, ProgressSpinnerModule],
  templateUrl: './patient-dashboard-component.html',
  styleUrl: './patient-dashboard-component.scss',
})
export class PatientDashboardComponent implements OnInit {
  readonly searchQuery = signal('');
  private codesService = inject(PatientCodesService);
  private patientVisitsService = inject(PatientVisitsService);
  protected translationService = inject(TranslationService);

  readonly dashboardConfig: DashboardConfig = {
    title: 'Dashboard',
    showSearch: true,
    showNotifications: true,
    userDisplayName: 'Jan Kowalski',
    userRole: 'Patient',
  };

  protected readonly isLoading = signal(true);

  protected readonly codesData = toSignal(
    this.codesService.getPrescriptions().pipe(
      map((results: Refferal[]) => {
        this.isLoading.set(false);
        return {
          prescriptions: results.filter(
            (code) => code.codeType?.toLowerCase() === 'prescription',
          ),
          referrals: results.filter(
            (code) => code.codeType?.toLowerCase() === 'referral',
          ),
        };
      }),
      catchError(() => {
        this.isLoading.set(false);
        return of({ prescriptions: [], referrals: [] });
      }),
    ),
  );

  protected readonly prescriptions = computed(
    () => this.codesData()?.prescriptions || [],
  );
  protected readonly refferals = computed(
    () => this.codesData()?.referrals || [],
  );

  ngOnInit(): void {
    this.patientVisitsService.getUpcomingVisits().subscribe((data) => {
      console.log(data);
    });
  }

  constructor() {
    this.patientVisitsService.getUpcomingVisits().subscribe((data) => {
      console.log(data);
    });
  }

  protected readonly upcomingVisits = signal([
    { id: 1, time: '8:00 am', doctor: 'Kazimierz Nowak' },
    { id: 2, time: '10:00 am', doctor: 'Jan Kowalski' },
    { id: 3, time: '1:00 pm', doctor: 'Piotr Nowak' },
  ]);
}
