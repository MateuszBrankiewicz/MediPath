import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TranslationService } from '../../../../core/services/translation/translation.service';

export interface Appointment {
  id: string;
  time: string;
  patientName: string;
  remarks?: string;
  status: 'available' | 'booked' | 'unavailable';
}

export interface AppointmentIndicator {
  id: string;
}

export interface CalendarDay {
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  isSelected: boolean;
  hasAppointments: boolean;
  appointments: AppointmentIndicator[];
  date: Date;
}

@Component({
  selector: 'app-doctor-schedule',
  imports: [CommonModule, ButtonModule],
  templateUrl: './doctor-schedule.html',
  styleUrl: './doctor-schedule.scss',
})
export class DoctorSchedule {
  protected translationService = inject(TranslationService);

  public selectedAppointment = signal<Appointment | null>(null);
  public currentMonth = signal<Date>(new Date());
  public selectedDate = signal<Date | null>(new Date());

  public todayAppointments: Appointment[] = [
    {
      id: '1',
      time: '8:00 am',
      patientName: 'Jan Nowak',
      status: 'booked',
    },
    {
      id: '2',
      time: '10:00 am',
      patientName: 'Michał Kowalski',
      status: 'booked',
    },
    {
      id: '3',
      time: '3:00 pm',
      patientName: 'Adam Brzęk',
      status: 'booked',
    },
  ];

  public unavailableSlots: Appointment[] = [
    {
      id: '4',
      time: '2:00 pm',
      patientName: 'Andrzej Nowak',
      status: 'unavailable',
      remarks:
        'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris vel sodales orci. Donec eu dolor orci. Proin efficitur metus eget metus luctus facilisis.',
    },
  ];

  public weekdays = [
    this.translationService.translate('weekdays.short.monday'),
    this.translationService.translate('weekdays.short.tuesday'),
    this.translationService.translate('weekdays.short.wednesday'),
    this.translationService.translate('weekdays.short.thursday'),
    this.translationService.translate('weekdays.short.friday'),
    this.translationService.translate('weekdays.short.saturday'),
    this.translationService.translate('weekdays.short.sunday'),
  ];

  public readonly currentDayNumber = new Date().getDate(); // 22
  public readonly currentDayName = this.getDayName(new Date());

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

  public changeSelectedAppointment(appointment: Appointment): void {
    this.selectedAppointment.set(appointment);
  }

  public rescheduleVisit(): void {
    console.log(
      'Reschedule visit for:',
      this.selectedAppointment()?.patientName,
    );
  }

  public cancelVisit(): void {
    console.log('Cancel visit for:', this.selectedAppointment()?.patientName);
  }

  private getDayName(date: Date): string {
    const dayKeys = [
      'weekdays.sunday',
      'weekdays.monday',
      'weekdays.tuesday',
      'weekdays.wednesday',
      'weekdays.thursday',
      'weekdays.friday',
      'weekdays.saturday',
    ];
    return this.translationService.translate(dayKeys[date.getDay()]);
  }

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
}
