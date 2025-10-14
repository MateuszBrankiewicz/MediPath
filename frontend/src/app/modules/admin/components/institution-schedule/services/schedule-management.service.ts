import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  DoctorSchedule,
  DoctorWithSchedule,
} from '../../../../../core/models/doctor.model';
import { ScheduleService } from '../../../../../core/services/schedule/schedule.service';
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
  private destroyRef = inject(DestroyRef);

  // State signals
  private selectedDate = signal<Date | null>(null);
  private selectedDoctorId = signal<string | null>(null);
  private selectedInstitutionId = signal<string | null>(null);
  private doctorsForInstitution = signal<DoctorWithSchedule[]>([]);
  private selectedDaySchedules = signal<DoctorSchedule[]>([]);
  private isLoading = signal<boolean>(false);

  // Getters for read-only access
  getSelectedDate = this.selectedDate.asReadonly();
  getSelectedDoctorId = this.selectedDoctorId.asReadonly();
  getSelectedInstitutionId = this.selectedInstitutionId.asReadonly();
  getDoctorsForInstitution = this.doctorsForInstitution.asReadonly();
  getSelectedDaySchedules = this.selectedDaySchedules.asReadonly();
  getIsLoading = this.isLoading.asReadonly();

  /**
   * Set selected date and load schedules for that day
   */
  setSelectedDate(date: Date): void {
    this.selectedDate.set(date);
    this.loadSchedulesForDay(date);
  }

  /**
   * Set selected doctor
   */
  setSelectedDoctor(doctorId: string): void {
    this.selectedDoctorId.set(doctorId);
  }

  /**
   * Set selected institution
   */
  setSelectedInstitution(institutionId: string): void {
    this.selectedInstitutionId.set(institutionId);
  }

  /**
   * Set doctors for institution
   */
  setDoctorsForInstitution(doctors: DoctorWithSchedule[]): void {
    this.doctorsForInstitution.set(doctors);
  }

  /**
   * Get formatted date string
   */
  getFormattedDate(date: Date): string {
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

  /**
   * Get selected doctor name
   */
  getSelectedDoctorName(): string {
    const doctorId = this.selectedDoctorId();
    if (!doctorId) return '';

    const doctor = this.doctorsForInstitution().find(
      (d) => d.doctorId === doctorId,
    );
    return doctor ? `${doctor.doctorName} ${doctor.doctorSurname}` : '';
  }

  /**
   * Format time from datetime string
   */
  formatTime(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    return date.toLocaleTimeString('pl-PL', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  /**
   * Format time for input field (HH:mm)
   */
  formatTimeForInput(dateTimeString: string): string {
    const date = new Date(dateTimeString);
    return date.toTimeString().slice(0, 5);
  }

  /**
   * Load schedules for selected day
   */
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

  /**
   * Update single schedule slot
   */
  updateSingleSlot(slot: DoctorSchedule, formData: SingleEditFormData): void {
    this.isLoading.set(true);

    const dateStr = slot.startHour.split('T')[0].split(' ')[0];
    const startHour = `${dateStr} ${formData.startTime}:00`;
    const endHour = `${dateStr} ${formData.endTime}:00`;

    this.scheduleService
      .editSchedule(slot.id, {
        startHour: startHour,
        endHour: endHour,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.isLoading.set(false);

        // Update local state
        const updatedSchedules = this.selectedDaySchedules().map((s) => {
          if (s.id === slot.id) {
            return {
              ...s,
              startHour: `${slot.startHour.split('T')[0]}T${formData.startTime}:00`,
              endHour: `${slot.endHour.split('T')[0]}T${formData.endTime}:00`,
            };
          }
          return s;
        });
        this.selectedDaySchedules.set(updatedSchedules);
      });
  }

  /**
   * Generate time slots based on bulk edit parameters
   */
  generateTimeSlots(formData: BulkEditFormData): DoctorSchedule[] {
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
    const dateStr = selectedDate.toISOString().split('T')[0]; // YYYY-MM-DD

    // Parse start and end times
    const [startHours, startMinutes] = formData.startTime
      .split(':')
      .map(Number);
    const [endHours, endMinutes] = formData.endTime.split(':').map(Number);

    const startTimeMinutes = startHours * 60 + startMinutes;
    const endTimeMinutes = endHours * 60 + endMinutes;

    // Generate slots
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

      // Stop if end time would exceed the specified end time
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

  /**
   * Save bulk changes (replace all slots for the day)
   */
  saveBulkChanges(formData: BulkEditFormData): void {
    const newSlots = this.generateTimeSlots(formData);

    // Update local state immediately
    this.selectedDaySchedules.set(newSlots);

    // TODO: Implement API call to save bulk changes
    // this.scheduleService.createBulkSchedule(...)
    console.log('Generated slots:', newSlots);
  }

  /**
   * Clear all state
   */
  clearState(): void {
    this.selectedDate.set(null);
    this.selectedDaySchedules.set([]);
  }
}
