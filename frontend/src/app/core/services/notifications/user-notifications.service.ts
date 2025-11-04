import { HttpClient } from '@angular/common/http';
import { inject, Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { map, Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { API_URL } from '../../../utils/constants';
import { MedicationReminder } from '../../models/reminder.model';
import {
  NotificationMessage,
  NotificationMessageResponse,
} from './user-notifications.model';

export type ListenerCallBack = (data: unknown) => void;

@Injectable({
  providedIn: 'root',
})
export class UserNotificationsService implements OnDestroy {
  private notifications = new Subject<NotificationMessage[]>();
  public notificationsArray$ = this.notifications.asObservable();
  private client: Client;
  private subscription: StompSubscription | undefined;
  private http = inject(HttpClient);
  private notificationSubject = new Subject<NotificationMessage>();

  public notifications$: Observable<NotificationMessage> =
    this.notificationSubject.asObservable();

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
    });

    this.client.onConnect = () => {
      this.client.subscribe('/user/notifications', (message: IMessage) => {
        try {
          const parsedMessage = JSON.parse(message.body) as NotificationMessage;
          this.notificationSubject.next(parsedMessage);
        } catch {
          throw new Error('Error parsing notification message');
        }
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Broker error:', frame.headers['message'], frame.body);
    };

    this.client.activate();
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.client.deactivate();
    this.notificationSubject.complete();
  }

  public getAllNotifications(): void {
    this.http
      .get<NotificationMessageResponse>(`${API_URL}/users/me/notifications`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.notifications))
      .subscribe((notifications) => {
        this.notifications.next(notifications);
      });
  }

  public addNotification(
    notification: MedicationReminder,
  ): Observable<unknown> {
    if (notification.startDate !== null) {
      const d = new Date(notification.startDate);

      notification.startDate = `${d.getFullYear()}-${(d.getMonth() + 1)
        .toString()
        .padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
    }

    if (notification.endDate !== null) {
      const d = new Date(notification.endDate);
      notification.endDate = `${d.getFullYear()}-${(d.getMonth() + 1)
        .toString()
        .padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
    }
    return this.http.post(`${API_URL}/notifications/add`, notification, {
      withCredentials: true,
    });
  }

  public markAllAsRead(): Observable<unknown> {
    return this.http.post(`${API_URL}/notifications/readall`, null, {
      withCredentials: true,
    });
  }

  public markAsRead(notification: NotificationMessage): Observable<unknown> {
    return this.http.post(
      `${API_URL}/notifications/read`,
      {
        timestamp: notification.timestamp,
        title: notification.title,
      },
      {
        withCredentials: true,
      },
    );
  }

  public deleteNotification(
    notification: NotificationMessage,
  ): Observable<unknown> {
    const dateString = notification.timestamp?.split('T')[0] || '';
    const time = notification.timestamp?.split('T')[1] || '';
    return this.http.delete(`${API_URL}/notifications`, {
      body: {
        reminderTime: time,
        title: notification.title,
        startDate: dateString,
        endDate: dateString,
      },
      withCredentials: true,
    });
  }

  public getNotifications(param: string): Observable<NotificationMessage[]> {
    return this.http.get<NotificationMessage[]>(
      `${API_URL}/users/me/notifications?filter=${param}`,
      {
        withCredentials: true,
      },
    );
  }
}
