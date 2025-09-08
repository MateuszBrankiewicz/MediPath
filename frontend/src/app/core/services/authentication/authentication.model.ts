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
}

export interface Notification {
  id: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: Date;
}

export interface ApiUserResponse {
  user: {
    id: string;
    name: string;
    surname: string;
    roleCode: number;
    notifications: Notification[];
    email: string;
  };
}

export const RoleCodeMapping: Record<number, UserRoles> = {
  0: UserRoles.GUEST,
  1: UserRoles.PATIENT,
};

export function getRoleFromCode(roleCode: number): UserRoles {
  console.log('Mapping role code:', roleCode);
  console.log('Mapped role:', RoleCodeMapping[roleCode]);
  return RoleCodeMapping[roleCode] || UserRoles.GUEST;
}
