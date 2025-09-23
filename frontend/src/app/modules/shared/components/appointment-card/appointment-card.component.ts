import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';

export interface AppointmentCardData {
  id: string;
  patientName: string;
  institutionName: string;
  visitDate: string;
  visitTime: string;
  status: 'scheduled' | 'completed' | 'canceled';
}

@Component({
  selector: 'app-appointment-card',
  imports: [CommonModule, ButtonModule],
  templateUrl: './appointment-card.component.html',
  styleUrl: './appointment-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentCardComponent {
  appointment = input.required<AppointmentCardData>();
  showPatientDetails = input<boolean>(true);
  showCancelButton = input<boolean>(true);
  actionButtonText = input<string>('Szczegóły pacjenta');
  cancelButtonText = input<string>('Anuluj');

  patientDetailsClick = output<string>();
  cancelVisitClick = output<string>();

  statusClass = computed(() => {
    switch (this.appointment().status) {
      case 'scheduled':
        return 'scheduled';
      case 'completed':
        return 'completed';
      case 'canceled':
        return 'canceled';
      default:
        return '';
    }
  });

  statusText = computed(() => {
    switch (this.appointment().status) {
      case 'scheduled':
        return 'Zaplanowane';
      case 'completed':
        return 'Zakończone';
      case 'canceled':
        return 'Anulowane';
      default:
        return '';
    }
  });

  isCancelDisabled = computed(() => {
    return this.appointment().status !== 'scheduled';
  });

  onPatientDetailsClick() {
    this.patientDetailsClick.emit(this.appointment().id);
  }

  onCancelVisitClick() {
    if (this.appointment().status === 'scheduled') {
      this.cancelVisitClick.emit(this.appointment().id);
    }
  }
}
