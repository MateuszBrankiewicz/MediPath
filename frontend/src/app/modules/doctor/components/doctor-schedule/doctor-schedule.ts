import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CalendarDay, InputSlot } from '../../../../core/models/schedule.model';
import { DateTimeService } from '../../../../core/services/date-time/date-time.service';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { mapSchedulesToCalendarDays } from '../../../../utils/calendarMapper';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { Calendar } from '../../../shared/components/calendar/calendar';
import {
  FilterButtonConfig,
  FilterButtonsComponent,
} from '../../../shared/components/filter-buttons/filter-buttons.component';

export interface Appointment {
  id: string;
  time: string;
  patientName: string;
  remarks?: string;
  status: 'available' | 'booked' | 'unavailable';
}

@Component({
  selector: 'app-doctor-schedule',
  imports: [CommonModule, ButtonModule, Calendar, FilterButtonsComponent],
  templateUrl: './doctor-schedule.html',
  styleUrl: './doctor-schedule.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorSchedule
  extends PaginatedComponentBase<Appointment>
  implements OnInit
{
  protected translationService = inject(TranslationService);
  protected dateTimeService = inject(DateTimeService);
  private doctorService = inject(DoctorService);
  private visitsService = inject(VisitsService);

  public selectedAppointment = signal<Appointment | null>(null);
  public currentMonth = signal<Date>(new Date());
  public selectedDate = signal<Date | null>(new Date());
  protected calendarDays = signal<CalendarDay[]>([]);
  public todayAppointments = signal<Appointment[]>([]);
  private allScheduleSlots = signal<InputSlot[]>([]);
  public viewFilter = signal<'all' | 'booked' | 'available'>('all');

  public readonly filterConfig: FilterButtonConfig<
    'all' | 'booked' | 'available'
  >[] = [
    {
      value: 'all',
      labelKey: 'doctor.schedule.filter.all',
      icon: 'pi-list',
    },
    {
      value: 'booked',
      labelKey: 'doctor.schedule.filter.booked',
      icon: 'pi-user',
    },
    {
      value: 'available',
      labelKey: 'doctor.schedule.filter.available',
      icon: 'pi-calendar-plus',
    },
  ];

  public readonly currentDayNumber = new Date().getDate();
  public readonly currentDayName = this.dateTimeService.getDayName(new Date());

  protected override get sourceData() {
    return this.todayAppointments();
  }

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

  protected readonly getCountToPaginate = computed(() => {
    const filter = this.viewFilter();
    const all = this.todayAppointments();

    switch (filter) {
      case 'booked':
        return all.filter(
          (apt) => apt.status === 'booked' || apt.status === 'unavailable',
        ).length;
      case 'available':
        return all.filter((apt) => apt.status === 'available').length;
      case 'all':
      default:
        return all.length;
    }
  });

  protected readonly selectedDateAppointments = computed(() => {
    const all = this.todayAppointments();
    const filter = this.viewFilter();
    let appointments = all;
    switch (filter) {
      case 'booked':
        appointments = all.filter(
          (apt) => apt.status === 'booked' || apt.status === 'unavailable',
        );
        break;
      case 'available':
        appointments = all.filter((apt) => apt.status === 'available');
        break;
      case 'all':
      default:
        appointments = all;
        break;
    }
    return appointments.slice(this.first(), this.first() + this.rows());
  });

  protected onMonthChanged(event: { year: number; month: number }): void {
    const newDate = new Date(event.year, event.month, 1);
    this.selectedDate.set(newDate);
  }

  public readonly currentMonthYear = computed(() => {
    return this.dateTimeService.formatMonthYear(this.currentMonth());
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

  public setViewFilter(filter: 'all' | 'booked' | 'available'): void {
    this.viewFilter.set(filter);
  }

  ngOnInit(): void {
    this.loadDoctorSchedule();
  }
  private loadDoctorSchedule(): void {
    this.doctorService.getDoctorsSchedule().subscribe((schedule) => {
      // Zapisujemy oryginalne sloty
      this.allScheduleSlots.set(schedule);

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

    // Pobieramy wszystkie sloty na wybrany dzień
    const allSlots = this.getAllSlotsForDate(date);

    // Pobieramy faktyczne wizyty
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

        // Tworzymy mapę wizyt po scheduleId dla szybkiego dostępu
        const visitsByScheduleId = new Map(
          visits.map((visit) => [
            typeof visit.time === 'string' ? visit.time : visit.time.scheduleId,
            visit,
          ]),
        );

        // Tworzymy appointments łącząc sloty z wizytami
        const appointments: Appointment[] = allSlots.map((slot) => {
          const visit = visitsByScheduleId.get(slot.id);

          if (visit) {
            // Jest wizyta - pokazujemy dane pacjenta
            return {
              id: visit.id,
              time:
                typeof visit.time === 'string'
                  ? visit.time
                  : visit.time.startTime,
              patientName: `${visit.patient.name} ${visit.patient.surname}`,
              status: mapStatus(String(visit.status)),
              remarks: visit.patientRemarks ?? undefined,
            };
          } else {
            // Nie ma wizyty - slot jest wolny
            return {
              id: slot.id,
              time: slot.startHour,
              patientName: '', // Pusty string dla wolnych slotów
              status: 'available' as const,
              remarks: undefined,
            };
          }
        });

        this.todayAppointments.set(appointments);
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        // W przypadku błędu pokazujemy wszystkie sloty jako dostępne
        const emptyAppointments: Appointment[] = allSlots.map((slot) => ({
          id: slot.id,
          time: slot.startHour,
          patientName: '',
          status: 'available' as const,
          remarks: undefined,
        }));
        this.todayAppointments.set(emptyAppointments);
      },
    });
  }

  private getAllSlotsForDate(date: Date): { id: string; startHour: string }[] {
    const days = this.calendarDays();
    const selectedDay = days.find(
      (day) =>
        day.date.getFullYear() === date.getFullYear() &&
        day.date.getMonth() === date.getMonth() &&
        day.date.getDate() === date.getDate(),
    );

    if (!selectedDay?.appointments) return [];

    // Pobieramy szczegóły slotów z zapisanych danych schedule
    const allSlots = this.allScheduleSlots();
    const slotsMap = new Map(allSlots.map((slot) => [slot.id, slot]));

    return selectedDay.appointments
      .map((apt) => {
        const slot = slotsMap.get(apt.id);
        return slot
          ? {
              id: slot.id,
              startHour: slot.startHour,
            }
          : null;
      })
      .filter(
        (slot): slot is { id: string; startHour: string } => slot !== null,
      );
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
