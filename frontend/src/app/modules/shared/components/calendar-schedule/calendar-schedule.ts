import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import {
  AvailableDay,
  CalendarDay,
} from '../../../../core/models/schedule.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AcceptActionDialogComponent } from '../ui/accept-action-dialog/accept-action-dialog-component';
import { TimeSlot } from '../ui/search-result.component/search-result.model';
import {
  dayNames,
  monthNames,
  timeOptions,
} from './calendar-schedule.constants';

export interface TimeOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-calendar-schedule',
  imports: [CommonModule, ButtonModule],
  templateUrl: './calendar-schedule.html',
  styleUrl: './calendar-schedule.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarSchedule {
  protected translationService = inject(TranslationService);
  private dialogService = inject(DialogService);
  public readonly size = input<'small' | 'medium' | 'large'>('medium');

  public readonly editMode = input<boolean>(false);

  public readonly dateTimeSelected = output<{
    date: Date;
    time: string;
    slotId?: string;
  }>();

  public readonly scheduleTimeSelected = output<{
    date: Date;
    startTime: string;
    endTime: string;
    customTime?: string;
  }>();

  public readonly initialSelectedDate = input<Date | string | null>(null);
  public readonly initialSelectedTime = input<string | null>(null);

  public readonly selectedDate = signal<Date | null>(null);
  public readonly selectedTime = signal<string | null>(null);
  public readonly currentMonth = signal<Date>(new Date());
  public readonly institutionId = input<string | null>(null);

  public readonly selectedStartTime = signal<string>('');
  public readonly selectedEndTime = signal<string>('');
  public readonly customTimeInputStart = signal<string>('');
  public readonly customTimeInputEnd = signal<string>('');

  public readonly availableAppointments = input<AvailableDay[]>([]);
  public readonly checkIfDateAppointmentIsFromThisInstitution = output<{
    date: Date;
    isFromInstitution: boolean;
  }>();

  public Math = Math;
  protected readonly dayNames = dayNames;
  protected readonly monthNames = monthNames;
  protected timeOptions: TimeOption[] = timeOptions;
  constructor() {
    effect(() => {
      const appointments = this.availableAppointments();
      const initialDate = this.initialSelectedDate();
      const initialTime = this.initialSelectedTime();
      if (initialDate) {
        const date =
          typeof initialDate === 'string' ? new Date(initialDate) : initialDate;
        this.selectedDate.set(date);
        this.selectedTime.set(initialTime);
      }

      if (appointments.length > 0) {
        const targetDate = initialDate
          ? typeof initialDate === 'string'
            ? new Date(initialDate)
            : initialDate
          : this.findFirstAvailableDate(appointments);

        if (targetDate) {
          this.navigateToMonth(targetDate);
        }
      }
    });
  }

  private findFirstAvailableDate(appointments: AvailableDay[]): Date | null {
    if (appointments.length === 0) return null;

    const sortedAppointments = [...appointments].sort((a, b) => {
      const dateA = typeof a.date === 'string' ? new Date(a.date) : a.date;
      const dateB = typeof b.date === 'string' ? new Date(b.date) : b.date;
      return dateA.getTime() - dateB.getTime();
    });

    const firstDate = sortedAppointments[0].date;
    return typeof firstDate === 'string' ? new Date(firstDate) : firstDate;
  }

  private navigateToMonth(targetDate: Date): void {
    const targetMonth = new Date(
      targetDate.getFullYear(),
      targetDate.getMonth(),
      1,
    );
    this.currentMonth.set(targetMonth);
  }

  public readonly calendarDays = computed(() => {
    const currentMonth = this.currentMonth();
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayOfWeek = (firstDayOfMonth.getDay() + 6) % 7;

    const days: CalendarDay[] = [];
    const today = new Date();

    const prevMonth = new Date(year, month - 1, 0);
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month - 1, prevMonth.getDate() - i);
      days.push({
        date,
        isCurrentMonth: false,
        isToday: false,
        hasAppointments: this.isDateAvailable(date),
        dayNumber: date.getDate(),
        isSelected: false,
        appointments: [],
      });
    }

    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
      const date = new Date(year, month, day);
      days.push({
        date,
        isCurrentMonth: true,
        isToday: date.toDateString() === today.toDateString(),
        hasAppointments: this.isDateAvailable(date),
        dayNumber: day,
        isSelected: false,
        appointments: [],
      });
    }

    const remainingDays = 42 - days.length;
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, month + 1, day);
      days.push({
        date,
        isCurrentMonth: false,
        isToday: false,
        hasAppointments: this.isDateAvailable(date),
        dayNumber: day,
        isSelected: false,
        appointments: [],
      });
    }
    return days;
  });

  public readonly availableTimes = computed(() => {
    const selected = this.selectedDate();
    if (!selected) return [];
    if (this.editMode()) {
      const appointmentsForDay = this.availableAppointments().find((app) => {
        const appDate =
          typeof app.date === 'string' ? new Date(app.date) : app.date;
        return appDate.toDateString() === selected.toDateString();
      });

      const bookedTimes = appointmentsForDay
        ? appointmentsForDay.slots.map((slot) => slot.time)
        : [];
      return this.timeOptions.map((option) => ({
        id: option.value,
        time: option.value,
        label: option.label,
        booked: false,
        available: !bookedTimes.includes(option.value),
      }));
    } else {
      const found = this.availableAppointments().find((app) => {
        const appDate =
          typeof app.date === 'string' ? new Date(app.date) : app.date;
        return appDate.toDateString() === selected.toDateString();
      });
      return found ? found.slots : [];
    }
  });

  protected availableEndTimes = computed(() => {
    const selectedStart = this.selectedStartTime();
    const times = this.availableTimes();
    if (!selectedStart) {
      return times;
    }
    const [startHour, startMinute] = selectedStart.split(':').map(Number);
    const startTotalMinutes = startHour * 60 + startMinute;

    const nextUnavailable = times
      .filter((option) => !option.available)
      .map((option) => {
        const [h, m] = option.time.split(':').map(Number);
        return h * 60 + m;
      })
      .filter((min) => min > startTotalMinutes)
      .sort((a, b) => a - b)[0];

    return times.filter((option) => {
      const [optionHour, optionMinute] = option.time.split(':').map(Number);
      const optionTotalMinutes = optionHour * 60 + optionMinute;
      if (nextUnavailable !== undefined) {
        return (
          optionTotalMinutes > startTotalMinutes &&
          optionTotalMinutes <= nextUnavailable - 30
        );
      }
      return optionTotalMinutes > startTotalMinutes && option.available;
    });
  });

  public readonly monthYearDisplay = computed(() => {
    const current = this.currentMonth();
    return `${monthNames[current.getMonth()]} ${current.getFullYear()}`;
  });

  public previousMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() - 1, 1),
    );
  }

  public nextMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() + 1, 1),
    );
  }

  public onDateSelect(calendarDay: CalendarDay): void {
    if (this.editMode()) {
      if (!calendarDay.isCurrentMonth) {
        return;
      }
      if (calendarDay.hasAppointments) {
        const ref = this.dialogService.open(AcceptActionDialogComponent, {
          data: {
            message: this.translationService.translate(
              'shared.calendar.editModeAppointmentExists',
            ),
            acceptLabel: this.translationService.translate('shared.yes'),
            rejectLabel: this.translationService.translate('shared.no'),
          },
        });
        if (!ref) {
          return;
        }
        ref.onClose.subscribe((accepted) => {
          if (accepted) {
            this.selectedDate.set(calendarDay.date);
            this.selectedTime.set(null);
            this.selectedStartTime.set('');
            this.selectedEndTime.set('');
            this.customTimeInputStart.set('');
            this.customTimeInputEnd.set('');
          }
        });
        return;
      }
      this.selectedDate.set(calendarDay.date);
      this.selectedTime.set(null);
      this.selectedStartTime.set('');
      this.selectedEndTime.set('');
      this.customTimeInputStart.set('');
      this.customTimeInputEnd.set('');
      return;
    }

    if (!calendarDay.hasAppointments) {
      return;
    }

    this.selectedDate.set(calendarDay.date);
    this.selectedTime.set(null);
  }

  public onTimeSelect(slot: TimeSlot): void {
    if (!this.selectedDate()) return;

    this.selectedTime.set(slot.time);

    this.dateTimeSelected.emit({
      date: this.selectedDate()!,
      time: slot.time,
      slotId: slot.id,
    });
  }

  protected isDateAvailable(date: Date): boolean {
    return this.availableAppointments().some((app) => {
      const appDate =
        typeof app.date === 'string' ? new Date(app.date) : app.date;
      if (!(appDate instanceof Date) || isNaN(appDate.getTime())) {
        return false;
      }

      return appDate.toDateString() === date.toDateString();
    });
  }

  public isDateSelected(calendarDay: CalendarDay): boolean {
    const selected = this.selectedDate();
    return selected
      ? selected.toDateString() === calendarDay.date.toDateString()
      : false;
  }

  public getFirstColumn(): TimeSlot[] {
    const times = this.availableTimes();
    const half = Math.ceil(times.length / 2);
    return times.slice(0, half);
  }

  public getSecondColumn(): TimeSlot[] {
    const times = this.availableTimes();
    const half = Math.ceil(times.length / 2);
    return times.slice(half);
  }

  public onStartTimeSelect(time: string): void {
    this.selectedStartTime.set(time);
    this.emitScheduleTime();
  }

  public onEndTimeSelect(time: string): void {
    this.selectedEndTime.set(time);
    this.emitScheduleTime();
  }

  public onCustomTimeStartChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.customTimeInputStart.set(input.value);
    this.selectedStartTime.set('');
    this.emitScheduleTime();
  }

  public onCustomTimeEndChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.customTimeInputEnd.set(input.value);
    this.emitScheduleTime();
  }

  private emitScheduleTime(): void {
    const date = this.selectedDate();
    let startTime = this.selectedStartTime();
    let endTime = this.selectedEndTime();
    const customTimeStart = this.customTimeInputStart();
    const customTimeEnd = this.customTimeInputEnd();
    if (!startTime) {
      startTime = customTimeStart;
    }
    if (!endTime) {
      endTime = customTimeEnd;
    }

    if (date && startTime && endTime) {
      this.scheduleTimeSelected.emit({
        date,
        startTime,
        endTime,
      });
    }
  }

  public isDaySelectable(calendarDay: CalendarDay): boolean {
    if (this.editMode()) {
      return calendarDay.isCurrentMonth && !calendarDay.hasAppointments;
    }
    return calendarDay.hasAppointments;
  }

  private checkIfAppointmentIsFromThisInstitution(date: Date): boolean {
    if (!this.institutionId()) {
      return false;
    }

    const appointment = this.availableAppointments().find((app) => {
      const appDate =
        typeof app.date === 'string' ? new Date(app.date) : app.date;
      return appDate.toDateString() === date.toDateString();
    });

    if (!appointment) {
      return false;
    }

    return appointment.slots.some(
      (slot) => slot.institutionId === this.institutionId(),
    );
  }
}
