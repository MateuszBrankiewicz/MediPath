import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputNumber } from 'primeng/inputnumber';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Select } from 'primeng/select';
import { catchError, of } from 'rxjs';
import { DoctorProfile } from '../../../../core/models/doctor.model';
import { AvailableDay } from '../../../../core/models/schedule.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { ScheduleService } from '../../../../core/services/schedule/schedule.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { InstitutionOption } from '../admin-dashboard/widgets/institution-select-card';

@Component({
  selector: 'app-create-schedule',
  imports: [
    CommonModule,
    CalendarSchedule,
    CardModule,
    Select,
    ButtonModule,
    FormsModule,
    InputNumber,
    FloatLabelModule,
    ProgressSpinnerModule,
  ],
  templateUrl: './create-schedule.html',
  styleUrl: './create-schedule.scss',
})
export class CreateSchedule implements OnInit {
  protected institutionStoreService = inject(InstitutionStoreService);
  private institutionService = inject(InstitutionService);
  protected schedulesForDoctor = signal<AvailableDay[]>([]);
  protected translationService = inject(TranslationService);
  private toastService = inject(ToastService);
  protected doctorOptions = signal<{ name: string; value: string }[]>([]);
  protected selectedDoctorId = signal<string>('');
  protected intervalMinutes = signal<number>(0);
  protected doctorsCache = signal<DoctorProfile[]>([]);
  protected selectedInstitution = signal<InstitutionOption | null>(null);
  private scheduleService = inject(ScheduleService);
  protected isLoading = signal<boolean>(false);
  protected isSending = signal<boolean>(false);
  protected dateTimeSelected = signal<{
    date: Date;
    startTime: string;
    endTime: string;
  } | null>(null);

  protected institutionOptions = signal<InstitutionOption[]>([]);

  protected selectedDoctorName = computed(() => {
    const id = this.selectedDoctorId();
    const doc = this.doctorsCache().find((d) => d.doctorId === id);
    return doc ? `${doc.doctorName} ${doc.doctorSurname}`.trim() : '';
  });

  private destroyRef = inject(DestroyRef);
  ngOnInit() {
    this.initDoctors();
    const availableInstitutions =
      this.institutionStoreService.getAvailableInstitutions();
    if (availableInstitutions.length === 0) {
      this.loadInstitutions();
    }
    this.institutionOptions.set(availableInstitutions);

    const institution = this.institutionStoreService.getInstitution();
    if (institution.id) {
      const foundInstitution = availableInstitutions.find(
        (inst) => inst.id === institution.id,
      );
      this.selectedInstitution.set(foundInstitution || null);
    } else {
      this.selectedInstitution.set(null);
    }
  }

  onScheduleTimeSelected(event: {
    date: Date;
    startTime: string;
    endTime: string;
    customTime?: string;
  }) {
    this.dateTimeSelected.update(() => {
      return event;
    });
  }

  protected onDoctorChange(value: string) {
    this.selectedDoctorId.set(value);
    this.updateSchedulesForSelectedDoctor();
  }

  private updateSchedulesForSelectedDoctor() {
    const docId = this.selectedDoctorId();
    const doc = this.doctorsCache().find((d) => d.doctorId === docId);
    if (!doc) {
      this.schedulesForDoctor.set([]);
      return;
    }

    const daysMap = new Map<string, AvailableDay>();
    doc.doctorSchedules.forEach((s) => {
      const dateKey = s.startHour.split(' ')[0];
      const time = s.startHour.split(' ')[1]?.substring(0, 5) ?? '';
      if (!daysMap.has(dateKey)) {
        daysMap.set(dateKey, { date: dateKey, slots: [] });
      }
      daysMap.get(dateKey)!.slots.push({
        id: s.id,
        time,
        available: !s.booked,
        booked: s.booked,
        institutionId: s.institution.institutionId,
      });
    });

    const availableDays = Array.from(daysMap.values()).sort((a, b) => {
      const aDate = typeof a.date === 'string' ? a.date : a.date.toISOString();
      const bDate = typeof b.date === 'string' ? b.date : b.date.toISOString();
      return aDate.localeCompare(bDate);
    });

    this.schedulesForDoctor.set(availableDays);
  }
  protected onInstitutionChange(value: InstitutionOption) {
    this.selectedInstitution.set(value);
    this.institutionStoreService.setInstitution(value);

    this.initDoctors();
  }
  protected today = new Date();

