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
}

export interface RescheduleData {
  doctorName: string;
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
    startTime: number[]; // [year, month, day, hour, minute]
    endTime: number[]; // [year, month, day, hour, minute]
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
}

export interface VisitCode {
  codeType: 'PRESCRIPTION' | 'REFERRAL';
  code: string;
  active: boolean;
}

export type VisitResponseArray = VisitResponse[];

export function convertVisitResponseToVisit(
  visitResponse: VisitResponse,
): Visit {
  const [year, month, day, hour, minute] = visitResponse.time.startTime;
  const visitDate = new Date(year, month - 1, day, hour, minute); // month is 0-indexed in JS Date

  const prescriptionCodes = visitResponse.codes.filter(
    (code) => code.codeType === 'PRESCRIPTION' && code.active,
  );
  const referralCodes = visitResponse.codes.filter(
    (code) => code.codeType === 'REFERRAL' && code.active,
  );

  return {
    id: visitResponse.id, // Keep as string
    doctorName: `${visitResponse.doctor.doctorName} ${visitResponse.doctor.doctorSurname}`,
    specialisation:
      visitResponse.doctor.specialisations.join(', ') || 'Brak specjalizacji',
    institution: visitResponse.institution.institutionName,
    address: '', // Not provided in API response
    date: visitDate,
    status: visitResponse.status.toLowerCase(),
    notes: visitResponse.note || undefined,
    prescriptionPin:
      prescriptionCodes.map((code) => code.code).join(', ') || undefined,
    referralPin: referralCodes.map((code) => code.code).join(', ') || undefined,
  };
}
