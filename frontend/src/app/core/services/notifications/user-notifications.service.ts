import { Injectable, OnDestroy } from '@angular/core';
import { Client, StompSubscription, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';
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
      debug: (msg) => console.debug('[STOMP]', msg),
    });

    this.client.onConnect = (frame) => {
      console.log('Connected: ', frame);
      this.client.subscribe('/user/notifications', (message: IMessage) => {
        const parsedMessage = JSON.parse(message.body) as NotificationMessage;
        console.log('private:', parsedMessage);
        this.notificationSubject.next(parsedMessage);
      });
    };

    this.client.onStompError = (frame) => {
      console.error('Broker error:', frame.headers['message'], frame.body);
    };

    // start połączenia
    this.client.activate();
  }

  // public send(notification: unknown): void {
  //   if (this.client.connected) {
  //     this.client.publish({
  //       destination: '/app/notifications',
  //       body: JSON.stringify(notification),
  //     });
  //   }
  // }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
    this.client.deactivate();
    this.notificationSubject.complete();
  }
}
