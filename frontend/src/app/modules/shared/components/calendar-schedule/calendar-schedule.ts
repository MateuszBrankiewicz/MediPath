import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import {
  AvailableDay,
  CalendarModel,
} from '../../../../core/models/schedule.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  generateCalendarDays,
  isSameDay,
} from '../../../../utils/createCalendarUtil';
import { AcceptActionDialogComponent } from '../ui/accept-action-dialog/accept-action-dialog-component';
import { TimeSlot } from '../ui/search-result.component/search-result.model';
import {
  dayNames,
  monthNames,
  timeOptions,
} from './calendar-schedule.constants';

export interface TimeOption {
  label: string;
  value: string;
}

interface ScheduleTimeEvent {
  date: Date;
  startTime: string;
  endTime: string;
  customTime?: string;
}

interface DateTimeEvent {
  date: Date;
  time: string;
  slotId?: string;
}

@Component({
  selector: 'app-calendar-schedule',
  imports: [CommonModule, ButtonModule],
  templateUrl: './calendar-schedule.html',
  styleUrl: './calendar-schedule.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CalendarSchedule {
  protected translationService = inject(TranslationService);
  private dialogService = inject(DialogService);

  public readonly size = input<'small' | 'medium' | 'large'>('medium');
  public readonly editMode = input<boolean>(false);
  public readonly initialSelectedDate = input<Date | string | null>(null);
  public readonly initialSelectedTime = input<string | null>(null);
  public readonly institutionId = input<string | null>(null);
  public readonly availableAppointments = input<AvailableDay[]>([]);

  public readonly dateTimeSelected = output<DateTimeEvent>();
  public readonly scheduleTimeSelected = output<ScheduleTimeEvent>();
  public readonly checkIfDateAppointmentIsFromThisInstitution = output<{
    date: Date;
    isFromInstitution: boolean;
  }>();

  public readonly selectedDate = signal<Date | null>(null);
  public readonly selectedTime = signal<string | null>(null);
  public readonly currentMonth = signal<Date>(new Date());
  public readonly selectedStartTime = signal<string>('');
  public readonly selectedEndTime = signal<string>('');
  public readonly customTimeInputStart = signal<string>('');
  public readonly customTimeInputEnd = signal<string>('');

  public Math = Math;
  protected readonly dayNames = dayNames;
  protected readonly monthNames = monthNames;
  protected timeOptions: TimeOption[] = timeOptions;

  public readonly calendarDays = computed(() =>
    generateCalendarDays({
      currentMonth: this.currentMonth(),
      selectedDate: this.selectedDate(),
      hasAppointmentsOnDate: (date) => this.isDateAvailable(date),
    }),
  );

  public readonly availableTimes = computed(() => {
    const selected = this.selectedDate();
    if (!selected) return [];

    if (this.editMode()) {
      return this.getEditModeTimeSlots(selected);
    }

    return this.getViewModeTimeSlots(selected);
  });

  protected readonly availableEndTimes = computed(() => {
    const selectedStart = this.selectedStartTime();
    const times = this.availableTimes();

    if (!selectedStart) return times;

    const startTotalMinutes = this.timeToMinutes(selectedStart);
    const nextUnavailableMinutes = this.findNextUnavailableSlot(
      times,
      startTotalMinutes,
    );

    return times.filter((option) => {
      const optionMinutes = this.timeToMinutes(option.time);
      const isAfterStart = optionMinutes > startTotalMinutes;
      const isBeforeBlocked =
        nextUnavailableMinutes === undefined ||
        optionMinutes <= nextUnavailableMinutes - 30;

      return isAfterStart && isBeforeBlocked && option.available;
    });
  });

  public readonly monthYearDisplay = computed(() => {
    const current = this.currentMonth();
    return `${monthNames[current.getMonth()]} ${current.getFullYear()}`;
  });

  constructor() {
    effect(() => {
      this.initializeCalendar();
    });
  }

  public previousMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() - 1, 1),
    );
  }

  public nextMonth(): void {
    const current = this.currentMonth();
    this.currentMonth.set(
      new Date(current.getFullYear(), current.getMonth() + 1, 1),
    );
  }

  public onDateSelect(calendarDay: CalendarModel): void {
    if (!calendarDay.isCurrentMonth) return;

    if (this.editMode()) {
      this.handleEditModeSelection(calendarDay);
    } else {
      this.handleViewModeSelection(calendarDay);
    }
  }

  public onTimeSelect(slot: TimeSlot): void {
    const date = this.selectedDate();
    if (!date) return;

    this.selectedTime.set(slot.time);
    this.dateTimeSelected.emit({
      date,
      time: slot.time,
      slotId: slot.id,
    });
  }

  public onStartTimeSelect(time: string): void {
    this.selectedStartTime.set(time);
    this.emitScheduleTime();
  }

  public onEndTimeSelect(time: string): void {
    this.selectedEndTime.set(time);
    this.emitScheduleTime();
  }

  public onCustomTimeStartChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.customTimeInputStart.set(input.value);
    this.selectedStartTime.set('');
    this.emitScheduleTime();
  }

  public onCustomTimeEndChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.customTimeInputEnd.set(input.value);
    this.emitScheduleTime();
  }

  public isDateSelected(calendarDay: CalendarModel): boolean {
    const selected = this.selectedDate();
    return selected ? isSameDay(selected, calendarDay.date) : false;
  }

  public getFirstColumn(): TimeSlot[] {
    const times = this.availableTimes();
    const half = Math.ceil(times.length / 2);
    return times.slice(0, half);
  }

  public getSecondColumn(): TimeSlot[] {
    const times = this.availableTimes();
    const half = Math.ceil(times.length / 2);
    return times.slice(half);
  }

  public isDaySelectable(calendarDay: CalendarModel): boolean {
    if (this.editMode()) {
      return calendarDay.isCurrentMonth && !calendarDay.hasAppointments;
    }
    return calendarDay.hasAppointments;
  }

  private initializeCalendar(): void {
    const appointments = this.availableAppointments();
    const initialDate = this.initialSelectedDate();
    const initialTime = this.initialSelectedTime();

    if (initialDate) {
      const date = this.parseDate(initialDate);
      this.selectedDate.set(date);
      this.selectedTime.set(initialTime);
    }

    if (appointments.length > 0) {
      const targetDate = initialDate
        ? this.parseDate(initialDate)
        : this.findFirstAvailableDate(appointments);

      if (targetDate) {
        this.navigateToMonth(targetDate);
      }
    }
  }

  private parseDate(date: Date | string): Date {
    return typeof date === 'string' ? new Date(date) : date;
  }

  private findFirstAvailableDate(appointments: AvailableDay[]): Date | null {
    if (appointments.length === 0) return null;

    const sortedAppointments = [...appointments].sort((a, b) => {
      const dateA = this.parseDate(a.date);
      const dateB = this.parseDate(b.date);
      return dateA.getTime() - dateB.getTime();
    });

    return this.parseDate(sortedAppointments[0].date);
  }

  private navigateToMonth(targetDate: Date): void {
    const targetMonth = new Date(
      targetDate.getFullYear(),
      targetDate.getMonth(),
      1,
    );
    this.currentMonth.set(targetMonth);
  }

  private isDateAvailable(date: Date): boolean {
    return this.availableAppointments().some((app) => {
      const appDate = this.parseDate(app.date);
      return isSameDay(appDate, date);
    });
  }

  private getEditModeTimeSlots(selected: Date): TimeSlot[] {
    const appointmentsForDay = this.findAppointmentForDate(selected);
    const bookedTimes = appointmentsForDay
      ? appointmentsForDay.slots.map((slot) => slot.time)
      : [];

    return this.timeOptions.map((option) => ({
      id: option.value,
      time: option.value,
      label: option.label,
      booked: false,
      available: !bookedTimes.includes(option.value),
    }));
  }

  private getViewModeTimeSlots(selected: Date): TimeSlot[] {
    const appointment = this.findAppointmentForDate(selected);
    return appointment ? appointment.slots : [];
  }

  private findAppointmentForDate(date: Date): AvailableDay | undefined {
    return this.availableAppointments().find((app) => {
      const appDate = this.parseDate(app.date);
      return isSameDay(appDate, date);
    });
  }

  private handleEditModeSelection(calendarDay: CalendarModel): void {
    if (calendarDay.hasAppointments) {
      this.showAppointmentExistsDialog(calendarDay);
    } else {
      this.selectDate(calendarDay.date);
    }
  }

  private handleViewModeSelection(calendarDay: CalendarModel): void {
    if (!calendarDay.hasAppointments) return;
    this.selectDate(calendarDay.date);
  }

  private showAppointmentExistsDialog(calendarDay: CalendarModel): void {
    const ref = this.dialogService.open(AcceptActionDialogComponent, {
      data: {
        message: this.translationService.translate(
          'shared.calendar.editModeAppointmentExists',
        ),
        acceptLabel: this.translationService.translate('shared.yes'),
        rejectLabel: this.translationService.translate('shared.no'),
      },
    });

    ref.onClose.subscribe((accepted) => {
      if (accepted) {
        this.selectDate(calendarDay.date);
      }
    });
  }

  private selectDate(date: Date): void {
    this.selectedDate.set(date);
    this.selectedTime.set(null);
    this.selectedStartTime.set('');
    this.selectedEndTime.set('');
    this.customTimeInputStart.set('');
    this.customTimeInputEnd.set('');
  }

  private emitScheduleTime(): void {
    const date = this.selectedDate();
    const startTime = this.selectedStartTime() || this.customTimeInputStart();
    const endTime = this.selectedEndTime() || this.customTimeInputEnd();

    if (date && startTime && endTime) {
      this.scheduleTimeSelected.emit({
        date,
        startTime,
        endTime,
      });
    }
  }

  private timeToMinutes(time: string): number {
    const [hour, minute] = time.split(':').map(Number);
    return hour * 60 + minute;
  }

  private findNextUnavailableSlot(
    times: TimeSlot[],
    startMinutes: number,
  ): number | undefined {
    return times
      .filter((option) => !option.available)
      .map((option) => this.timeToMinutes(option.time))
      .filter((minutes) => minutes > startMinutes)
      .sort((a, b) => a - b)[0];
  }
}
