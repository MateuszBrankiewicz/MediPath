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
