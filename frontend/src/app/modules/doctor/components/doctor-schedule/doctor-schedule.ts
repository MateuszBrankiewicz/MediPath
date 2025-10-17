import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CalendarDay } from '../../../../core/models/schedule.model';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { mapSchedulesToCalendarDays } from '../../../../utils/calendarMapper';
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
export class DoctorSchedule implements OnInit {
  protected translationService = inject(TranslationService);
  private doctorService = inject(DoctorService);
  public selectedAppointment = signal<Appointment | null>(null);
  public currentMonth = signal<Date>(new Date());
  public selectedDate = signal<Date | null>(new Date());
  protected calendarDays = signal<CalendarDay[]>([]);
  private visitsService = inject(VisitsService);
  public todayAppointments = signal<Appointment[]>([]);

  public readonly currentDayNumber = new Date().getDate(); // 22
  public readonly currentDayName = this.getDayName(new Date());

  protected readonly appointmentsByDate = computed(() => {
    const map = new Map<string, { id: string }[]>();

    const today = new Date().toISOString().split('T')[0];
    const todaySlots = this.todayAppointments().map((app) => ({
      id: app.id,
    }));

    if (todaySlots.length > 0) {
      map.set(today, todaySlots);
    }

    return map;
  });

  protected readonly selectedDateAppointments = computed(() => {
    return this.todayAppointments();
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

  public getStatusClass(status: Appointment['status']): string {
    switch (status) {
      case 'booked':
        return 'status-upcoming';
      case 'available':
        return 'status-cancelled';
      case 'unavailable':
        return 'status-completed';
      default:
        return '';
    }
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

  ngOnInit(): void {
    this.loadDoctorSchedule();
  }
  private loadDoctorSchedule(): void {
    this.doctorService.getDoctorsSchedule().subscribe((schedule) => {
      const calendarDays = mapSchedulesToCalendarDays(
        schedule,
        {
          displayedMonth: new Date().getMonth(),
          displayedYear: new Date().getFullYear(),
        },
        true,
      ) as CalendarDay[];
      this.calendarDays.set(calendarDays);
      console.log(calendarDays);
    });
  }

  protected onDateSelected(date: Date): void {
    this.selectedDate.set(date);
    const selected: Date | null = this.selectedDate();
    const dateString = selected
      ? `${selected.getFullYear()}-${String(selected.getMonth() + 1).padStart(2, '0')}-${String(
          selected.getDate(),
        ).padStart(2, '0')}`
      : '';
    this.visitsService.getDoctorVisitByDate(dateString).subscribe({
      next: (visits) => {
        const mapStatus = (s: string): Appointment['status'] => {
          switch (s) {
            case 'Upcoming':
              return 'booked';
            case 'Cancelled':
              return 'available';
            case 'Completed':
              return 'unavailable';
            default:
              return 'unavailable';
          }
        };

        const appointments: Appointment[] = visits.map((visit) => ({
          id: visit.id,
          time:
            typeof visit.time === 'string' ? visit.time : visit.time.startTime,
          patientName: `${visit.patient.name} ${visit.patient.surname}`,
          status: mapStatus(String(visit.status)),
          remarks: visit.patientRemarks ?? undefined,
        }));
        this.todayAppointments.set(appointments);
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
      },
    });
  }

  protected readonly availableCount = computed((): number => {
    const days = this.calendarDays();
    const selectedDate = this.selectedDate();

    if (!days || days.length === 0 || !selectedDate) return 0;

    const selectedDay = days.find(
      (day) =>
        day.date.getFullYear() === selectedDate.getFullYear() &&
        day.date.getMonth() === selectedDate.getMonth() &&
        day.date.getDate() === selectedDate.getDate(),
    );

    if (!selectedDay?.appointments) return 0;
    console.log('wywoluje');
    return selectedDay.appointments.filter(
      (apt) => apt.type !== 'available-same' && apt.type !== 'available-other',
    ).length;
  });
}
