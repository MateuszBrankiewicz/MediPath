export interface VisitPageModel {
  id: string;
  doctorName: string;
  institution: string;
  date: Date;
  status: VisitStatus;
}

export enum VisitStatus {
  Scheduled = 'scheduled',
  Canceled = 'canceled',
  Completed = 'completed',
}

export interface Visit {
  id: string;
  doctorName: string;
  doctorPhoto?: string;
  specialisation: string;
  institution: string;
  institutionPhoto?: string;
  address: string;
  date: Date;
  status: string;
  notes?: string;
  prescriptionPin?: string;
  referralPin?: string;
  commentId?: string | null;
}

export interface RescheduleData {
  doctorName: string;
  doctorId: string;
  institution: string;
  selectedDate?: Date;
  selectedTime?: string;
  selectedSlotId?: string;
  patientRemarks?: string;
}

export interface VisitBasicInfo {
  id: string;
  doctorName: string;
  date: string;
}

export interface VisitResponse {
  patient: {
    userId: string;
    name: string;
    surname: string;
    govID: string;
    valid: boolean;
  };
  doctor: {
    userId: string;
    doctorName: string;
    doctorSurname: string;
    specialisations: string[];
    valid: boolean;
  };
  time: {
    scheduleId: string;
    startTime: string;
    endTime: string;
    valid: boolean;
  };
  institution: {
    institutionId: string;
    institutionName: string;
    valid: boolean;
  };
  patientRemarks: string | null;
  id: string;
  status: 'Upcoming' | 'Cancelled' | 'Completed';
  note: string;
  codes: VisitCode[];
  doctorPfp?: string;
  institutionPfp?: string;
  institutionAddress?: string;
  commentId: string | null;
}

export interface VisitCode {
  codeType: 'PRESCRIPTION' | 'REFERRAL';
  code: string;
  active: boolean;
}

export type VisitResponseArray = VisitResponse[];
export interface UpcomingVisitsResponse {
  visits: VisitResponse[];
}

export interface SingleVisitResponse {
  visit: VisitResponse;
}

export interface InstitutionObject {
  institutionId: string;
  institutionName: string;
}

export interface FinishVisitResponse {
  prescriptions: string[];
  referrals: string[];
  note: string;
}

export interface VisitApiResponseArray {
  visits: VisitResponse[];
}
