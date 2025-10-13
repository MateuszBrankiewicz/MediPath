import { DoctorSchedule } from '../core/models/doctor.model';
import { CalendarDay } from '../core/models/schedule.model';
import { MapToCalendarDaysOptions } from '../modules/admin/components/institution-schedule/institution-schedule';

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

  const calendarDays: CalendarDay[] = [];

  groupedByDate.forEach((dayData, dateKey) => {
    const date = new Date(dateKey + 'T00:00:00');

    let isFromThisInstitution: boolean | undefined = undefined;

    if (
      options.selectedInstitutionIds &&
      options.selectedInstitutionIds.length > 0
    ) {
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
      isToday: date.getTime() === today.getTime(),
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
