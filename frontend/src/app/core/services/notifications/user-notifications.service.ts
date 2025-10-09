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
        const parsedMessage = JSON.parse(message.body) as NotificationMessage;
        this.notificationSubject.next(parsedMessage);
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

  public getAllNotifications(): Observable<NotificationMessage[]> {
    return this.http
      .get<NotificationMessageResponse>(`${API_URL}/users/me/notifications`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.notifications));
  }

  public addNotification(
    notification: MedicationReminder,
  ): Observable<unknown> {
    if (notification.startDate !== null) {
      notification.startDate = new Date(notification.startDate)
        .toISOString()
        .split('T')[0];
    }
    if (notification.endDate !== null) {
      notification.endDate = new Date(notification.endDate)
        .toISOString()
        .split('T')[0];
    }
    return this.http.post(`${API_URL}/notifications/add`, notification, {
      withCredentials: true,
    });
  }
}
