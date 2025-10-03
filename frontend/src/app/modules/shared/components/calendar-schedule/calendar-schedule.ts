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
import { TranslationService } from '../../../../core/services/translation/translation.service';

export interface TimeSlot {
  id: string;
  time: string;
  available: boolean;
  booked: boolean;
}

export interface AvailableDay {
  date: Date | string;
  slots: TimeSlot[];
}

export interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  hasAppointments: boolean;
  dayNumber: number;
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

  public readonly dateTimeSelected = output<{
    date: Date;
    time: string;
    slotId?: string;
  }>();

  public readonly initialSelectedDate = input<Date | string | null>(null);
  public readonly initialSelectedTime = input<string | null>(null);

  public readonly selectedDate = signal<Date | null>(null);
  public readonly selectedTime = signal<string | null>(null);
  public readonly currentMonth = signal<Date>(new Date());

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

    if (!calendarDay.hasAppointments) {
      console.log('No appointments for this date');
      return;
    }

    console.log('Setting selected date to:', calendarDay.date);
    this.selectedDate.set(calendarDay.date);
    this.selectedTime.set(null);

    console.log('Selected date is now:', this.selectedDate());
  }

  public onTimeSelect(slot: TimeSlot): void {
    if (!this.selectedDate()) return;

    this.selectedTime.set(slot.time);

    console.log('Calendar onTimeSelect - slot:', slot);
    console.log('Emitting slotId:', slot.id);

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
}
