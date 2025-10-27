import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { DoctorSchedule } from '../../../../core/models/doctor.model';
import { CalendarDay } from '../../../../core/models/schedule.model';
import { DateTimeService } from '../../../../core/services/date-time/date-time.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { mapSchedulesToCalendarDays } from '../../../../utils/calendarMapper';
import { Calendar } from '../../../shared/components/calendar/calendar';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { SelectInstitution } from '../shared/select-institution/select-institution';
import { ScheduleDetailsDialog } from './components/schedule-details-dialog/schedule-details-dialog';
import { ScheduleManagementService } from './services/schedule-management.service';

@Component({
  selector: 'app-institution-schedule',
  imports: [
    Calendar,
    ButtonModule,
    ScheduleDetailsDialog,
    SelectModule,
    FormsModule,
    SelectInstitution,
    ProgressSpinnerModule,
  ],
  templateUrl: './institution-schedule.html',
  styleUrl: './institution-schedule.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionSchedule {
  protected translationService = inject(TranslationService);
  protected scheduleManagementService = inject(ScheduleManagementService);
  protected dateTimeService = inject(DateTimeService);

  protected otherLoading = signal<boolean>(false);

  public currentMonth = signal<Date>(new Date());
  public readonly currentDayNumber = new Date().getDate();
  public readonly currentDayName = this.dateTimeService.getDayName(new Date());
  private router = inject(Router);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private institutionService = inject(InstitutionService);

  protected selectedDoctorId = signal<string | null>(null);

  protected doctorsForInstitution = signal<
    {
      doctorId: string;
      doctorName: string;
      doctorSurname: string;
      schedules: DoctorSchedule[];
    }[]
  >([]);

  protected isLoading = signal<boolean>(false);

  protected selectedInstitution = computed(() => {
    return this.institutionStoreService.selectedInstitution()?.id;
  });

  constructor() {
    effect(() => {
      const institutionId =
        this.institutionStoreService.selectedInstitution()?.id;
      if (!institutionId) {
        return;
      }
      this.loadDoctorsForInstitution(institutionId);
    });

    effect(() => {
      const doctors = this.doctorsForInstitution();
      if (doctors.length > 0 && !this.selectedDoctorId()) {
        this.selectedDoctorId.set(doctors[0].doctorId);
      }
    });
  }

  protected showScheduleModal = signal<boolean>(false);
  protected selectedDate = signal<Date | null>(null);

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
      return mapSchedulesToCalendarDays(schedule, {
        displayedMonth: date.getMonth(),
        displayedYear: date.getFullYear(),
        selectedInstitutionIds: availableInstitutionIds,
      });
    },
  );

  public readonly currentMonthYear = computed(() => {
    return this.dateTimeService.formatMonthYear(this.currentMonth());
  });

  protected readonly selectedDoctorName = computed(() => {
    const doctorId = this.selectedDoctorId();
    if (!doctorId) return '';
    const doctor = this.doctorsForInstitution().find(
      (d) => d.doctorId === doctorId,
    );
    return doctor ? `${doctor.doctorName} ${doctor.doctorSurname}` : '';
  });

  protected saveSchedule(): void {
    this.router.navigate(['/admin/schedule/add-schedule']);
  }

  private loadDoctorsForInstitution(institutionId: string): void {
    this.isLoading.set(true);
    this.otherLoading.set(true);
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
        this.isLoading.set(false);
        this.otherLoading.set(false);
      });
  }

  protected onDaySelected(date: Date): void {
    this.scheduleManagementService.setDoctorsForInstitution(
      this.doctorsForInstitution(),
    );
    this.scheduleManagementService.setSelectedDoctor(
      this.selectedDoctorId() || '',
    );
    this.scheduleManagementService.setSelectedInstitution(
      this.selectedInstitution() || '',
    );
    this.scheduleManagementService.setSelectedDate(date);

    this.selectedDate.set(date);
    this.showScheduleModal.set(true);
  }

  protected closeScheduleModal(): void {
    this.showScheduleModal.set(false);
    this.selectedDate.set(null);
    this.scheduleManagementService.clearState();
  }

  protected onScheduleUpdated(): void {
    const institutionId = this.selectedInstitution();
    if (institutionId) {
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
          this.scheduleManagementService.setDoctorsForInstitution(
            doctorsWithSchedule,
          );

          const selectedDate = this.scheduleManagementService.getSelectedDate();
          if (selectedDate) {
            this.scheduleManagementService.setSelectedDate(selectedDate);
          }
        });
    }
  }

  protected selectDoctor(doctorId: string): void {
    this.selectedDoctorId.set(doctorId);
  }
}
