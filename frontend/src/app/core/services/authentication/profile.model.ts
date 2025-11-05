import { UserRoles } from './authentication.model';

export type LocalDateTuple = [number, number, number];

export interface UserProfileAddress {
  province: string;
  city: string;
  street: string;
  number: string;
  postalCode: string;
  valid?: boolean;
}

export interface UserProfileEmployer {
  institutionId: string;
  institutionName: string;
  valid: boolean;
}

export interface UserProfileDoctorSummary {
  userId: string;
  doctorName: string;
  doctorSurname: string;
  specialisations?: string[];
  valid?: boolean;
}

export interface UserProfileMedicalHistoryEntry {
  userId: string | null;
  title: string;
  note?: string;
  date: string;
  doctor: UserProfileDoctorSummary | null;
  id: string;
}

export interface UserProfileNotification {
  title: string;
  content: string;
  timestamp: [number, number, number, number, number];
  system: boolean;
  read: boolean;
}

export interface UserProfileSettings {
  language: string;
  systemNotifications: boolean;
  userNotifications: boolean;
  lastPanel: number | UserRoles;
}

export interface UserProfileEntity {
  name: string;
  surname: string;
  email: string;
  birthDate?: LocalDateTuple | null;
  phoneNumber?: string | null;
  govId?: string | null;
  licenceNumber?: string | null;
  rating?: number | null;
  numOfRatings?: number | null;
  roleCode: number | UserRoles;
  specialisations?: string[];
  pfpImage?: string | null;
  address?: UserProfileAddress | null;
  employers?: UserProfileEmployer[];
  latestMedicalHistory?: UserProfileMedicalHistoryEntry[];
  notifications?: UserProfileNotification[];
  userSettings: UserProfileSettings;
}

export interface GetUserProfileResponse {
  user: UserProfileEntity;
}

export interface UserProfileFormValue {
  name: string;
  surname: string;
  birthDate: string;
  phoneNumber: string;
  governmentId: string;
  province: string;
  postalCode: string;
  city: string;
  number: string;
  street: string;
  rating?: number | null;
}

export interface UpdateUserProfileRequest {
  name: string;
  surname: string;
  phoneNumber?: string | null;
  birthDate?: LocalDateTuple | null;
  province: string;
  city: string;
  street: string;
  number: string;
  postalCode: string;
}
