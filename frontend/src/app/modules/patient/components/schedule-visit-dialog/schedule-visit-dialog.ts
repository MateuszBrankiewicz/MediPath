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
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Textarea } from 'primeng/textarea';
import {
  AvailableDay,
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
    CommonModule,
    CalendarSchedule,
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
        date: term.date,
        slots:
          term.slots?.map((slot) => {
            let time: string;

            if (slot.startTime) {
              time = slot.startTime.includes('T')
                ? slot.startTime.split('T')[1]
                : slot.startTime;
            } else if (slot.time) {
              time = slot.time;
            } else {
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

  ngOnInit(): void {
    if (!this.config.data.visitId) {
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
