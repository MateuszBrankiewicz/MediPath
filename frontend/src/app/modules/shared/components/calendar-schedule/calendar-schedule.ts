import { Component, computed, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';

interface TimeSlot {
  time: string;
  available: boolean;
}

interface AvailableDay {
  date: Date;
  slots: TimeSlot[];
}

interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  hasAppointments: boolean;
  dayNumber: number;
}

@Component({
  selector: 'app-calendar-schedule',
  imports: [CommonModule, ButtonModule],
  templateUrl: './calendar-schedule.html',
  styleUrl: './calendar-schedule.scss',
})
export class CalendarSchedule {
  // Output event for parent component
  public readonly dateTimeSelected = output<{ date: Date; time: string }>();

  // Reactive state
  public readonly selectedDate = signal<Date | null>(null);
  public readonly selectedTime = signal<string | null>(null);
  public readonly currentMonth = signal<Date>(new Date());

  // Mock data for available appointments
  private readonly availableAppointments = signal<AvailableDay[]>([
    {
      date: new Date(2025, 8, 16),
      slots: [
        { time: '8:00 am', available: true },
        { time: '8:20 am', available: true },
        { time: '8:40 am', available: true },
        { time: '9:20 am', available: true },
        { time: '10:40 am', available: true },
        { time: '11:20 am', available: true },
        { time: '11:40 am', available: true },
        { time: '12:00 am', available: false },
        { time: '1:20 pm', available: true },
        { time: '1:40 pm', available: true },
      ],
    },
    {
      date: new Date(2025, 8, 17),
      slots: [
        { time: '8:00 am', available: true },
        { time: '9:00 am', available: true },
        { time: '10:00 am', available: false },
        { time: '11:00 am', available: true },
        { time: '2:00 pm', available: true },
        { time: '3:00 pm', available: true },
      ],
    },
  ]);

  public readonly monthNames = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

  public readonly dayNames = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  public Math = Math;

  public readonly calendarDays = computed(() => {
    const currentMonth = this.currentMonth();
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayOfWeek = (firstDayOfMonth.getDay() + 6) % 7; // Monday = 0

    const days: CalendarDay[] = [];
    const today = new Date();

    // Previous month days
    const prevMonth = new Date(year, month - 1, 0);
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const date = new Date(year, month - 1, prevMonth.getDate() - i);
      days.push({
        date,
        isCurrentMonth: false,
        isToday: false,
        hasAppointments: this.isDateAvailable(date),
        dayNumber: date.getDate(),
      });
    }

    for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
      const date = new Date(year, month, day);
      days.push({
        date,
        isCurrentMonth: true,
        isToday: date.toDateString() === today.toDateString(),
        hasAppointments: this.isDateAvailable(date),
        dayNumber: day,
      });
    }

    const remainingDays = 42 - days.length; // 6 rows × 7 days
    for (let day = 1; day <= remainingDays; day++) {
      const date = new Date(year, month + 1, day);
      days.push({
        date,
        isCurrentMonth: false,
        isToday: false,
        hasAppointments: this.isDateAvailable(date),
        dayNumber: day,
      });
    }

    return days;
  });

  public readonly availableTimes = computed(() => {
    const selected = this.selectedDate();
    if (!selected) return [];

    const appointment = this.availableAppointments().find(
      (app) => app.date.toDateString() === selected.toDateString(),
    );

    return appointment?.slots || [];
  });

  public readonly monthYearDisplay = computed(() => {
    const current = this.currentMonth();
    return `${this.monthNames[current.getMonth()]} ${current.getFullYear()}`;
  });

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

  public onDateSelect(calendarDay: CalendarDay): void {
    console.log('Calendar day clicked:', {
      date: calendarDay.date,
      hasAppointments: calendarDay.hasAppointments,
      isCurrentMonth: calendarDay.isCurrentMonth,
      dayNumber: calendarDay.dayNumber,
    });

    if (!calendarDay.hasAppointments) {
      console.log('No appointments for this date');
      return;
    }

    if (!calendarDay.isCurrentMonth) {
      console.log('Date is not in current month');
      return;
    }

    console.log('Setting selected date to:', calendarDay.date);
    this.selectedDate.set(calendarDay.date);
    this.selectedTime.set(null);

    console.log('Selected date is now:', this.selectedDate());
  }

  public onTimeSelect(time: string): void {
    if (!this.selectedDate()) return;

    this.selectedTime.set(time);

    this.dateTimeSelected.emit({
      date: this.selectedDate()!,
      time: time,
    });
  }

  public isDateAvailable = (date: Date): boolean => {
    return this.availableAppointments().some(
      (app) => app.date.toDateString() === date.toDateString(),
    );
  };

  public isDateSelected(calendarDay: CalendarDay): boolean {
    const selected = this.selectedDate();
    return selected
      ? selected.toDateString() === calendarDay.date.toDateString()
      : false;
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
}
