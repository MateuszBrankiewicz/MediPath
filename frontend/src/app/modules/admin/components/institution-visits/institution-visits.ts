import { Component, inject, signal } from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  AppointmentCardComponent,
  AppointmentCardData,
} from '../../../shared/components/appointment-card/appointment-card.component';

@Component({
  selector: 'app-institution-visits',
  imports: [AppointmentCardComponent],
  templateUrl: './institution-visits.html',
  styleUrl: './institution-visits.scss',
})
export class InstitutionVisits {
  protected readonly translationService = inject(TranslationService);
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
}
