import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { Card } from 'primeng/card';
import { Divider } from 'primeng/divider';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Textarea } from 'primeng/textarea';
import {
  AvailableDay,
  InputSlot,
  ScheduleResponse,
} from '../../../../core/models/schedule.model';
import { RescheduleData } from '../../../../core/models/visit.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';

import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';

@Component({
  selector: 'app-schedule-visit-dialog',
  imports: [
    ButtonModule,
    Card,
    CommonModule,
    CalendarSchedule,
    Divider,
    Textarea,
    FormsModule,
  ],
  templateUrl: './schedule-visit-dialog.html',
  styleUrl: './schedule-visit-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ScheduleVisitDialog implements OnInit {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);
  protected translationService = inject(TranslationService);

  protected readonly visitId = this.config.data?.visitId;

  protected readonly availableDays = signal<AvailableDay[]>([]);

  constructor() {
    if (!this.visitId) {
      this.initializeAvailableDays();
    }
  }

  private visitService = inject(VisitsService);

  private initializeAvailableDays(): void {
    const backendData = this.config.data?.availableTerms || [];

    if (backendData.length === 0) {
      this.availableDays.set([]);
      return;
    }

    if (
      backendData[0] &&
      typeof backendData[0] === 'object' &&
      'date' in backendData[0] &&
      'slots' in backendData[0]
    ) {
      const convertedDays: AvailableDay[] = backendData as AvailableDay[];
      this.availableDays.set(convertedDays);
      return;
    }

    let allSlots: InputSlot[] = [];

    if (Array.isArray(backendData[0])) {
      allSlots = backendData.flat();
    } else {
      allSlots = backendData;
    }

    const groupedByDate = new Map<string, InputSlot[]>();

    allSlots.forEach((slot: InputSlot) => {
      if (slot.startHour) {
        const datePart = slot.startHour.split(' ')[0];
        if (!groupedByDate.has(datePart)) {
          groupedByDate.set(datePart, []);
        }
        groupedByDate.get(datePart)!.push(slot);
      }
    });

    const convertedDays: AvailableDay[] = Array.from(
      groupedByDate.entries(),
    ).map(([date, slots]) => ({
      date: date,
      slots: slots.map((slot) => ({
        id: slot.id,
        time: slot.startHour.split(' ')[1].substring(0, 5),
        booked: slot.booked,
        available: !slot.booked,
      })),
    }));

    this.availableDays.set(convertedDays);
  }

  ngOnInit(): void {
    if (!this.config.data.visitId) {
      this.initPlainVisitData();

      return;
    }
    this.initVisitInformation();
    this.initializeAvailableDays();
  }

  protected readonly rescheduleData = signal<RescheduleData | null>(null);

  protected onDateTimeSelected(selection: {
    date: Date;
    time: string;
    slotId?: string;
  }): void {
    this.rescheduleData.update((data) => ({
      doctorName: data?.doctorName ?? '',
      doctorId: data?.doctorId ?? '',
      institution: data?.institution ?? '',
      patientRemarks: data?.patientRemarks ?? '',
      selectedDate: selection.date,
      selectedTime: selection.time,
      selectedSlotId: selection.slotId,
    }));
  }

  protected onRemarksChange(remarks: string | null): void {
    this.rescheduleData.update((data) => ({
      doctorName: data?.doctorName ?? '',
      doctorId: data?.doctorId ?? '',
      institution: data?.institution ?? '',
      patientRemarks: remarks ?? '',
      selectedDate: data?.selectedDate ?? new Date(),
      selectedTime: data?.selectedTime ?? '',
      selectedSlotId: data?.selectedSlotId ?? '',
    }));
  }

  private initPlainVisitData(): void {
    console.log(this.config.data);
    this.rescheduleData.set({
      doctorName: this.config.data?.event.doctor.name || '',
      doctorId: this.config.data?.event.doctor.id || '',
      institution:
        this.config.data?.event.institution.institution.institutionName || '',
      patientRemarks: this.config.data?.event.patientRemarks || '',
      selectedDate: this.config.data?.event.day || null,
      selectedTime: this.config.data?.event.time || null,
      selectedSlotId: this.config.data?.event.slotId || null,
    });
  }

  protected isFormValid(): boolean {
    const data = this.rescheduleData();
    return !!(data && data.selectedDate && data.selectedTime);
  }

  protected confirmReschedule(): void {
    if (!this.isFormValid()) {
      return;
    }

    const rescheduleData = this.rescheduleData();
    if (!rescheduleData) {
      return;
    }

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

  private initVisitInformation(): void {
    this.visitService
      .getVisitDetails(this.config.data?.visitId)
      .subscribe((visit) => {
        const startTime = new Date(visit.visit.time.startTime);

        this.rescheduleData.set({
          doctorName: `${visit.visit.doctor.doctorName} ${visit.visit.doctor.doctorSurname}`,
          doctorId: visit.visit.doctor.userId,
          institution: visit.visit.institution.institutionName,
          patientRemarks: visit.visit.patientRemarks || '',
          selectedDate: startTime,
          selectedTime: startTime.toTimeString().slice(0, 5),
          selectedSlotId: visit.visit.time.scheduleId,
        });

        if (visit.visit.doctor.userId) {
          this.getDoctorSchedule(visit.visit.doctor.userId);
        }
      });
  }

  private getDoctorSchedule(doctorId: string): void {
    this.visitService
      .getDoctorSchedule(doctorId)
      .subscribe((response: ScheduleResponse) => {
        let schedules = response.schedules || [];
        const availableDaysMap = new Map<string, AvailableDay>();
        console.log(schedules.length);
        schedules = schedules.filter((schedule) => {
          return (
            schedule.institution.institutionName ===
            this.rescheduleData()?.institution
          );
        });
        schedules.forEach((schedule) => {
          const startDate = new Date(schedule.startHour);
          const dateKey = startDate.toISOString().split('T')[0];
          const timeString = startDate.toTimeString().slice(0, 5);

          if (!availableDaysMap.has(dateKey)) {
            availableDaysMap.set(dateKey, {
              date: dateKey,
              slots: [],
            });
          }

          const day = availableDaysMap.get(dateKey)!;
          day.slots.push({
            id: schedule.id,
            time: timeString,
            available: !schedule.booked,
            booked: schedule.booked,
          });
        });

        const availableDays = Array.from(availableDaysMap.values()).sort(
          (a, b) => {
            const aDate =
              typeof a.date === 'string'
                ? a.date
                : a.date.toISOString().split('T')[0];
            const bDate =
              typeof b.date === 'string'
                ? b.date
                : b.date.toISOString().split('T')[0];
            return aDate.localeCompare(bDate);
          },
        );

        this.availableDays.set(availableDays);
      });
  }
}
