import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { NotificationMessage } from './user-notifications.model';

export type ListenerCallBack = (data: unknown) => void;

@Injectable({
  providedIn: 'root',
})
export class UserNotificationsService implements OnDestroy {
  private client: Client;
  private subscription: StompSubscription | undefined;

  private notificationSubject = new Subject<NotificationMessage>();

  public notifications$: Observable<NotificationMessage> =
    this.notificationSubject.asObservable();

  constructor() {
    console.log('Initializing UserNotificationsService');

    this.client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 5000,
    });

    this.client.onConnect = (frame) => {
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
}
