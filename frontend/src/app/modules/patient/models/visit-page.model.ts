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
  const prescriptionCodes = visitResponse.codes.filter(
    (code) => code.codeType === 'PRESCRIPTION' && code.active,
  );
  const referralCodes = visitResponse.codes.filter(
    (code) => code.codeType === 'REFERRAL' && code.active,
  );

  return {
    id: visitResponse.id,
    doctorName: `${visitResponse.doctor.doctorName} ${visitResponse.doctor.doctorSurname}`,
    specialisation:
      visitResponse.doctor.specialisations.join(', ') || 'Brak specjalizacji',
    institution: visitResponse.institution.institutionName,
    address: '',
    date: new Date(visitResponse.time.startTime),
    status: visitResponse.status.toLowerCase(),
    notes: visitResponse.note || undefined,
    prescriptionPin:
      prescriptionCodes.map((code) => code.code).join(', ') || undefined,
    referralPin: referralCodes.map((code) => code.code).join(', ') || undefined,
  };
}
