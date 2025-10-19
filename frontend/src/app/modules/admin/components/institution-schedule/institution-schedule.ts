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
import { DoctorWithSchedule } from '../../../../core/models/doctor.model';
import { CalendarDay } from '../../../../core/models/schedule.model';
import { DateTimeService } from '../../../../core/services/date-time/date-time.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { mapSchedulesToCalendarDays } from '../../../../utils/calendarMapper';
import { Calendar } from '../../../shared/components/calendar/calendar';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { InstitutionOption } from '../admin-dashboard/widgets/institution-select-card';
import { ScheduleDetailsDialog } from './components/schedule-details-dialog/schedule-details-dialog';
import { ScheduleManagementService } from './services/schedule-management.service';

@Component({
  selector: 'app-institution-schedule',
  imports: [Calendar, ButtonModule, ScheduleDetailsDialog],
  templateUrl: './institution-schedule.html',
  styleUrl: './institution-schedule.scss',
})
export class InstitutionSchedule implements OnInit {
  protected translationService = inject(TranslationService);
  protected scheduleManagementService = inject(ScheduleManagementService);
  protected dateTimeService = inject(DateTimeService);

  public currentMonth = signal<Date>(new Date());
  public readonly currentDayNumber = new Date().getDate();
  public readonly currentDayName = this.dateTimeService.getDayName(new Date());
  private router = inject(Router);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private institutionService = inject(InstitutionService);

  protected selectedDoctorId = signal<string | null>(null);
  protected readonly institutionOptions = signal<InstitutionOption[]>([]);
  protected selectedInstitution = signal<string | null>(null);
  protected doctorsForInstitution = signal<DoctorWithSchedule[]>([]);

  protected showScheduleModal = signal<boolean>(false);
  protected selectedDate = signal<Date | null>(null);

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
}
