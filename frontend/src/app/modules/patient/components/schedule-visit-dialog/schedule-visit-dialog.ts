import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';
import { RescheduleData } from '../../models/visit-page.model';

@Component({
  selector: 'app-schedule-visit-dialog',
  imports: [ButtonModule, CommonModule, CalendarSchedule],
  templateUrl: './schedule-visit-dialog.html',
  styleUrl: './schedule-visit-dialog.scss',
})
export class ScheduleVisitDialog {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);

  protected readonly visitId = this.config.data?.visitId;

  protected readonly rescheduleData = signal<RescheduleData>({
    doctorName: 'Jadwiga Chymyl',
    institution: 'Szpital Powiatowy, Kraśnik',
    selectedDate: undefined,
    selectedTime: undefined,
    patientRemarks: '',
  });

  protected onDateTimeSelected(selection: { date: Date; time: string }): void {
    this.rescheduleData.update((data) => ({
      ...data,
      selectedDate: selection.date,
      selectedTime: selection.time,
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

    const result = {
      visitId: this.visitId,
      newDate: this.rescheduleData().selectedDate,
      newTime: this.rescheduleData().selectedTime,
      remarks: this.rescheduleData().patientRemarks,
    };

    console.log('Rescheduling visit:', result);
    this.ref.close(result);
  }

  protected cancelReschedule(): void {
    this.ref.close(null);
  }
}
