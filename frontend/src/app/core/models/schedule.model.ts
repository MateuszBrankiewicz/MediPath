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
  institutionId?: string;
}

export interface AvailableDay {
  date: Date | string;
  slots: TimeSlot[];
}

export interface CalendarDay {
  date: Date;
  dayNumber: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  hasAppointments: boolean;
  isSelected: boolean;
  isFromThisInstitution?: boolean;
  appointments: AppointmentIndicator[];
}

export interface AppointmentIndicator {
  id: string;
}

export interface InputSlot {
  startHour: string;
  endHour: string;
  doctor: {
    userId: string;
    doctorName: string;
    doctorSurname: string;
    specialisations: string[];
  };
  institution: {
    institutionId: string;
    institutionName: string;
  };
  id: string;
  booked: boolean;
}

export interface CreateScheduleRequest {
  doctorID: string;
  institutionID: string;
  startHour: string;
  endHour: string;
  interval: string;
}
