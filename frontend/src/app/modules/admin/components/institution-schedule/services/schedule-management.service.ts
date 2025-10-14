import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of } from 'rxjs';
import {
  DoctorSchedule,
  DoctorWithSchedule,
} from '../../../../../core/models/doctor.model';
import { ScheduleService } from '../../../../../core/services/schedule/schedule.service';
import { ToastService } from '../../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

export interface BulkEditFormData {
  startTime: string;
  endTime: string;
  interval: number;
}

export interface SingleEditFormData {
  startTime: string;
  endTime: string;
}

@Injectable({
  providedIn: 'root',
})
export class ScheduleManagementService {
  private scheduleService = inject(ScheduleService);
  private translationService = inject(TranslationService);
  private toastService = inject(ToastService);
  private destroyRef = inject(DestroyRef);

  private selectedDate = signal<Date | null>(null);
  private selectedDoctorId = signal<string | null>(null);
  private selectedInstitutionId = signal<string | null>(null);
  private doctorsForInstitution = signal<DoctorWithSchedule[]>([]);
  private selectedDaySchedules = signal<DoctorSchedule[]>([]);
  private isLoading = signal<boolean>(false);
  public getSelectedDate = this.selectedDate.asReadonly();
  public getSelectedDoctorId = this.selectedDoctorId.asReadonly();
  public getSelectedInstitutionId = this.selectedInstitutionId.asReadonly();
  public getDoctorsForInstitution = this.doctorsForInstitution.asReadonly();
  public getSelectedDaySchedules = this.selectedDaySchedules.asReadonly();
  public getIsLoading = this.isLoading.asReadonly();

  public setSelectedDate(date: Date): void {
    this.selectedDate.set(date);
    this.loadSchedulesForDay(date);
  }

  public setSelectedDoctor(doctorId: string): void {
    this.selectedDoctorId.set(doctorId);
  }

  public setSelectedInstitution(institutionId: string): void {
    this.selectedInstitutionId.set(institutionId);
  }

  public setDoctorsForInstitution(doctors: DoctorWithSchedule[]): void {
    this.doctorsForInstitution.set(doctors);
  }

  public getFormattedDate(date: Date): string {
    const dayKeys = [
      'weekdays.sunday',
      'weekdays.monday',
      'weekdays.tuesday',
      'weekdays.wednesday',
      'weekdays.thursday',
      'weekdays.friday',
      'weekdays.saturday',
    ];
    const dayName = this.translationService.translate(dayKeys[date.getDay()]);
    return `${dayName}, ${date.getDate()}.${date.getMonth() + 1}.${date.getFullYear()}`;
  }

  public getSelectedDoctorName(): string {
    const doctorId = this.selectedDoctorId();
    if (!doctorId) return '';

    const doctor = this.doctorsForInstitution().find(
      (d) => d.doctorId === doctorId,
    );
    return doctor ? `${doctor.doctorName} ${doctor.doctorSurname}` : '';
  }

