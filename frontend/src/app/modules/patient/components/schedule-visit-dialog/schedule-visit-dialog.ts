import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import {
  AvailableDay,
  CalendarSchedule,
} from '../../../shared/components/calendar-schedule/calendar-schedule';
import { RescheduleData } from '../../models/visit-page.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-schedule-visit-dialog',
  imports: [ButtonModule, CommonModule, CalendarSchedule],
  templateUrl: './schedule-visit-dialog.html',
  styleUrl: './schedule-visit-dialog.scss',
})
export class ScheduleVisitDialog {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);
  protected translationService = inject(TranslationService);

  protected readonly visitId = this.config.data?.visitId;

  protected readonly availableDays = signal<AvailableDay[]>([]);

  protected readonly isLoading = signal(!this.config.data?.availableTerms);

  constructor() {
    this.initializeAvailableDays();
  }

  private initializeAvailableDays(): void {
    const backendData = this.config.data?.availableTerms || [];

    const convertedDays: AvailableDay[] = backendData.map(
      (term: {
        date: string;
        dayName: string;
        dayNumber: string;
        slots: {
          id?: string;
          isBooked?: boolean;
          startTime?: string;
          time?: string;
          available?: boolean;
          booked?: boolean;
        }[];
      }) => ({
        date: term.date, // Keep as string, calendar will handle conversion
        slots:
          term.slots?.map((slot) => {
            // Handle different slot formats from backend
            let time: string;

            if (slot.startTime) {
              // Extract time from ISO string (e.g., "2026-02-01T14:00" -> "14:00")
              time = slot.startTime.includes('T')
                ? slot.startTime.split('T')[1]
                : slot.startTime;
            } else if (slot.time) {
              // Use direct time format
              time = slot.time;
            } else {
              console.warn('Slot missing time information:', slot);
              time = '00:00';
            }

            const convertedSlot = {
              id: slot.id,
              time: time,
              available:
                slot.available !== undefined ? slot.available : !slot.isBooked,
              booked: slot.booked !== undefined ? slot.booked : !!slot.isBooked,
            };

            return convertedSlot;
          }) || [],
      }),
    );

    this.availableDays.set(convertedDays);
  }

  protected readonly rescheduleData = signal<RescheduleData>({
    doctorName: this.config.data?.event.doctor?.name || 'Dr. Smith',
    institution: this.config.data?.institution || 'Szpital Powiatowy, Kraśnik',
    selectedDate: this.config.data?.event?.day || undefined,
    selectedTime: this.config.data?.event?.time || undefined,
    selectedSlotId: this.config.data?.event?.slotId || undefined,
    patientRemarks: '',
  });

  protected onDateTimeSelected(selection: {
    date: Date;
    time: string;
    slotId?: string;
  }): void {
    this.rescheduleData.update((data) => ({
      ...data,
      selectedDate: selection.date,
      selectedTime: selection.time,
      selectedSlotId: selection.slotId,
    }));
  }

  protected onRemarksChange(remarks: string): void {
    this.rescheduleData.update((data) => ({
      ...data,
      patientRemarks: remarks,
    }));
  }

  protected isFormValid(): boolean {
    const data = this.rescheduleData();
    return !!(data.selectedDate && data.selectedTime);
  }

  protected confirmReschedule(): void {
    if (!this.isFormValid()) {
      console.warn('Please select date and time');
      return;
    }

    const rescheduleData = this.rescheduleData();

    const result = {
      visitId: this.visitId,
      newDate: rescheduleData.selectedDate,
      newTime: rescheduleData.selectedTime,
      slotId: rescheduleData.selectedSlotId,
      remarks: rescheduleData.patientRemarks,
    };

    this.ref.close(result);
  }

  protected cancelReschedule(): void {
    this.ref.close(null);
  }
}
