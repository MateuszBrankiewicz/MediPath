export enum UserRoles {
  ADMIN = 'admin',
  DOCTOR = 'doctor',
  PATIENT = 'patient',
  STAFF = 'staff',
  GUEST = 'guest',
}

export interface UserBasicInfo {
  id: string;
  name: string;
  surname: string;
  roleCode: number | UserRoles;
  notifications: Notification[];
  email: string;
  userSettings: UserSettings;
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
  };
}

export const RoleCodeMapping: Record<number, UserRoles> = {
  0: UserRoles.GUEST,
  1: UserRoles.PATIENT,
  2: UserRoles.DOCTOR,
  3: UserRoles.STAFF,
  4: UserRoles.ADMIN,
};

export function getRoleFromCode(roleCode: number): UserRoles {
  return RoleCodeMapping[roleCode] || UserRoles.GUEST;
}
