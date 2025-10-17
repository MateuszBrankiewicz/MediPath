import { DoctorSchedule } from '../core/models/doctor.model';
import {
  AppointmentIndicator,
  CalendarDay,
  MapToCalendarDaysOptions,
} from '../core/models/schedule.model';

export function mapSchedulesToCalendarDays(
  schedules: DoctorSchedule[],
  options: MapToCalendarDaysOptions,
  isToDoctorSchedule?: boolean,
): CalendarDay[] {
  const groupedByDate = new Map<
    string,
    {
      appointments: AppointmentIndicator[];
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

    let type: 'available-same' | 'available-other' | 'unavailable';

    if (isToDoctorSchedule) {
      if (schedule.booked) {
        console.log(schedule);
        type = 'available-same';
      } else {
        type = 'unavailable';
      }
    } else {
      if (schedule.booked) {
        type = 'unavailable';
      } else {
        const isFromSelectedInstitution =
          options.selectedInstitutionIds &&
          options.selectedInstitutionIds.includes(
            schedule.institution.institutionId,
          );

        type = isFromSelectedInstitution ? 'available-same' : 'available-other';
      }
    }
    dayData.appointments.push({ id: schedule.id, type });
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
