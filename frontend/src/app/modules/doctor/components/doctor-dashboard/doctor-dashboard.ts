import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TranslationService } from '../../../../core/services/translation/translation.service';

import { Router } from '@angular/router';

import { AvailableDay, TimeSlot } from '../../../../core/models/schedule.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import {
  AppointmentItem,
  AppointmentsList,
} from '../../../shared/components/ui/appointments-list/appointments-list';
import {
  StatsCard,
  StatsCardData,
} from '../../../shared/components/ui/stats-card/stats-card';
import {
  WelcomeCard,
  WelcomeCardData,
} from '../../../shared/components/ui/welcome-card/welcome-card';

interface CalendarDay {
  date: number | null;
  isToday: boolean;
  isSelected: boolean;
  isOtherMonth: boolean;
  hasAppointments: boolean;
}

interface LocalTimeSlot extends TimeSlot {
  selected?: boolean;
}

@Component({
  selector: 'app-doctor-dashboard',
  imports: [
    CommonModule,
    ButtonModule,
    StatsCard,
    WelcomeCard,
    AppointmentsList,
  ],
  templateUrl: './doctor-dashboard.html',
  styleUrl: './doctor-dashboard.scss',
})
export class DoctorDashboard implements OnInit {
  protected translationService = inject(TranslationService);
  protected router = inject(Router);
  private visitsService = inject(VisitsService);

  private readonly authService = inject(AuthenticationService);
  protected readonly doctorName = signal('Jan');
  protected readonly currentDate = signal(new Date());
  protected readonly patientsToday = signal(14);
  protected readonly currentMonth = signal(new Date());
  protected readonly selectedDate = signal<Date | null>(new Date());
  protected readonly selectedTimeSlot = signal<string | null>(null);

  protected readonly user = computed(() => this.authService.getUser());
  get welcomeCardData(): WelcomeCardData {
    return {
      userName: this.user()?.name || '',
      welcomeMessage: this.translationService.translate(
        'doctor.dashboard.welcomeBack',
      ),
      subtitle: this.translationService.translate(
        'doctor.dashboard.haveNiceDay',
      ),
      variant: 'gradient',
    };
  }

  get ratingCardData(): StatsCardData {
    const user = this.user();
    const ratingValue = user?.rating != null ? user.rating.toFixed(1) : 'N/A';
    return {
      title: this.translationService.translate('doctor.dashboard.myRating'),
      value: ratingValue,
      subtitle: `/5★ ${this.translationService.translate('doctor.dashboard.satisfiedPatients')}`,
      variant: 'default',
    };
  }

  get patientCountCardData(): StatsCardData {
    return {
      title: this.translationService.translate(
        'doctor.dashboard.patientsForToday',
      ),
      value: this.patientsToday(),
      variant: 'gradient',
    };
  }

  readonly currentVisit = signal<AppointmentItem | null>(null);

  protected readonly todaysAppointments = signal<AppointmentItem[]>([]);

  protected readonly availableAppointments = signal<AvailableDay[]>([
    {
      date: new Date(2024, 5, 10),
      slots: [
        { id: '1', time: '09:00', available: true, booked: false },
        { id: '2', time: '10:30', available: true, booked: false },
      ],
    },
    {
      date: new Date(2024, 5, 26),
      slots: [
        { id: '3', time: '14:00', available: true, booked: false },
        { id: '4', time: '15:30', available: true, booked: false },
      ],
    },
  ]);

  protected readonly dayLabels = signal([
    'doctor.dashboard.mon',
    'doctor.dashboard.tue',
    'doctor.dashboard.wed',
    'doctor.dashboard.thu',
    'doctor.dashboard.fri',
    'doctor.dashboard.sat',
    'doctor.dashboard.sun',
  ]);

  get currentMonthDisplay(): string {
    const month = this.currentMonth();
    return month.toLocaleDateString('pl-PL', {
      month: 'long',
      year: 'numeric',
    });
  }

  ngOnInit(): void {
    this.loadAppointments();
  }

