import { Component, inject, input, output } from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Patient } from '../../models/patient.model';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-patient-card',
  imports: [DatePipe],
  template: `
    <div class="patient-card" [class.highlighted]="isHighlighted()">
      @if (isHighlighted()) {
        <div class="highlight-badge">Mateusz</div>
      }
      <div class="patient-info">
        <h3 class="patient-name">
          {{ patient().firstName }} {{ patient().lastName }}
        </h3>
        <p class="last-visit">
          {{ translationService.translate('doctor.patients.lastVisit') }}
          {{ patient().lastVisitDate | date: 'longDate' }}
        </p>
      </div>
      <button class="view-profile-btn" (click)="onViewProfile()" type="button">
        {{ translationService.translate('doctor.patients.viewProfile') }}
      </button>
    </div>
  `,
  styleUrl: './patient-card.scss',
})
export class PatientCardComponent {
  translationService = inject(TranslationService);

  patient = input.required<Patient>();
  isHighlighted = input<boolean>(false);

  viewProfile = output<Patient>();

  onViewProfile() {
    this.viewProfile.emit(this.patient());
  }
}
