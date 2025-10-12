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
import {
  AvailableDay,
  CalendarDay,
} from '../../../../core/models/schedule.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { TimeSlot } from '../ui/search-result.component/search-result.model';

export interface TimeOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-calendar-schedule',
  imports: [CommonModule, ButtonModule],
  templateUrl: './calendar-schedule.html',
  styleUrl: './calendar-schedule.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarSchedule {
  protected translationService = inject(TranslationService);

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

  public readonly selectedStartTime = signal<string>('');
  public readonly selectedEndTime = signal<string>('');
  public readonly customTimeInputStart = signal<string>('');
  public readonly customTimeInputEnd = signal<string>('');

  public readonly availableAppointments = input<AvailableDay[]>([]);

  public readonly monthNames = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

  public readonly dayNames = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  public readonly timeOptions: TimeOption[] = [
    { label: '06:00', value: '06:00' },
    { label: '06:30', value: '06:30' },
    { label: '07:00', value: '07:00' },
    { label: '07:30', value: '07:30' },
    { label: '08:00', value: '08:00' },
    { label: '08:30', value: '08:30' },
    { label: '09:00', value: '09:00' },
    { label: '09:30', value: '09:30' },
    { label: '10:00', value: '10:00' },
    { label: '10:30', value: '10:30' },
    { label: '11:00', value: '11:00' },
    { label: '11:30', value: '11:30' },
    { label: '12:00', value: '12:00' },
    { label: '12:30', value: '12:30' },
    { label: '13:00', value: '13:00' },
    { label: '13:30', value: '13:30' },
    { label: '14:00', value: '14:00' },
    { label: '14:30', value: '14:30' },
    { label: '15:00', value: '15:00' },
    { label: '15:30', value: '15:30' },
    { label: '16:00', value: '16:00' },
    { label: '16:30', value: '16:30' },
    { label: '17:00', value: '17:00' },
    { label: '17:30', value: '17:30' },
    { label: '18:00', value: '18:00' },
    { label: '18:30', value: '18:30' },
    { label: '19:00', value: '19:00' },
    { label: '19:30', value: '19:30' },
    { label: '20:00', value: '20:00' },
    { label: '20:30', value: '20:30' },
    { label: '21:00', value: '21:00' },
    { label: '21:30', value: '21:30' },
    { label: '22:00', value: '22:00' },
  ];

  public Math = Math;

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
      });
    }

    return days;
  });

  public readonly availableTimes = computed(() => {
    const selected = this.selectedDate();
    if (!selected) return [];

    const appointment = this.availableAppointments().find((app) => {
      const appDate =
        typeof app.date === 'string' ? new Date(app.date) : app.date;
      return appDate.toDateString() === selected.toDateString();
    });

    return appointment?.slots || [];
  });

  public readonly monthYearDisplay = computed(() => {
    const current = this.currentMonth();
    return `${this.monthNames[current.getMonth()]} ${current.getFullYear()}`;
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
    console.log('Calendar day clicked:', {
      date: calendarDay.date,
      hasAppointments: calendarDay.hasAppointments,
      isCurrentMonth: calendarDay.isCurrentMonth,
      dayNumber: calendarDay.dayNumber,
    });

    if (this.editMode()) {
      // In edit mode, don't allow selecting days from other months
      if (!calendarDay.isCurrentMonth) {
        return;
      }

      // In edit mode, don't allow selecting days that already have appointments/slots
      if (calendarDay.hasAppointments) {
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

    // In normal mode, only allow dates with appointments
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
      // In edit mode, selectable only if it's current month AND doesn't have appointments
      return calendarDay.isCurrentMonth && !calendarDay.hasAppointments;
    }
    // In normal mode, selectable only if has appointments
    return calendarDay.hasAppointments;
  }
}
