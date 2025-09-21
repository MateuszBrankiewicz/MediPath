export interface VisitPageModel {
  id: number;
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
  id: number;
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
  patientRemarks?: string;
}
