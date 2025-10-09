import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { catchError, map, of } from 'rxjs';
import { Refferal } from '../../../../core/models/refferal.model';
import {
  VisitBasicInfo,
  VisitResponse,
} from '../../../../core/models/visit.model';
import { CodesService } from '../../../../core/services/codes/codes.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { DashboardConfig } from '../../../shared/components/layout/dashboard-layout-component/dashboard-layout-component';

@Component({
  selector: 'app-patient-dashboard-component',
  imports: [InputTextModule, ButtonModule, CardModule, ProgressSpinnerModule],
  templateUrl: './patient-dashboard-component.html',
  styleUrl: './patient-dashboard-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientDashboardComponent implements OnInit {
  readonly searchQuery = signal('');
  private codesService = inject(CodesService);
  private patientVisitsService = inject(VisitsService);
  protected translationService = inject(TranslationService);

  private destroyRef = inject(DestroyRef);

  protected readonly upcomingVisits = signal<VisitBasicInfo[]>([]);

  readonly dashboardConfig: DashboardConfig = {
    title: 'Dashboard',
    showSearch: true,
    showNotifications: true,
    userDisplayName: 'Jan Kowalski',
    userRole: 'Patient',
  };

  ngOnInit(): void {
    this.patientVisitsService
      .getUpcomingVisits()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((visits: VisitResponse[]) => {
          return visits.map((visit: VisitResponse): VisitBasicInfo => {
            return {
              id: visit.id,
              doctorName: `${visit.doctor.doctorName} ${visit.doctor.doctorSurname}`,
              date: visit.time.startTime,
            };
          });
        }),
      )
      .subscribe({
        next: (visits) => {
          this.upcomingVisits.set(visits);
        },
        error: (error) => {
          console.error('Failed to load upcoming visits:', error);
        },
      });
  }

  protected readonly codesData = toSignal(
    this.codesService.getPrescriptions().pipe(
      map((results: Refferal[]) => {
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

  protected cancelVisit(visitId: string): void {
    this.patientVisitsService.cancelVisit(visitId).subscribe({
      next: () => {
        const newVisits = this.upcomingVisits().filter(
          (visit) => visit.id !== visitId,
        );
        this.upcomingVisits.set(newVisits);
      },
      error: (error) => {
        console.error('Failed to cancel visit:', error);
      },
    });
  }
}
