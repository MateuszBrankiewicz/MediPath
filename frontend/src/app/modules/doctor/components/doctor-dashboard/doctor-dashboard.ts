import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import {
  AvailableDay,
  CalendarModel,
  TimeSlot,
} from '../../../../core/models/schedule.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import {
  generateCalendarDays,
  isSameDay,
} from '../../../../utils/createCalendarUtil';
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
    ProgressSpinnerModule,
  ],
  templateUrl: './doctor-dashboard.html',
  styleUrl: './doctor-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorDashboard implements OnInit {
  protected readonly translationService = inject(TranslationService);
  private readonly router = inject(Router);
  private readonly visitsService = inject(VisitsService);
  private readonly authService = inject(AuthenticationService);

  protected readonly currentDate = signal(new Date());
  protected readonly patientsToday = signal(14);
  protected readonly currentMonth = signal(new Date());
  protected readonly selectedDate = signal<Date | null>(new Date());
  protected readonly selectedTimeSlot = signal<string | null>(null);
  protected readonly todaysAppointments = signal<AppointmentItem[]>([]);
  protected readonly availableAppointments = signal<AvailableDay[]>([]);

  protected readonly user = computed(() => this.authService.getUser());

  protected readonly welcomeCardData = computed<WelcomeCardData>(() => ({
    userName: this.user()?.name || '',
    welcomeMessage: this.translationService.translate(
      'doctor.dashboard.welcomeBack',
    ),
    subtitle: this.translationService.translate('doctor.dashboard.haveNiceDay'),
    variant: 'gradient',
  }));

  protected readonly ratingCardData = computed<StatsCardData>(() => {
    const user = this.user();
    const ratingValue = user?.rating != null ? user.rating.toFixed(1) : 'N/A';
    return {
      title: this.translationService.translate('doctor.dashboard.myRating'),
      value: ratingValue,
      subtitle: `/5★ ${this.translationService.translate('doctor.dashboard.satisfiedPatients')}`,
      variant: 'default',
    };
  });

  protected readonly patientCountCardData = computed<StatsCardData>(() => ({
    title: this.translationService.translate(
      'doctor.dashboard.patientsForToday',
    ),
    value: this.patientsToday(),
    variant: 'gradient',
  }));

  protected readonly currentVisit = computed(() => {
    const todayVisits = this.todaysAppointments();
    if (todayVisits.length === 0) {
      return null;
    }

    const nowTime = new Date().getTime();

    return todayVisits.reduce((closest, current) => {
      const closestDiff = Math.abs(new Date(closest.time).getTime() - nowTime);
      const currentDiff = Math.abs(new Date(current.time).getTime() - nowTime);

      return currentDiff < closestDiff ? current : closest;
    });
  });

  protected readonly dayLabels = computed(() => [
    this.translationService.translate('doctor.dashboard.mon'),
    this.translationService.translate('doctor.dashboard.tue'),
    this.translationService.translate('doctor.dashboard.wed'),
    this.translationService.translate('doctor.dashboard.thu'),
    this.translationService.translate('doctor.dashboard.fri'),
    this.translationService.translate('doctor.dashboard.sat'),
    this.translationService.translate('doctor.dashboard.sun'),
  ]);

  protected readonly currentMonthDisplay = computed(() => {
    const month = this.currentMonth();
    return month.toLocaleDateString('pl-PL', {
      month: 'long',
      year: 'numeric',
    });
  });

  protected readonly calendarDays = computed(() =>
    generateCalendarDays({
      currentMonth: this.currentMonth(),
      selectedDate: this.selectedDate(),
      hasAppointmentsOnDate: (date) => this.hasAppointmentsOnDate(date),
    }),
  );

  protected readonly availableTimeSlots = computed<LocalTimeSlot[]>(() => {
    const selected = this.selectedDate();
    if (!selected) return [];

    const appointment = this.availableAppointments().find((app) => {
      const appDate = new Date(app.date);
      return isSameDay(appDate, selected);
    });

    return (
      appointment?.slots?.map((slot) => ({
        ...slot,
        selected: slot.time === this.selectedTimeSlot(),
      })) || []
    );
  });

  protected readonly formattedDate = computed(() => {
    const today = this.currentDate();
    return today.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  });

  protected readonly formattedTime = computed(() => {
    const today = this.currentDate();
    return today.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  });

  ngOnInit(): void {
    this.loadAppointments();
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

  protected onCalendarDayClick(day: CalendarModel): void {
    if (!day.isCurrentMonth || !day.date) return;

    this.selectedDate.set(day.date);
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

  protected onViewCurrentVisit(): void {
    const currentVisit = this.currentVisit();
    if (!currentVisit) {
      return;
    }
    this.router.navigate(['/doctor/current-visit', currentVisit.id]);
  }

  protected onAppointmentClick(appointment: AppointmentItem): void {
    this.router.navigate(['/doctor/current-visit', appointment.id]);
  }

  protected onDateTimeSelected(event: {
    date: Date;
    time: string;
    slotId?: string;
  }): void {
    console.log('Date/time selected:', event);
  }

  private hasAppointmentsOnDate(date: Date): boolean {
    return this.availableAppointments().some((app) => {
      const appDate = new Date(app.date);
      return isSameDay(appDate, date);
    });
  }

  private loadAppointments(): void {
    const selected = this.selectedDate();
    if (!selected) return;

    const dateString = this.formatDateForApi(selected);

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
            id: visit.id,
          }));
        this.todaysAppointments.set(appointments);
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
      },
    });
  }

  private formatDateForApi(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
