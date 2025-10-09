import { FormControl } from '@angular/forms';

export interface MedicalRecord {
  id: string;
  title: string;
  date: Date;
  doctor: {
    doctorName: string;
    doctorSurname: string;
  };
  notes: string | null;
}

export interface MedicalHistoryResponse {
  date: string;
  id: string;
  note: string;
  title: string;
  userId: string;
  doctor: {
    doctorName: string;
    doctorSurname: string;
    userId: string;
    specializations: string[];
    valid: boolean;
  };
}

export interface MedicalHistoryApiRequest {
  date: string;
  note: string;
  title: string;
  doctor: {
    doctorName: string;
    doctorSurname: string;
  };
}

export interface MedicalHistoryApiResponse {
  medicalhistories: MedicalHistoryResponse[];
}

export interface MedicalHistoryEntry {
  date: string;
  description: string;
}

export type MedicalHistoryDialogMode = 'view' | 'edit';

export interface MedicalHistoryDialogData {
  mode?: MedicalHistoryDialogMode;
  record?: MedicalHistoryResponse;
}

export interface MedicalHistoryDialogResult {
  record: MedicalHistoryResponse;
  mode: MedicalHistoryDialogMode;
}

export interface MedicalHistoryFormModel {
  id: FormControl<string>;
  title: FormControl<string>;
  date: FormControl<Date | null>;
  doctorName: FormControl<string>;
  doctorSurname: FormControl<string>;
  notes: FormControl<string | null>;
}
