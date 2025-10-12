import { DaySchedule } from '../../modules/shared/components/ui/search-result.component/search-result.model';
import { InstitutionShortInfo } from './institution.model';

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
