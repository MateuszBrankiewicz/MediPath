import { Notification as AuthNotification } from '../authentication/authentication.model';

export type NotificationCategory = AuthNotification['type'];

export interface UserNotification extends Omit<AuthNotification, 'createdAt'> {
  createdAt: Date;
  title?: string;
  metadata?: Record<string, unknown> | null;
}

export interface UserNotificationPayload
  extends Partial<Omit<UserNotification, 'createdAt'>> {
  createdAt?: string | number | Date;
  eventType?: string;
  notifications?: UserNotificationPayload[];
}
