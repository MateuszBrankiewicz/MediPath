import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

export interface AppointmentItem {
  id?: string | number;
  time: string;
  patientName?: string;
  doctorName?: string;
  type?: string;
  status?: string;
}

@Component({
  selector: 'app-appointments-list',
  imports: [CommonModule, CardModule, ButtonModule],
  template: `
    <p-card class="appointments-card">
      <div class="appointments-content">
        <h3 class="appointments-title">{{ title() }}</h3>

        @if (showCurrentAppointment() && currentAppointment()) {
          <div class="current-appointment">
            <div class="appointment-time">
              {{ currentAppointment()!.time }}
              @if (currentAppointment()!.type) {
                > {{ currentAppointment()!.type }}
              }
            </div>
            <div class="appointment-patient">
              @if (currentAppointment()!.patientName) {
                {{ patientLabel() }} {{ currentAppointment()!.patientName }}
              }
              @if (currentAppointment()!.doctorName) {
                {{ doctorLabel() }} {{ currentAppointment()!.doctorName }}
              }
            </div>
            @if (showViewButton()) {
              <p-button
                [label]="viewButtonLabel()"
                class="view-button"
                (click)="onViewAppointment(currentAppointment()!)"
              >
              </p-button>
            }
          </div>
        }

        @if (appointments().length > 0) {
          <div class="appointments-list">
            @for (
              appointment of appointments();
              track appointment.id || $index
            ) {
              <div
                class="appointment-item"
                (click)="onAppointmentClick(appointment)"
              >
                <span class="appointment-time">{{ appointment.time }}</span>
                <span class="appointment-person">
                  {{ appointment.patientName || appointment.doctorName }}
                </span>
                @if (appointment.status) {
                  <span
                    class="appointment-status"
                    [class]="'status-' + appointment.status.toLowerCase()"
                  >
                    {{ appointment.status }}
                  </span>
                }
              </div>
            }
          </div>
        }

        @if (appointments().length === 0 && !currentAppointment()) {
          <div class="no-appointments">
            {{ emptyMessage() || 'No appointments scheduled' }}
          </div>
        }
      </div>
    </p-card>
  `,
  styleUrl: './appointments-list.scss',
})
export class AppointmentsList {
  readonly title = input.required<string>();
  readonly appointments = input<AppointmentItem[]>([]);
  readonly currentAppointment = input<AppointmentItem | null>(null);
  readonly showCurrentAppointment = input<boolean>(false);
  readonly showViewButton = input<boolean>(false);
  readonly viewButtonLabel = input<string>('VIEW');
  readonly patientLabel = input<string>('with');
  readonly doctorLabel = input<string>('Dr.');
  readonly emptyMessage = input<string>('');

  readonly appointmentClick = output<AppointmentItem>();
  readonly viewAppointment = output<AppointmentItem>();

  onAppointmentClick(appointment: AppointmentItem): void {
    this.appointmentClick.emit(appointment);
  }

  onViewAppointment(appointment: AppointmentItem): void {
    this.viewAppointment.emit(appointment);
  }
}
