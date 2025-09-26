import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, map, of } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { DashboardConfig } from '../../../shared/components/layout/dashboard-layout-component/dashboard-layout-component';
import { Refferal } from '../../models/refferal-page.model';
import { VisitBasicInfo, VisitResponse } from '../../models/visit-page.model';
import { PatientCodesService } from '../../services/patient-codes.service';
import { PatientVisitsService } from '../../services/patient-visits.service';

@Component({
  selector: 'app-patient-dashboard-component',
  imports: [InputTextModule, ButtonModule, CardModule, ProgressSpinnerModule],
  templateUrl: './patient-dashboard-component.html',
  styleUrl: './patient-dashboard-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientDashboardComponent {
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

  protected readonly upcomingVisits = toSignal(
    this.patientVisitsService.getUpcomingVisits().pipe(
      map((visits: VisitResponse[]) => {
        return visits.map((visit: VisitResponse): VisitBasicInfo => {
          const [year, month, day, hour, minute] = visit.time.startTime;
          const dateString = `${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.${year} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
          return {
            id: visit.id,
            doctorName: `${visit.doctor.doctorName} ${visit.doctor.doctorSurname}`,
            date: dateString,
          };
        });
      }),
      catchError(() => {
        this.isLoading.set(false);
        return of([] as VisitBasicInfo[]);
      }),
    ),
    { initialValue: [] as VisitBasicInfo[] },
  );
}
