export enum UsedState {
  USED = 'USED',
  UNUSED = 'UNUSED',
}

export interface Refferal {
  id: number;
  doctorName: string;
  prescriptionPin: number;
  status: UsedState;
  date: Date;
  codeType?: string;
}

export interface ApiCode {
  codes: {
    codeType: CodeType;
    code: string;
    isActive: boolean;
  };
  date: string;
  doctor: string;
}

export interface ApiCodesResponse {
  codes: ApiCode[];
}
export type CodeType = 'PRESCRIPTION' | 'REFERRAL';
