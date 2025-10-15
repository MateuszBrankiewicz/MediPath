import {
  Component,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CalendarDay } from '../../../../core/models/schedule.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';

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
    const firstDayOfWeek = (firstDayOfMonth.getDay() + 6) % 7;

    const days: CalendarDay[] = [];
    const today = new Date();
    const selected = this.selectedDate();

    const prevMonthDate = new Date(year, month - 1, 1);
    const prevYear = prevMonthDate.getFullYear();
    const prevMonth = prevMonthDate.getMonth();
    const lastDayOfPrevMonth = new Date(prevYear, prevMonth + 1, 0);

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

    const nextMonthDate = new Date(year, month + 1, 1);
    const nextYear = nextMonthDate.getFullYear();
    const nextMonth = nextMonthDate.getMonth();

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

  protected checkIfAppointmentIsAvailable(): boolean {
    return true;
  }

  protected groupAppointmentsByType(
    day: CalendarDay,
  ): { type: string; count: number }[] {
    if (!day.hasAppointments || !day.appointments.length) {
      return [];
    }

    const groups = new Map<string, number>();

    day.appointments.forEach((appointment) => {
      const type = appointment.type || 'available-same';
      groups.set(type, (groups.get(type) || 0) + 1);
    });

    return Array.from(groups.entries()).map(([type, count]) => ({
      type,
      count,
    }));
  }
}
