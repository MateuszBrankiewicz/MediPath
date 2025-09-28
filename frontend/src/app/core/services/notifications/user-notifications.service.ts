import { HttpClient } from '@angular/common/http';
import {
  DestroyRef,
  Injectable,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { Observable, catchError, map, of, tap, throwError } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import { AuthenticationService } from '../authentication/authentication';
import { Notification as AuthNotification } from '../authentication/authentication.model';
import {
  UserNotification,
  UserNotificationPayload,
} from './user-notifications.model';

const RECONNECT_DELAY_MS = 5_000;

@Injectable({
  providedIn: 'root',
})
export class UserNotificationsService {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authenticationService = inject(AuthenticationService);

  private readonly notificationsSignal = signal<UserNotification[]>([]);
  private readonly connectingSignal = signal(false);
  private readonly connectedSignal = signal(false);
  private readonly errorSignal = signal<unknown>(null);

  private socket: WebSocket | null = null;
  private reconnectHandle: ReturnType<typeof setTimeout> | null = null;
  private isManuallyClosed = false;

  readonly notifications = this.notificationsSignal.asReadonly();
  readonly isConnecting = this.connectingSignal.asReadonly();
  readonly isConnected = this.connectedSignal.asReadonly();
  readonly lastError = this.errorSignal.asReadonly();

  readonly unreadCount = computed(() =>
    this.notificationsSignal().reduce(
      (total, notification) => (notification.read ? total : total + 1),
      0,
    ),
  );

  constructor() {
    this.destroyRef.onDestroy(() => this.cleanup());
    effect(
      () => {
        const user = this.authenticationService.userChanges();
        if (!user) {
          this.resetState();
          return;
        }

        this.initialize(user.notifications);
        this.connect();
      },
      { allowSignalWrites: true },
    );
  }

  initialize(initialNotifications?: AuthNotification[] | null): void {
    if (!initialNotifications || initialNotifications.length === 0) {
      if (this.notificationsSignal().length === 0) {
        this.notificationsSignal.set([]);
      }
      return;
    }

    const normalized = initialNotifications
      .map((notification) => this.normalizeNotification(notification))
      .filter(
        (notification): notification is UserNotification => !!notification,
      );

    this.setNotifications(normalized);
  }

  connect(): void {
    if (this.socket || this.connectingSignal()) {
      return;
    }

    this.isManuallyClosed = false;
    this.openSocket();
  }

  disconnect(): void {
    this.isManuallyClosed = true;
    this.closeSocket();
  }

  markAsRead(notificationId: string): Observable<void> {
    const previous = this.setReadFlag(notificationId, true);

    return this.http
      .patch<void>(
        `${API_URL}/users/me/notifications/${notificationId}/read`,
        {},
        { withCredentials: true },
      )
      .pipe(
        catchError((error) => {
          if (previous !== undefined) {
            this.setReadFlag(notificationId, previous);
          }
          return throwError(() => error);
        }),
      );
  }

  markAllAsRead(): Observable<void> {
    const unreadIds = this.notificationsSignal()
      .filter((notification) => !notification.read)
      .map((notification) => notification.id);

    if (unreadIds.length === 0) {
      return of(void 0);
    }

    const previousStates = this.captureCurrentReadState(unreadIds);
    this.setReadFlags(unreadIds, true);

    return this.http
      .patch<void>(
        `${API_URL}/users/me/notifications/read`,
        {},
        { withCredentials: true },
      )
      .pipe(
        catchError((error) => {
          this.restoreReadFlags(previousStates);
          return throwError(() => error);
        }),
      );
  }

  refresh() {
    return this.fetchLatest();
  }

  private fetchLatest() {
    return this.http
      .get<AuthNotification[]>(`${API_URL}/users/me/notifications`, {
        withCredentials: true,
      })
      .pipe(
        map((items) =>
          items
            .map((item) => this.normalizeNotification(item))
            .filter((notification): notification is UserNotification =>
              Boolean(notification),
            ),
        ),
        tap((notifications) => this.setNotifications(notifications)),
        catchError((error) => {
          this.errorSignal.set(error);
          return throwError(() => error);
        }),
      );
  }

  private openSocket(): void {
    this.connectingSignal.set(true);
    this.errorSignal.set(null);

    try {
      const url = this.resolveWebSocketUrl();
      this.socket = new WebSocket(url);
    } catch (error) {
      this.handleConnectionError(error);
      return;
    }

    this.socket.addEventListener('open', () => {
      this.connectingSignal.set(false);
      this.connectedSignal.set(true);
      this.refresh()
        .pipe(catchError(() => of([] as UserNotification[])))
        .subscribe();
    });

    this.socket.addEventListener('message', (event) => {
      this.handleIncomingMessage(event.data);
    });

    this.socket.addEventListener('error', (event) => {
      this.handleConnectionError(event);
    });

    this.socket.addEventListener('close', () => {
      this.connectedSignal.set(false);
      this.connectingSignal.set(false);
      this.socket = null;

      if (!this.isManuallyClosed) {
        this.scheduleReconnect();
      }
    });
  }

  private handleIncomingMessage(data: unknown): void {
    if (typeof data === 'string') {
      this.processPayload(data);
      return;
    }

    if (data instanceof Blob) {
      data
        .text()
        .then((text) => this.processPayload(text))
        .catch((error) => {
          this.errorSignal.set(error);
        });
      return;
    }
  }

  private processPayload(json: string): void {
    try {
      const payload = JSON.parse(json) as
        | UserNotificationPayload
        | UserNotificationPayload[];
      this.applyPayload(payload);
    } catch (error) {
      this.errorSignal.set(error);
    }
  }

  private applyPayload(
    payload: UserNotificationPayload | UserNotificationPayload[],
  ): void {
    if (Array.isArray(payload)) {
      const notifications = payload
        .map((item) => this.normalizeNotification(item))
        .filter((item): item is UserNotification => Boolean(item));

      this.setNotifications(notifications);
      return;
    }

    if (Array.isArray(payload.notifications)) {
      const notifications = payload.notifications
        .map((item) => this.normalizeNotification(item))
        .filter((item): item is UserNotification => Boolean(item));

      this.setNotifications(notifications);
      return;
    }

    const notification = this.normalizeNotification(payload);
    if (!notification) {
      return;
    }

    this.upsertNotifications([notification]);
  }

  private normalizeNotification(
    payload: UserNotificationPayload | AuthNotification,
  ): UserNotification | null {
    if (!payload) {
      return null;
    }

    const id = this.extractString(payload.id);
    const message = this.extractString(payload.message);

    if (!id || !message) {
      return null;
    }

    const createdAt = this.parseDate(
      (payload as UserNotificationPayload).createdAt ??
        (payload as AuthNotification).createdAt,
    );
    if (!createdAt) {
      return null;
    }

    return {
      id,
      message,
      type: this.extractString(payload.type) ?? 'info',
      read: Boolean(payload.read),
      createdAt,
      title:
        this.extractString((payload as UserNotificationPayload).title) ??
        undefined,
      metadata:
        this.extractRecord((payload as UserNotificationPayload).metadata) ??
        undefined,
    };
  }

  private upsertNotifications(notifications: UserNotification[]): void {
    if (notifications.length === 0) {
      return;
    }

    this.notificationsSignal.update((current) => {
      const map = new Map(current.map((item) => [item.id, item] as const));
      notifications.forEach((notification) => {
        const existing = map.get(notification.id);
        map.set(
          notification.id,
          existing ? { ...existing, ...notification } : notification,
        );
      });

      return this.sortNotifications(Array.from(map.values()));
    });
  }

  private setNotifications(notifications: UserNotification[]): void {
    this.notificationsSignal.set(this.sortNotifications(notifications));
  }

  private sortNotifications(
    notifications: UserNotification[],
  ): UserNotification[] {
    return [...notifications].sort(
      (a, b) => b.createdAt.getTime() - a.createdAt.getTime(),
    );
  }

  private captureCurrentReadState(ids: string[]): Map<string, boolean> {
    const lookup = new Map<string, boolean>();
    ids.forEach((id) => {
      const notification = this.notificationsSignal().find(
        (item) => item.id === id,
      );
      if (notification) {
        lookup.set(id, notification.read);
      }
    });
    return lookup;
  }

  private setReadFlags(ids: string[], read: boolean): void {
    if (ids.length === 0) {
      return;
    }

    this.notificationsSignal.update((current) =>
      current.map((notification) =>
        ids.includes(notification.id)
          ? { ...notification, read }
          : notification,
      ),
    );
  }

  private restoreReadFlags(previousStates: Map<string, boolean>): void {
    if (previousStates.size === 0) {
      return;
    }

    this.notificationsSignal.update((current) =>
      current.map((notification) => {
        const previous = previousStates.get(notification.id);
        return previous === undefined
          ? notification
          : { ...notification, read: previous };
      }),
    );
  }

  private setReadFlag(
    notificationId: string,
    read: boolean,
  ): boolean | undefined {
    let previous: boolean | undefined;

    this.notificationsSignal.update((current) =>
      current.map((notification) => {
        if (notification.id !== notificationId) {
          return notification;
        }

        previous = notification.read;
        return { ...notification, read };
      }),
    );

    return previous;
  }

  private scheduleReconnect(): void {
    if (this.reconnectHandle) {
      return;
    }

    this.reconnectHandle = setTimeout(() => {
      this.reconnectHandle = null;
      this.openSocket();
    }, RECONNECT_DELAY_MS);
  }

  private closeSocket(): void {
    if (this.reconnectHandle) {
      clearTimeout(this.reconnectHandle);
      this.reconnectHandle = null;
    }

    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }

    this.connectedSignal.set(false);
    this.connectingSignal.set(false);
  }

  private resolveWebSocketUrl(): string {
    try {
      const apiUrl = new URL(API_URL);
      const isSecure = apiUrl.protocol === 'https:';
      apiUrl.protocol = isSecure ? 'wss:' : 'ws:';
      apiUrl.pathname = this.joinPaths(
        apiUrl.pathname,
        '/ws/user/notifications',
      );
      return apiUrl.toString();
    } catch {
      const isSecure = window.location.protocol === 'https:';
      const base = `${isSecure ? 'wss' : 'ws'}://${window.location.host}`;
      return `${base}/api/ws/user/notifications`;
    }
  }

  private joinPaths(base: string, path: string): string {
    const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return `${normalizedBase}${normalizedPath}`;
  }

  private parseDate(
    value: string | number | Date | null | undefined,
  ): Date | null {
    if (!value) {
      return null;
    }

    if (value instanceof Date) {
      return new Date(value.getTime());
    }

    const timestamp =
      typeof value === 'number'
        ? value
        : Date.parse(typeof value === 'string' ? value : String(value));

    if (Number.isNaN(timestamp)) {
      return null;
    }

    return new Date(timestamp);
  }

  private extractString(value: unknown): string | null {
    if (typeof value === 'string') {
      return value;
    }

    if (typeof value === 'number' && Number.isFinite(value)) {
      return String(value);
    }

    return null;
  }

  private extractRecord(value: unknown): Record<string, unknown> | null {
    if (!value || typeof value !== 'object') {
      return null;
    }

    return value as Record<string, unknown>;
  }

  private handleConnectionError(error: unknown): void {
    this.errorSignal.set(error);
    this.connectingSignal.set(false);
    this.connectedSignal.set(false);
    this.closeSocket();
    if (!this.isManuallyClosed) {
      this.scheduleReconnect();
    }
  }

  private cleanup(): void {
    this.isManuallyClosed = true;
    this.closeSocket();
  }

  private resetState(): void {
    this.disconnect();
    this.notificationsSignal.set([]);
    this.errorSignal.set(null);
  }
}
