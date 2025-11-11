import { Component, inject, OnInit, signal } from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  AppointmentCardComponent,
  AppointmentCardData,
} from '../../../shared/components/appointment-card/appointment-card.component';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';

@Component({
  selector: 'app-institution-visits',
  imports: [AppointmentCardComponent],
  templateUrl: './institution-visits.html',
  styleUrl: './institution-visits.scss',
})
export class InstitutionVisits implements OnInit {
  protected readonly translationService = inject(TranslationService);
  private readonly institutionService = inject(InstitutionService);
  private readonly institutionStore = inject(InstitutionStoreService);
  protected readonly visits = signal<AppointmentCardData[]>([
    {
      id: '1',
      patientName: 'Anna Nowak',
      institutionName: 'Szpital Nr 4, Lublin',
      visitDate: '20-03-2025',
      visitTime: '14:30',
      status: 'scheduled',
    },
    {
      id: '2',
      patientName: 'Jan Kowalski',
      institutionName: 'Szpital Nr 4, Lublin',
      visitDate: '13-03-2025',
      visitTime: '10:00',
      status: 'canceled',
    },
    {
      id: '3',
      patientName: 'Janina Kowalska',
      institutionName: 'Gabinet Stomatologiczny Kowalska, Lublin',
      visitDate: '20-02-2025',
      visitTime: '16:00',
      status: 'completed',
    },
    {
      id: '4',
      patientName: 'Janina Kowalska',
      institutionName: 'Gabinet Stomatologiczny Kowalska, Lublin',
      visitDate: '20-01-2025',
      visitTime: '09:30',
      status: 'scheduled',
    },
  ]);
  ngOnInit(): void {
    this.loadVisits();
  }
  private loadVisits(): void {
    this.institutionService
      .getVisitsForInstitution(this.institutionStore.getInstitution().id)
      .subscribe((visits) => {
        const mappedVisits = (Array.isArray(visits) ? visits : []).map(
          (visit) => ({
            id: visit.id,
            patientName: visit.patient
              ? `${visit.patient.name} ${visit.patient.surname}`
              : '',
            institutionName: visit.institution?.institutionName || '',
            visitDate: visit.time?.startTime
              ? this.formatDate(visit.time.startTime)
              : '',
            visitTime: visit.time?.startTime
              ? this.formatTime(visit.time.startTime)
              : '',
            status: 'completed' as const,

            patientId: visit.patient?.userId,
          }),
        );
        this.visits.set(mappedVisits);
      });
  }
  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleDateString('pl-PL', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
        })
      : '';
  }

  private formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleTimeString('pl-PL', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        })
      : '';
  }
}