  protected onIntervalChange(value: number | null) {
    if (typeof value === 'number' && value > 0) {
      this.intervalMinutes.set(value);
    }
  }
  protected onCreateSchedule() {
    this.isSending.set(true);
    const doctorId = this.selectedDoctorId();
    const dateTime = this.dateTimeSelected();
    const interval = this.intervalMinutes();
    const institutionId = this.institutionStoreService.getInstitution().id;

    if (!doctorId || !dateTime || interval <= 0) {
      this.toastService.showError(
        this.translationService.translate('admin.createSchedule.validation'),
      );
      return;
    }

    const startTimeFormatted = this.formatDateTime(
      dateTime.date,
      dateTime.startTime,
    );

    const endTimeFormatted = this.formatDateTime(
      dateTime.date,
      dateTime.endTime,
    );

    const pad = (n: number) => n.toString().padStart(2, '0');
    const intervalFormatted = `${pad(Math.floor(interval / 60))}:${pad(interval % 60)}:00`;

    this.scheduleService
      .createSchedule({
        doctorID: doctorId,
        institutionID: institutionId,
        startHour: startTimeFormatted,
        endHour: endTimeFormatted,
        interval: intervalFormatted,
      })
      .pipe(
        catchError(() => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.createSchedule.scheduleCreationError',
            ),
          );
          this.isSending.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((response) => {
        if (response) {
          this.toastService.showSuccess(
            this.translationService.translate(
              'admin.createSchedule.scheduleCreated',
            ),
          );
          this.dateTimeSelected.set(null);
          this.intervalMinutes.set(0);
          this.updateSchedulesForSelectedDoctor();
          this.isSending.set(false);
        }
      });
  }
  private initDoctors() {
    this.isLoading.set(true);
    const selectedInstitution =
      this.institutionStoreService.selectedInstitution();
    if (!selectedInstitution) {
      return;
    }

    this.institutionService
      .getDoctorsForInstitution(selectedInstitution.id)
      .pipe(
        catchError(() => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.createSchedule.doctorsLoadError',
            ),
          );
          this.isLoading.set(false);
          return of([]);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((doctors: DoctorProfile[]) => {
        this.doctorsCache.set(doctors);
        this.doctorOptions.set(
          doctors.map((d) => ({
            name: `${d.doctorName} ${d.doctorSurname}`.trim(),
            value: d.doctorId,
          })),
        );
        if (!this.selectedDoctorId() && doctors.length > 0) {
          this.selectedDoctorId.set(doctors[0].doctorId);
        }

        this.updateSchedulesForSelectedDoctor();
        this.isLoading.set(false);
      });
  }

  private loadInstitutions() {
    this.isLoading.set(true);
    this.institutionStoreService
      .loadAvailableInstitutions()
      .pipe(
        catchError(() => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.createSchedule.institutionsLoadError',
            ),
          );
          this.isLoading.set(false);
          return of([]);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((institutions) => {
        this.institutionOptions.set(institutions);
        const institution = this.institutionStoreService.getInstitution();
        if (institution.id) {
          const foundInstitution = institutions.find(
            (inst) => inst.id === institution.id,
          );
          this.selectedInstitution.set(foundInstitution || null);
        } else {
          this.selectedInstitution.set(null);
        }
        this.isLoading.set(false);
      });
  }
  private formatDateTime = (date: Date, time: string) => {
    const pad = (n: number) => n.toString().padStart(2, '0');

    const [h, m] = time.split(':');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(Number(h))}:${pad(Number(m))}:00`;
  };
}
