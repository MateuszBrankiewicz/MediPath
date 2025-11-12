export enum UserRoles {
  ADMIN = 'admin',
  DOCTOR = 'doctor',
  PATIENT = 'patient',
  STAFF = 'staff',
  GUEST = 'guest',
}

export enum UserRolesNumbers {
  PATIENT = 1,
  DOCTOR = 2,
  STAFF = 4,
  ADMIN = 8,
}

export interface UserBasicInfo {
  id?: string;
  name: string;
  surname: string;
  roleCode: number | UserRoles;
  notifications: Notification[];
  email?: string;
  userSettings: UserSettings;
  pfpImage?: string;
  rating?: number;
}

export interface Notification {
  id: string;
  title: string;
  content: string;
  type: string;
  read: boolean;
  createdAt: Date;
  system: boolean;
}

export interface ApiNotification {
  title: string;
  content: string;
  timestamp: string;
  system: boolean;
  read: boolean;
}

export interface UserSettings {
  language: string;
  lastPanel: number | UserRoles;
  systemNotifications: boolean;
  emailNotifications: boolean;
  userNotifications?: boolean;
}

export interface ApiUserResponse {
  user: {
    id: string;
    name: string;
    surname: string;
    roleCode: number;
    notifications: ApiNotification[];
    email: string;
    userSettings: UserSettings;
    pfpImage?: string;
    rating?: number;
  };
}

export const RoleCodeMapping: Record<number, UserRoles> = {
  0: UserRoles.GUEST,
  1: UserRoles.PATIENT,
  2: UserRoles.DOCTOR,
  4: UserRoles.STAFF,
  8: UserRoles.ADMIN,
};

export function getRoleFromCode(roleCode: number): UserRoles {
  return RoleCodeMapping[roleCode] || UserRoles.GUEST;
}
