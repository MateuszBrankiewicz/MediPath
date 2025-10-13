import {
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
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
  appointments: AppointmentIndicator[];
}

@Component({
  selector: 'app-calendar',
  imports: [ButtonModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss',
})
export class Calendar {
  public appointments = input<CalendarDay[]>([]);
  public currentInstitutionId = input<string | null>(null);
  public currentMonth = signal<Date>(new Date());
  public selectedDate = signal<Date | null>(null);
  private translationService = inject(TranslationService);
  public daySelected = output<Date>();
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
        return;
      } else {
        this.selectedDate.set(day.date);
        this.daySelected.emit(day.date);
        console.log(this.selectedDate());
      }
    }
  }

  public readonly calendarDays = computed(() => {
    const currentMonth = this.currentMonth();
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const appointmentsData = this.appointments();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayOfWeek = (firstDayOfMonth.getDay() + 6) % 7; // Convert to Monday = 0

    const days: CalendarDay[] = [];
    const today = new Date();
    const selected = this.selectedDate();

    // Calculate previous month's year and month correctly
    const prevMonthDate = new Date(year, month - 1, 1);
    const prevYear = prevMonthDate.getFullYear();
    const prevMonth = prevMonthDate.getMonth();
    const lastDayOfPrevMonth = new Date(prevYear, prevMonth + 1, 0);

    // Add days from previous month
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const dayNumber = lastDayOfPrevMonth.getDate() - i;
      const date = new Date(prevYear, prevMonth, dayNumber);
      const dayData = this.findAppointmentDataForDate(date, appointmentsData);
      days.push({
        date,
        dayNumber: dayNumber,
        isCurrentMonth: false,
        isToday: this.isSameDay(date, today),
        isSelected: selected ? this.isSameDay(date, selected) : false,
        hasAppointments: dayData?.hasAppointments || false,
        appointments: dayData?.appointments || [],
        isFromThisInstitution: dayData?.isFromThisInstitution,
      });
    }

    // Add days from current month
    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
      const date = new Date(year, month, day);
      const dayData = this.findAppointmentDataForDate(date, appointmentsData);
      days.push({
        date,
        dayNumber: day,
        isCurrentMonth: true,
        isToday: this.isSameDay(date, today),
        isSelected: selected ? this.isSameDay(date, selected) : false,
        hasAppointments: dayData?.hasAppointments || false,
        appointments: dayData?.appointments || [],
        isFromThisInstitution: dayData?.isFromThisInstitution,
      });
    }

    // Calculate next month's year and month correctly
    const nextMonthDate = new Date(year, month + 1, 1);
    const nextYear = nextMonthDate.getFullYear();
    const nextMonth = nextMonthDate.getMonth();

    // Add days from next month to complete the 42-day grid (6 weeks × 7 days)
    const remainingDays = 42 - days.length;
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(nextYear, nextMonth, day);
      const dayData = this.findAppointmentDataForDate(date, appointmentsData);
      days.push({
        date,
        dayNumber: day,
        isCurrentMonth: false,
        isToday: this.isSameDay(date, today),
        isSelected: selected ? this.isSameDay(date, selected) : false,
        hasAppointments: dayData?.hasAppointments || false,
        appointments: dayData?.appointments || [],
        isFromThisInstitution: dayData?.isFromThisInstitution,
      });
    }

    return days;
  });

  private findAppointmentDataForDate(
    date: Date,
    appointmentsData: CalendarDay[],
  ): CalendarDay | undefined {
    return appointmentsData.find(
      (appointment) => appointment.date.toDateString() === date.toDateString(),
    );
  }

  private isSameDay(date1: Date, date2: Date): boolean {
    return (
      date1.getFullYear() === date2.getFullYear() &&
      date1.getMonth() === date2.getMonth() &&
      date1.getDate() === date2.getDate()
    );
  }

  private toLocalISOString(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  protected checkIfAppointmentIsAvailable(): boolean {
    return true;
  }
}