  public formatTime(dateTimeString: string): string {
    const normalizedString = dateTimeString.replace(' ', 'T');
    const date = new Date(normalizedString);

    if (isNaN(date.getTime())) {
      console.error('Invalid date string:', dateTimeString);
      return 'Invalid Date';
    }

    return date.toLocaleTimeString('pl-PL', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  public formatTimeForInput(dateTimeString: string): string {
    const normalizedString = dateTimeString.replace(' ', 'T');
    const date = new Date(normalizedString);

    if (isNaN(date.getTime())) {
      console.error('Invalid date string:', dateTimeString);
      return '';
    }

    return date.toTimeString().slice(0, 5);
  }

  private loadSchedulesForDay(date: Date): void {
    const doctor = this.doctorsForInstitution().find(
      (doc) => doc.doctorId === this.selectedDoctorId(),
    );

    if (!doctor) {
      this.selectedDaySchedules.set([]);
      return;
    }
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const isoDate = `${year}-${month}-${day}`;

    const schedulesForDay = doctor.schedules.filter((s) =>
      s.startHour.startsWith(isoDate),
    );

    this.selectedDaySchedules.set(schedulesForDay);
  }

  public updateSingleSlot(
    slot: DoctorSchedule,
    formData: SingleEditFormData,
    onSuccess?: () => void,
  ): void {
    this.isLoading.set(true);

    const dateStr = slot.startHour.includes('T')
      ? slot.startHour.split('T')[0]
      : slot.startHour.split(' ')[0];
    const startHour = `${dateStr} ${formData.startTime}:00`;
    const endHour = `${dateStr} ${formData.endTime}:00`;

    this.scheduleService
      .editSchedule(slot.id, {
        startHour: startHour,
        endHour: endHour,
      })
      .pipe(
        catchError((error) => {
          console.error('Error updating schedule:', error);
          this.toastService.showError('doctor.schedule.error.update');
          this.isLoading.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((response) => {
        if (response === null) return;

        this.isLoading.set(false);

        const updatedSchedules = this.selectedDaySchedules().map((s) => {
          if (s.id === slot.id) {
            return {
              ...s,
              startHour: `${dateStr} ${formData.startTime}:00`,
              endHour: `${dateStr} ${formData.endTime}:00`,
            };
          }
          return s;
        });
        this.selectedDaySchedules.set(updatedSchedules);

        this.toastService.showSuccess('doctor.schedule.success.update');

        if (onSuccess) {
          onSuccess();
        }
      });
  }

  public generateTimeSlots(formData: BulkEditFormData): DoctorSchedule[] {
    const selectedDate = this.selectedDate();
    const selectedDoctorId = this.selectedDoctorId();
    const selectedInstitutionId = this.selectedInstitutionId();

    if (!selectedDate || !selectedDoctorId || !selectedInstitutionId) {
      return [];
    }

    const doctor = this.doctorsForInstitution().find(
      (d) => d.doctorId === selectedDoctorId,
    );

    if (!doctor) {
      return [];
    }

    const slots: DoctorSchedule[] = [];
    const year = selectedDate.getFullYear();
    const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(selectedDate.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;

    const [startHours, startMinutes] = formData.startTime
      .split(':')
      .map(Number);
    const [endHours, endMinutes] = formData.endTime.split(':').map(Number);

    const startTimeMinutes = startHours * 60 + startMinutes;
    const endTimeMinutes = endHours * 60 + endMinutes;

    for (
      let currentMinutes = startTimeMinutes;
      currentMinutes < endTimeMinutes;
      currentMinutes += formData.interval
    ) {
      const slotStartHours = Math.floor(currentMinutes / 60);
      const slotStartMinutes = currentMinutes % 60;

      const slotEndMinutes = currentMinutes + formData.interval;
      const slotEndHours = Math.floor(slotEndMinutes / 60);
      const slotEndMinutesRemainder = slotEndMinutes % 60;

      if (slotEndMinutes > endTimeMinutes) {
        break;
      }

      const startDateTime = `${dateStr}T${String(slotStartHours).padStart(2, '0')}:${String(slotStartMinutes).padStart(2, '0')}:00`;
      const endDateTime = `${dateStr}T${String(slotEndHours).padStart(2, '0')}:${String(slotEndMinutesRemainder).padStart(2, '0')}:00`;

      slots.push({
        id: `generated-${currentMinutes}`,
        startHour: startDateTime,
        endHour: endDateTime,
        booked: false,
        doctor: {
          userId: doctor.doctorId,
          doctorName: doctor.doctorName,
          doctorSurname: doctor.doctorSurname,
          specialisations: [],
        },
        institution: {
          institutionId: selectedInstitutionId,
          institutionName: '',
        },
      });
    }

    return slots;
  }

  public saveBulkChanges(
    formData: BulkEditFormData,
    onSuccess?: () => void,
  ): void {
    const selectedDate = this.selectedDate();
    const selectedDoctorId = this.selectedDoctorId();
    const selectedInstitutionId = this.selectedInstitutionId();
    const currentSchedules = this.selectedDaySchedules();

    if (!selectedDate || !selectedDoctorId || !selectedInstitutionId) {
      return;
    }

    let oldStartHour = '';
    let oldEndHour = '';

    if (currentSchedules.length > 0) {
      const sortedSchedules = [...currentSchedules].sort((a, b) =>
        a.startHour.localeCompare(b.startHour),
      );
      oldStartHour = sortedSchedules[0].startHour.replace('T', ' ');
      oldEndHour = sortedSchedules[sortedSchedules.length - 1].endHour.replace(
        'T',
        ' ',
      );
    }

    const year = selectedDate.getFullYear();
    const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(selectedDate.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;

    const newStartHour = `${dateStr} ${formData.startTime}:00`;
    const newEndHour = `${dateStr} ${formData.endTime}:00`;

    this.isLoading.set(true);

    this.scheduleService
      .updateManySchedules({
        doctorID: selectedDoctorId,
        institutionID: selectedInstitutionId,
        startHour: oldStartHour || newStartHour,
        endHour: oldEndHour || newEndHour,
        newStartHour: newStartHour,
        newEndHour: newEndHour,
        newInterval: formData.interval,
      })
      .pipe(
        catchError(() => {
          this.toastService.showError('doctor.schedule.error.bulk_update');
          this.isLoading.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((response) => {
        if (response === null) return;

        this.isLoading.set(false);
        this.loadSchedulesForDay(selectedDate);

        this.toastService.showSuccess('doctor.schedule.success.bulk_update');

        if (onSuccess) {
          onSuccess();
        }
      });
  }

  public clearState(): void {
    this.selectedDate.set(null);
    this.selectedDaySchedules.set([]);
  }

  public deleteSlot(slotId: string, onSuccess?: () => void): void {
    this.isLoading.set(true);

    this.scheduleService
      .deleteSchedule(slotId)
      .pipe(
        catchError(() => {
          this.toastService.showError('doctor.schedule.error.delete');
          this.isLoading.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((response) => {
        if (response === null) return;

        this.isLoading.set(false);
        const updatedSchedules = this.selectedDaySchedules().filter(
          (s) => s.id !== slotId,
        );
        this.selectedDaySchedules.set(updatedSchedules);

        this.toastService.showSuccess('doctor.schedule.success.delete');

        if (onSuccess) {
          onSuccess();
        }
      });
  }
}
