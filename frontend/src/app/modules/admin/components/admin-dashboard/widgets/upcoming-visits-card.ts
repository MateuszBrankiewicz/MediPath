import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';

export interface UpcomingVisitItem {
  id: string | number;
  time: string;
  patientName: string;
  doctorName: string;
  doctorId: string;
  date: string;
}

@Component({
  selector: 'app-upcoming-visits-card',
  imports: [CommonModule, CardModule, ButtonModule],
  template: `
    <p-card class="upcoming-card">
      <div class="upcoming-header">
        <i class="pi pi-calendar"></i>
        <h3 class="upcoming-title">{{ title() }}</h3>
      </div>
      <div class="upcoming-list">
        @for (visit of visits(); track visit.id) {
          <div class="upcoming-item">
            <span class="upcoming-time">{{ visit.time | date }}</span>
            <span class="upcoming-name">- {{ visit.patientName }}</span>
            <span class="spacer"></span>
            <p-button
              [label]="changeDoctorLabel()"
              severity="secondary"
              [rounded]="true"
              (click)="changeDoctor.emit(visit)"
            ></p-button>
            <p-button
              [label]="cancelVisitLabel()"
              severity="danger"
              [rounded]="true"
              (click)="cancelVisit.emit(visit)"
            ></p-button>
          </div>
        }
      </div>
    </p-card>
  `,
  styles: [
    `
      .upcoming-card {
        height: 100%;
      }
      .upcoming-header {
        display: flex;
        align-items: center;
        gap: 8px;
        height: 40px;
        background: linear-gradient(135deg, #7788c9 0%, #5c6fc2 100%);
        color: white;
        padding: 30px 12px;
        border-radius: 12px 12px 0 0;
        margin: -1rem -1rem 0 -1rem;
      }
      .upcoming-title {
        margin: 0;
        font-size: 22px;
      }
      .upcoming-list {
        background: var(--p-surface-50);
        padding: 12px;
        border-radius: 12px;
      }
      .upcoming-item {
        display: flex;
        align-items: center;
        gap: 8px;
        background: var(--p-surface-0);
        padding: 12px 16px;
        border-radius: 12px;
      }
      .upcoming-item + .upcoming-item {
        margin-top: 10px;
      }
      .upcoming-time {
        font-weight: 600;
      }
      .upcoming-name {
      }
      .spacer {
        flex: 1;
      }
    `,
  ],
})
export class UpcomingVisitsCard {
  readonly title = input<string>('Upcoming visits');
  readonly changeDoctorLabel = input<string>('CHANGE DOCTOR');
  readonly cancelVisitLabel = input<string>('CANCEL VISIT');
  readonly visits = input<UpcomingVisitItem[]>([]);

  readonly changeDoctor = output<UpcomingVisitItem>();
  readonly cancelVisit = output<UpcomingVisitItem>();
}
