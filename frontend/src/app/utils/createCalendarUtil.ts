import { CalendarConfig, CalendarModel } from '../core/models/schedule.model';

export function isSameDay(date1: Date, date2: Date): boolean {
  return date1.toDateString() === date2.toDateString();
}

export function generateCalendarDays(config: CalendarConfig): CalendarModel[] {
  const { currentMonth, selectedDate, hasAppointmentsOnDate } = config;
  const year = currentMonth.getFullYear();
  const month = currentMonth.getMonth();
  const today = new Date();

  const firstDayOfMonth = new Date(year, month, 1);
  const lastDayOfMonth = new Date(year, month + 1, 0);
  const firstDayOfWeek = getFirstDayOfWeek(firstDayOfMonth);

  const previousMonthDays = generatePreviousMonthDays(
    year,
    month,
    firstDayOfWeek,
    today,
    selectedDate,
    hasAppointmentsOnDate,
  );

  const currentMonthDays = generateCurrentMonthDays(
    year,
    month,
    lastDayOfMonth,
    today,
    selectedDate,
    hasAppointmentsOnDate,
  );

  const totalDaysGenerated = previousMonthDays.length + currentMonthDays.length;
  const remainingDays = 42 - totalDaysGenerated;

  const nextMonthDays = generateNextMonthDays(
    year,
    month,
    remainingDays,
    today,
    selectedDate,
    hasAppointmentsOnDate,
  );

  return [...previousMonthDays, ...currentMonthDays, ...nextMonthDays];
}

export function formatMonthYear(date: Date, locale = 'pl-PL'): string {
  return date.toLocaleDateString(locale, {
    month: 'long',
    year: 'numeric',
  });
}

export function getPreviousMonth(currentMonth: Date): Date {
  return new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1);
}

export function getNextMonth(currentMonth: Date): Date {
  return new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1);
}

export function isDateInRange(
  date: Date,
  startDate: Date,
  endDate: Date,
): boolean {
  const dateOnly = new Date(
    date.getFullYear(),
    date.getMonth(),
    date.getDate(),
  );
  const start = new Date(
    startDate.getFullYear(),
    startDate.getMonth(),
    startDate.getDate(),
  );
  const end = new Date(
    endDate.getFullYear(),
    endDate.getMonth(),
    endDate.getDate(),
  );

  return dateOnly >= start && dateOnly <= end;
}

export function addDays(date: Date, days: number): Date {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

export function getDateOnly(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function createCalendarDay(
  date: Date,
  dayNumber: number,
  isCurrentMonth: boolean,
  today: Date,
  selectedDate: Date | null,
  hasAppointmentsOnDate: (date: Date) => boolean,
): CalendarModel {
  return {
    date,
    dayNumber,
    isCurrentMonth,
    isToday: isSameDay(date, today),
    isSelected: selectedDate ? isSameDay(date, selectedDate) : false,
    hasAppointments: hasAppointmentsOnDate(date),
  };
}

function generatePreviousMonthDays(
  year: number,
  month: number,
  firstDayOfWeek: number,
  today: Date,
  selectedDate: Date | null,
  hasAppointmentsOnDate: (date: Date) => boolean,
): CalendarModel[] {
  const days: CalendarModel[] = [];
  const prevMonthDate = new Date(year, month - 1, 1);
  const prevYear = prevMonthDate.getFullYear();
  const prevMonth = prevMonthDate.getMonth();
  const lastDayOfPrevMonth = new Date(prevYear, prevMonth + 1, 0);

  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const dayNumber = lastDayOfPrevMonth.getDate() - i;
    const date = new Date(prevYear, prevMonth, dayNumber);
    days.push(
      createCalendarDay(
        date,
        dayNumber,
        false,
        today,
        selectedDate,
        hasAppointmentsOnDate,
      ),
    );
  }

  return days;
}

function generateCurrentMonthDays(
  year: number,
  month: number,
  lastDayOfMonth: Date,
  today: Date,
  selectedDate: Date | null,
  hasAppointmentsOnDate: (date: Date) => boolean,
): CalendarModel[] {
  const days: CalendarModel[] = [];

  for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
    const date = new Date(year, month, day);
    days.push(
      createCalendarDay(
        date,
        day,
        true,
        today,
        selectedDate,
        hasAppointmentsOnDate,
      ),
    );
  }

  return days;
}

function generateNextMonthDays(
  year: number,
  month: number,
  remainingDays: number,
  today: Date,
  selectedDate: Date | null,
  hasAppointmentsOnDate: (date: Date) => boolean,
): CalendarModel[] {
  const days: CalendarModel[] = [];
  const nextMonthDate = new Date(year, month + 1, 1);
  const nextYear = nextMonthDate.getFullYear();
  const nextMonth = nextMonthDate.getMonth();

  for (let day = 1; day <= remainingDays; day++) {
    const date = new Date(nextYear, nextMonth, day);
    days.push(
      createCalendarDay(
        date,
        day,
        false,
        today,
        selectedDate,
        hasAppointmentsOnDate,
      ),
    );
  }

  return days;
}

function getFirstDayOfWeek(firstDayOfMonth: Date): number {
  return (firstDayOfMonth.getDay() + 6) % 7;
}
