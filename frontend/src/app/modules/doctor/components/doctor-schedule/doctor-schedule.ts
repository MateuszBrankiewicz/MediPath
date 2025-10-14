import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CalendarDay } from '../../../../core/models/schedule.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Calendar } from '../../../shared/components/calendar/calendar';

export interface Appointment {
  id: string;
  time: string;
  patientName: string;
  remarks?: string;
  status: 'available' | 'booked' | 'unavailable';
}

@Component({
  selector: 'app-doctor-schedule',
  imports: [CommonModule, ButtonModule, Calendar],
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

  public readonly currentDayNumber = new Date().getDate(); // 22
  public readonly currentDayName = this.getDayName(new Date());

  protected readonly appointmentsByDate = computed(() => {
    const map = new Map<string, { id: string }[]>();

    const today = new Date().toISOString().split('T')[0];
    const todaySlots = [
      ...this.todayAppointments,
      ...this.unavailableSlots,
    ].map((app) => ({ id: app.id }));

    if (todaySlots.length > 0) {
      map.set(today, todaySlots);
    }

    return map;
  });
  protected onMonthChanged(event: { year: number; month: number }): void {
    const newDate = new Date(event.year, event.month, 1);
    this.selectedDate.set(newDate);
  }

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
}
