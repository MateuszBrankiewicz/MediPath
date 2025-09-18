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
}
