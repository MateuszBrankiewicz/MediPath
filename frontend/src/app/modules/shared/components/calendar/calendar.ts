import { Component, computed, inject, input, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AppointmentIndicator } from '../../../doctor/components/doctor-schedule/doctor-schedule';
export interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  hasAppointments: boolean;
  isSelected: boolean;
  isFromThisInstitution?: boolean;
  appointments: { id: string }[];
}

@Component({
  selector: 'app-calendar',
  imports: [ButtonModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss',
})
export class Calendar {
  public appointments = input<CalendarDay[]>([]);
  public currentMonth = signal<Date>(new Date());
  public selectedDate = signal<Date | null>(null);
  private translationService = inject(TranslationService);
  public previousMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() - 1, 1),
    );
  }

  public weekdays = [
    this.translationService.translate('weekdays.short.monday'),
    this.translationService.translate('weekdays.short.tuesday'),
    this.translationService.translate('weekdays.short.wednesday'),
    this.translationService.translate('weekdays.short.thursday'),
    this.translationService.translate('weekdays.short.friday'),
    this.translationService.translate('weekdays.short.saturday'),
    this.translationService.translate('weekdays.short.sunday'),
  ];

  public readonly currentMonthYear = computed(() => {
    const current = this.currentMonth();
    const monthKeys = [
      'months.january',
      'months.february',
      'months.march',
      'months.april',
      'months.may',
      'months.june',
      'months.july',
      'months.august',
      'months.september',
      'months.october',
      'months.november',
      'months.december',
    ];
    const monthName = this.translationService.translate(
      monthKeys[current.getMonth()],
    );
    return `${monthName} ${current.getFullYear()}`;
  });

  public nextMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() + 1, 1),
    );
  }

  public selectDate(day: CalendarDay): void {
    if (day.isCurrentMonth) {
      const currentSelected = this.selectedDate();
      if (
        currentSelected &&
        day.date.toDateString() === currentSelected.toDateString()
      ) {
        this.selectedDate.set(new Date());
      } else {
        this.selectedDate.set(day.date);
      }
    }
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
    const selected = this.selectedDate();

    const prevMonth = new Date(year, month - 1, 0);
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month - 1, prevMonth.getDate() - i);
      days.push({
        date,
        dayNumber: date.getDate(),
        isCurrentMonth: false,
        isToday: false,
        isSelected: false,
        hasAppointments: this.hasAppointmentsOnDate(date),
        appointments: [],
      });
    }

    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
      const date = new Date(year, month, day);
      days.push({
        date,
        dayNumber: day,
        isCurrentMonth: true,
        isToday: date.toDateString() === today.toDateString(),
        isSelected: selected
          ? date.toDateString() === selected.toDateString()
          : false,
        hasAppointments: this.hasAppointmentsOnDate(date),
        appointments: this.getAppointmentsForDate(date),
      });
    }

    const remainingDays = 42 - days.length;
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, month + 1, day);
      days.push({
        date,
        dayNumber: day,
        isCurrentMonth: false,
        isToday: false,
        isSelected: false,
        hasAppointments: false,
        appointments: [],
      });
    }

    return days;
  });

  private hasAppointmentsOnDate(date: Date): boolean {
    const appointmentDays = [
      1, 3, 5, 8, 9, 10, 11, 14, 16, 18, 21, 22, 24, 29, 30, 31,
    ];
    return appointmentDays.includes(date.getDate());
  }

  private getAppointmentsForDate(date: Date): AppointmentIndicator[] {
    const dayOfMonth = date.getDate();
    const appointmentDays = [
      1, 3, 5, 8, 9, 10, 11, 14, 16, 18, 21, 22, 24, 29, 30, 31,
    ];

    if (appointmentDays.includes(dayOfMonth)) {
      return [{ id: `indicator-${dayOfMonth}` }];
    }
    return [];
  }

  protected checkAppointmentsAvailability(day: CalendarDay): boolean {
    console.log(day);
    return day.appointments && day.appointments.length > 0;
  }

  protected checkIfAppointmentIsAvailable(
    indicator: AppointmentIndicator,
  ): boolean {
    const availableDays = [1, 3, 5, 8, 9, 10, 11];
    const idParts = indicator.id.split('-');
    const dayNumber = parseInt(idParts[1], 10);
    return availableDays.includes(dayNumber);
  }
}
