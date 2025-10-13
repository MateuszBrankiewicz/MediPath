import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import {
  DoctorSchedule,
  DoctorWithSchedule,
} from '../../../../core/models/doctor.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { ScheduleService } from '../../../../core/services/schedule/schedule.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  Calendar,
  CalendarDay,
} from '../../../shared/components/calendar/calendar';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { InstitutionOption } from '../admin-dashboard/widgets/institution-select-card';

@Component({
  selector: 'app-institution-schedule',
  imports: [Calendar, ButtonModule],
  templateUrl: './institution-schedule.html',
  styleUrl: './institution-schedule.scss',
})
export class InstitutionSchedule implements OnInit {
  protected translationService = inject(TranslationService);
  public currentMonth = signal<Date>(new Date());
  public readonly currentDayNumber = new Date().getDate();
  public readonly currentDayName = this.getDayName(new Date());
  private router = inject(Router);
  private scheduleService = inject(ScheduleService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private institutionService = inject(InstitutionService);
  protected selectedDoctorId = signal<string | null>(null);
  protected readonly institutionOptions = signal<InstitutionOption[]>([]);
  protected selectedInstitution = signal<string | null>(null);
  protected doctorsForInstitution = signal<DoctorWithSchedule[]>([]);

  ngOnInit(): void {
    this.institutionOptions.set(
      this.institutionStoreService.getAvailableInstitutions(),
    );
    this.selectedInstitution.set(
      this.institutionStoreService.getInstitution().id,
    );
    this.loadDoctorsForInstitution(
      this.institutionStoreService.getInstitution().id,
    );
  }

  protected mapSelectedDoctorScheduleToCalendarDays = computed<CalendarDay[]>(
    () => {
      const selectedDoctorId = this.selectedDoctorId();
      if (!selectedDoctorId) {
        return [];
      }
      const doctor = this.doctorsForInstitution().find(
        (doc) => doc.doctorId === selectedDoctorId,
      );
      if (!doctor) {
        return [];
      }

      const schedule = doctor.schedules || [];
      const date = new Date();
      const availableInstitutionIds = this.institutionStoreService
        .getAvailableInstitutions()
        .map((inst) => inst.id);
      console.log('Available Institution IDs:', availableInstitutionIds);
      return mapSchedulesToCalendarDays(schedule, {
        displayedMonth: date.getMonth(),
        displayedYear: date.getFullYear(),
        selectedInstitutionIds: availableInstitutionIds,
      });
    },
  );

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

  protected saveSchedule(): void {
    this.router.navigate(['/admin/schedule/add-schedule']);
  }

  private loadDoctorsForInstitution(institutionId: string): void {
    this.institutionService
      .getDoctorsForInstitution(institutionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((doctors) => {
        const doctorsWithSchedule = doctors.map((doctor) => ({
          doctorId: doctor.doctorId,
          doctorName: doctor.doctorName,
          doctorSurname: doctor.doctorSurname,
          schedules: doctor.doctorSchedules,
        }));
        this.doctorsForInstitution.set(doctorsWithSchedule);
        this.selectedDoctorId.set(
          doctorsWithSchedule.length > 0
            ? doctorsWithSchedule[0].doctorId
            : null,
        );
      });
  }

  protected selectDoctor(doctorId: string): void {
    this.selectedDoctorId.set(doctorId);
  }

  protected selectedDoctorIdFn(): string | null {
    return this.selectedDoctorId();
  }

  protected onInstitutionChange(value: string | null): void {
    this.selectedInstitution.set(value);
    if (value) {
      const institutions =
        this.institutionStoreService.getAvailableInstitutions();
      const selectedInstitution = institutions.find(
        (inst) => inst.id === value,
      );
      if (selectedInstitution) {
        this.institutionStoreService.setInstitution(selectedInstitution);
      }
      this.loadDoctorsForInstitution(value);
    }
  }
  protected onDaySelected(date: Date): void {
    const doctor = this.doctorsForInstitution().find(
      (doc) => doc.doctorId === this.selectedDoctorId(),
    );
    if (!doctor) {
      return;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const isoDate = `${year}-${month}-${day}`;
    console.log(
      doctor.schedules.filter((s) => s.startHour.startsWith(isoDate)),
    );
  }
}
export interface MapToCalendarDaysOptions {
  displayedMonth: number;
  displayedYear: number;
  selectedInstitutionIds?: string[];
}
export function mapSchedulesToCalendarDays(
  schedules: DoctorSchedule[],
  options: MapToCalendarDaysOptions,
): CalendarDay[] {
  const groupedByDate = new Map<
    string,
    {
      appointments: { id: string }[];
      institutionIds: Set<string>;
    }
  >();

  schedules.forEach((schedule) => {
    const dateKey = schedule.startHour.substring(0, 10);

    if (!groupedByDate.has(dateKey)) {
      groupedByDate.set(dateKey, {
        appointments: [],
        institutionIds: new Set(),
      });
    }

    const dayData = groupedByDate.get(dateKey)!;
    dayData.appointments.push({ id: schedule.id });
    dayData.institutionIds.add(schedule.institution.institutionId);
  });

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // Krok 2: Mapujemy zgrupowane dane na obiekty CalendarDay
  const calendarDays: CalendarDay[] = [];

  groupedByDate.forEach((dayData, dateKey) => {
    const date = new Date(dateKey + 'T00:00:00');

    // --- ZMODYFIKOWANA LOGIKA ---
    let isFromThisInstitution: boolean | undefined = undefined;

    // Sprawdzamy, tylko jeśli tablica wybranych placówek nie jest pusta
    if (
      options.selectedInstitutionIds &&
      options.selectedInstitutionIds.length > 0
    ) {
      // isFromThisInstitution będzie 'true', jeśli chociaż JEDEN z wybranych ID placówek
      // znajduje się w zbiorze ID placówek dla tego dnia.
      isFromThisInstitution = options.selectedInstitutionIds.some((id) =>
        dayData.institutionIds.has(id),
      );
    }

    const calendarDay: CalendarDay = {
      date: date,
      dayNumber: date.getDate(),
      isCurrentMonth:
        date.getFullYear() === options.displayedYear &&
        date.getMonth() === options.displayedMonth,
      isToday: date.getTime() === today.getTime(), // Dziś jest 13 października 2025
      hasAppointments: true,
      isSelected: false,
      isFromThisInstitution: isFromThisInstitution,
      appointments: dayData.appointments,
    };

    calendarDays.push(calendarDay);
  });

  calendarDays.sort((a, b) => a.date.getTime() - b.date.getTime());

  return calendarDays;
}
