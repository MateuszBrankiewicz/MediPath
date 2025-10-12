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
import { Select } from 'primeng/select';
import { DoctorProfile } from '../../../../core/models/doctor.model';
import { InstitutionShortInfo } from '../../../../core/models/institution.model';
import { AvailableDay } from '../../../../core/models/schedule.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { CalendarSchedule } from '../../../shared/components/calendar-schedule/calendar-schedule';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';

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
  ],
  templateUrl: './create-schedule.html',
  styleUrl: './create-schedule.scss',
})
export class CreateSchedule implements OnInit {
  protected institutionStoreService = inject(InstitutionStoreService);
  private institutionService = inject(InstitutionService);
  protected schedulesForDoctor = signal<AvailableDay[]>([]);
  protected translationService = inject(TranslationService);
  protected doctorOptions = signal<{ name: string; value: string }[]>([]);
  protected selectedDoctorId = signal<string>('');
  protected intervalMinutes = signal<number>(30);
  protected doctorsCache = signal<DoctorProfile[]>([]);
  protected selectedInstitution = signal<InstitutionShortInfo | null>(null);

  protected dateTimeSelected = signal<{
    date: Date;
    startTime: string;
    endTime: string;
  } | null>(null);

  protected institutionOptions = signal<InstitutionShortInfo[]>([]);

  protected selectedDoctorName = computed(() => {
    const id = this.selectedDoctorId();
    const doc = this.doctorsCache().find((d) => d.doctorId === id);
    return doc ? `${doc.doctorName} ${doc.doctorSurname}`.trim() : '';
  });

  private destroyRef = inject(DestroyRef);
  ngOnInit() {
    this.initDoctors();
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
      });
    });

    const availableDays = Array.from(daysMap.values()).sort((a, b) => {
      const aDate = typeof a.date === 'string' ? a.date : a.date.toISOString();
      const bDate = typeof b.date === 'string' ? b.date : b.date.toISOString();
      return aDate.localeCompare(bDate);
    });

    this.schedulesForDoctor.set(availableDays);
  }
  protected onInstitutionChange(value: InstitutionShortInfo) {
    this.selectedInstitution.set(value);
  }
  // Day selection helpers (UI only for now)
  // protected toggleDay(dayIndex: number) {
  //   this.selectedDays.update((set) => {
  //     const next = new Set(set);
  //     if (next.has(dayIndex)) next.delete(dayIndex);
  //     else next.add(dayIndex);
  //     return next;
  //   });
  // }

  // protected isDaySelected(dayIndex: number): boolean {
  //   return this.selectedDays().has(dayIndex);
  // }

  protected onIntervalChange(value: number | null) {
    if (typeof value === 'number' && value > 0) {
      this.intervalMinutes.set(value);
    }
  }

  private initDoctors() {
    this.institutionService
      .getDoctorsForInstitution(
        this.institutionStoreService.getInstitution().institutionId,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((doctors: DoctorProfile[]) => {
        this.doctorsCache.set(doctors);
        this.doctorOptions.set(
          doctors.map((d) => ({
            name: `${d.doctorName} ${d.doctorSurname}`.trim(),
            value: d.doctorId,
          })),
        );

        // Select first doctor by default
        if (!this.selectedDoctorId() && doctors.length > 0) {
          this.selectedDoctorId.set(doctors[0].doctorId);
        }

        this.updateSchedulesForSelectedDoctor();
      });
  }
}
