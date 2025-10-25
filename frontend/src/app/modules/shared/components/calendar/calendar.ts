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
import { generateCalendarDays } from '../../../../utils/createCalendarUtil';

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
    const days = generateCalendarDays({
      currentMonth: this.currentMonth(),
      selectedDate: this.selectedDate(),
      hasAppointmentsOnDate: (date) => this.hasAppointmentsOnDate(date),
    });

    return days.map((day) => {
      const appointmentData = this.findAppointmentDataForDate(
        day.date,
        this.appointments(),
      );
      return {
        ...day,
        appointments: appointmentData?.appointments || [],
        isFromThisInstitution: appointmentData?.isFromThisInstitution,
      };
    });
  });

  private hasAppointmentsOnDate(date: Date): boolean {
    return this.appointments().some(
      (appointment) => appointment.date.toDateString() === date.toDateString(),
    );
  }

  private findAppointmentDataForDate(
    date: Date,
    appointmentsData: CalendarDay[],
  ): CalendarDay | undefined {
    return appointmentsData.find(
      (appointment) => appointment.date.toDateString() === date.toDateString(),
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
