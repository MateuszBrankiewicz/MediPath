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

export interface MedicalHistoryApiResponse {
  medicalhistories: MedicalHistoryResponse[];
}
