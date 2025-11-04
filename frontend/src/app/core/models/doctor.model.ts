import { DaySchedule } from '../../modules/shared/components/ui/search-result.component/search-result.model';
import { InstitutionShortInfo } from './institution.model';
import { VisitStatus } from './visit.model';

export interface DoctorPageModel {
  name: string;
  surname: string;
  photoUrl: string;
  pwz: string;
  rating: {
    stars: number;
    opinions: number;
  };
  institutions: InstitutionShortInfo[];
  specialisation: string[];
  schedule: DaySchedule[];
  comments: Comment[];
}

export interface Comment {
  id: string;
  userName: string;
  visitedInstitution: string;
  content: string;
  dateOfVisit: Date;
  numberOfStars: number;
}

export interface DoctorDetailsApiResponse {
  doctor: {
    employers: InstitutionShortInfo[];
    id: string;
    licence_number: string;
    name: string;
    surname: string;
    specialisations: string[];
  };
}

export interface ScheduleDoctorInfo {
  userId: string;
  doctorName: string;
  doctorSurname: string;
  specialisations: string[];
}

export interface ScheduleInstitutionInfo {
  institutionId: string;
  institutionName: string;
}

export interface DoctorSchedule {
  id: string;
  startHour: string;
  endHour: string;
  doctor: ScheduleDoctorInfo;
  institution: ScheduleInstitutionInfo;
  booked: boolean;
}

export interface DoctorProfile {
  doctorId: string;
  doctorName: string;
  doctorSurname: string;
  doctorPfp: string;
  doctorSchedules: DoctorSchedule[];
  rating: number;
  licenceNumber: string;
  numofratings: number;
}

export interface DoctorApiResponse {
  doctors: DoctorProfile[];
}

export interface DoctorWithSchedule {
  doctorId: string;
  doctorName: string;
  doctorSurname: string;
  schedules: DoctorSchedule[];
}

export interface PatientForDoctor {
  id: string;
  name: string;
  surname: string;
  lastVisit: {
    endTime: string;
    startTime: string;
    status: VisitStatus;
    id: string;
  };
}

export interface DoctorPatientsApiResponse {
  patients: PatientForDoctor[];
}

export interface VisitsForPatientProfile {
  id: string;
  note: string;
  institution: string;
  codes: { codeType: string; code: string; active: boolean }[];
  patientRemarks: string;
  startTime: number[];
  endTime: number[];
  status: 'Upcoming' | 'Completed' | 'Cancelled';
}

export interface DoctorPatientsVisitApiResponse {
  visits: VisitsForPatientProfile[];
}