  get calendarDays(): CalendarDay[] {
    const currentMonth = this.currentMonth();
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const today = new Date();
    const selectedDate = this.selectedDate();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayOfWeek = (firstDayOfMonth.getDay() + 6) % 7;

    const days: CalendarDay[] = [];

    const prevMonth = new Date(year, month - 1, 0);
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const date = prevMonth.getDate() - i;
      days.push({
        date,
        isToday: false,
        isSelected: false,
        isOtherMonth: true,
        hasAppointments: false,
      });
    }

    for (let date = 1; date <= lastDayOfMonth.getDate(); date++) {
      const currentDate = new Date(year, month, date);
      const isToday = currentDate.toDateString() === today.toDateString();
      const isSelected = selectedDate
        ? currentDate.toDateString() === selectedDate.toDateString()
        : isToday;
      days.push({
        date,
        isToday,
        isSelected,
        isOtherMonth: false,
        hasAppointments: this.hasAppointmentsOnDate(currentDate),
      });
    }

    const remainingDays = 42 - days.length;
    for (let date = 1; date <= remainingDays; date++) {
      days.push({
        date,
        isToday: false,
        isSelected: false,
        isOtherMonth: true,
        hasAppointments: false,
      });
    }

    return days;
  }

  get availableTimeSlots(): LocalTimeSlot[] {
    const selected = this.selectedDate();
    if (!selected) return [];

    const appointment = this.availableAppointments().find((app) => {
      const appDate = new Date(app.date);
      return appDate.toDateString() === selected.toDateString();
    });

    return (
      appointment?.slots?.map((slot) => ({
        ...slot,
        selected: slot.time === this.selectedTimeSlot(),
      })) || []
    );
  }

  protected previousMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() - 1, 1),
    );
  }

  protected nextMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() + 1, 1),
    );
  }

  protected onCalendarDayClick(day: CalendarDay): void {
    if (day.isOtherMonth || !day.date) return;

    const month = this.currentMonth();
    const selectedDate = new Date(
      month.getFullYear(),
      month.getMonth(),
      day.date,
    );
    this.selectedDate.set(selectedDate);
    this.loadAppointments();
    this.selectedTimeSlot.set(null);
  }

  protected onTimeSlotClick(slot: LocalTimeSlot): void {
    this.selectedTimeSlot.set(slot.time);
  }

  protected goToToday(): void {
    const today = new Date();
    this.currentMonth.set(new Date(today.getFullYear(), today.getMonth(), 1));
    this.selectedDate.set(today);
  }

  private hasAppointmentsOnDate(date: Date): boolean {
    return this.availableAppointments().some((app) => {
      const appDate = new Date(app.date);
      return appDate.toDateString() === date.toDateString();
    });
  }

  protected onViewCurrentVisit(): void {
    this.router.navigate(['/doctor/current-visit', '1']);
  }

  protected onAppointmentClick(appointment: AppointmentItem): void {
    console.log('Appointment clicked:', appointment);
  }

  protected onDateTimeSelected(event: {
    date: Date;
    time: string;
    slotId?: string;
  }): void {
    console.log('Date/time selected:', event);
  }

  get formattedDate(): string {
    const today = this.currentDate();
    return today.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  }

  get formattedTime(): string {
    const today = this.currentDate();
    return today.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  }

  private loadAppointments() {
    const selected: Date | null = this.selectedDate();
    const dateString = selected
      ? `${selected.getFullYear()}-${String(selected.getMonth() + 1).padStart(2, '0')}-${String(
          selected.getDate(),
        ).padStart(2, '0')}`
      : '';
    this.visitsService.getDoctorVisitByDate(dateString).subscribe({
      next: (visits) => {
        const appointments: AppointmentItem[] = visits
          .filter((visit) => visit.status === 'Upcoming')
          .map((visit) => ({
            time:
              typeof visit.time === 'string'
                ? visit.time
                : visit.time.startTime,
            patientName: `${visit.patient.name} ${visit.patient.surname}`,
            type: '',
          }));
        this.todaysAppointments.set(appointments);
        this.currentVisit.set(appointments.length > 0 ? appointments[0] : null);
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
      },
    });
  }
}
