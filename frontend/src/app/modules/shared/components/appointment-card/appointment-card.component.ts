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
  patientId?: string;
}

@Component({
  selector: 'app-appointment-card',
  imports: [CommonModule, ButtonModule],
  templateUrl: './appointment-card.component.html',
  styleUrl: './appointment-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentCardComponent {
  public appointment = input.required<AppointmentCardData>();
  public showPatientDetails = input<boolean>(true);
  public showCancelButton = input<boolean>(true);
  public actionButtonText = input<string>('Szczegóły pacjenta');
  public cancelButtonText = input<string>('Anuluj');
  public showVisitDetails = input<boolean>(false);
  public patientDetailsClick = output<string>();
  public cancelVisitClick = output<string>();

  public readonly primaryActionButton = input<{ icon: string; label: string }>({
    icon: 'pi pi-user',
    label: 'Szczegóły pacjenta',
  });

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

  protected readonly isCancelDisabled = computed(() => {
    return this.appointment().status !== 'scheduled';
  });

  protected readonly isViewDetailsDisabled = computed(() => {
    return this.appointment().status !== 'completed';
  });

  onPatientDetailsClick() {
    if (this.showVisitDetails()) {
      this.patientDetailsClick.emit(this.appointment().id);
      return;
    }
    if (this.appointment().patientId) {
      this.patientDetailsClick.emit(this.appointment().patientId!);
    }
  }

  onCancelVisitClick() {
    if (this.appointment().status === 'scheduled') {
      this.cancelVisitClick.emit(this.appointment().id);
    }
  }
}
