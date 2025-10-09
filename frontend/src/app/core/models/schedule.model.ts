export interface ScheduleVisitRequest {
  scheduleID: string;
  patientRemarks: string;
}

export interface ScheduleResponse {
  schedules: {
    id: string;
    startHour: string;
    endHour: string;
    booked: boolean;
    doctor: {
      userId: string;
      doctorName: string;
      doctorSurname: string;
      specialisations: string[];
      valid: boolean;
    };
    institution: {
      institutionId: string;
      institutionName: string;
      valid: boolean;
    };
  }[];
}

export interface ScheduleItem {
  id: string;
  startTime: string;
  isBooked: boolean;
}

export interface ScheduleByInstitutionResponse {
  schedules: {
    id: string;
    startHour: string;
    isBooked: boolean;
  }[];
}

export interface TimeSlot {
  id: string;
  time: string;
  available: boolean;
  booked: boolean;
}

export interface AvailableDay {
  date: Date | string;
  slots: TimeSlot[];
}

export interface CalendarDay {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  hasAppointments: boolean;
  dayNumber: number;
}
