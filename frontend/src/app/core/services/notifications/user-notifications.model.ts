import { Notification as AuthNotification } from '../authentication/authentication.model';

export type NotificationCategory = AuthNotification['type'];

export interface UserNotification extends AuthNotification {
  metadata?: Record<string, unknown> | null;
}

export interface UserNotificationPayload
  extends Partial<Omit<UserNotification, 'createdAt'>> {
  createdAt?: string | number | Date;
  timestamp?: string | number | Date;
  message?: string;
  notifications?: UserNotificationPayload[];
}
export interface NotificationMessage {
  title: string;
  content: string;
  type?: string;
  timestamp?: string;
  read?: boolean;
}

export interface NotificationMessageResponse {
  notifications: NotificationMessage[];
}
