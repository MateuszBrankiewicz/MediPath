import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import {
  catchError,
  map,
  Observable,
  of,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import {
  ForgotPasswordRequest,
  RegisterUser,
} from '../../../modules/auth/models/auth.constants';
import { SelectOption } from '../../../modules/shared/components/forms/input-for-auth/select-with-search/select-with-search';
import { API_URL } from '../../../utils/constants';
import {
  ApiNotification,
  ApiUserResponse,
  getRoleFromCode,
  Notification,
  UserBasicInfo,
  UserRoles,
  UserSettings,
} from './authentication.model';
import {
  GetUserProfileResponse,
  LocalDateTuple,
  UpdateUserProfileRequest,
  UserProfileEntity,
  UserProfileFormValue,
} from './profile.model';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  private http = inject(HttpClient);

  private readonly user = signal<UserBasicInfo | null>(null);
  public readonly userChanges = this.user.asReadonly();

  public readonly userRole = computed(() => {
    const roleCode = this.user()?.roleCode;
    return getRoleFromCode(typeof roleCode === 'number' ? roleCode : 0);
  });

  public registerUser(userToRegister: RegisterUser) {
    return this.http.post(API_URL + '/users/register', userToRegister);
  }

  public getCities(searchTerm: string) {
    if (!searchTerm || searchTerm.trim().length < 2) {
      return of([]);
    }
    return this.http.get<SelectOption[]>(API_URL + `/cities/${searchTerm}`);
  }

  public getProvinces() {
    return this.http.get<string[]>(API_URL + '/provinces');
  }

  public login(email: string, password: string) {
    return this.http
      .post(
        API_URL + '/users/login',
        { email, password },
        { withCredentials: true },
      )
      .pipe(
        switchMap(() => this.getUserRoleFromApi()),
        tap((response: ApiUserResponse) => {
          const lastPanel = getRoleFromCode(
            response.user.userSettings.lastPanel as number,
          );
          const normalizedSettings = this.normalizeUserSettings(
            response.user.userSettings,
          );
          const notifications = this.normalizeNotifications(
            response.user.notifications ?? [],
          );
          const userInfo: UserBasicInfo = {
            id: response.user.id,
            name: response.user.name,
            surname: response.user.surname,
            roleCode: response.user.roleCode,
            notifications,
            email: response.user.email,
            pfpImage: response.user.pfpImage,
            userSettings: {
              ...normalizedSettings,
              lastPanel,
            },
          };
          this.user.set(userInfo);
        }),
        catchError((error) => {
          this.user.set(null);
          throw error;
        }),
      );
  }

  public getLastPanel(): UserRoles | null {
    const lastPanel = this.user()?.userSettings.lastPanel;
    return typeof lastPanel === 'number'
      ? getRoleFromCode(lastPanel)
      : (lastPanel ?? null);
  }

  public resetPassword(email: string) {
    return this.http.get<{ message: string }>(
      API_URL + '/users/resetpassword?address=' + email,
    );
  }

  public sentPasswordWithToken(request: ForgotPasswordRequest) {
    return this.http.post(API_URL + '/users/resetpassword', request);
  }

  public getUserRoleFromApi() {
    return this.http.get<ApiUserResponse>(API_URL + '/users/profile', {
      withCredentials: true,
    });
  }

  public getUserProfile(): Observable<UserProfileFormValue> {
    return this.http
      .get<GetUserProfileResponse>(API_URL + '/users/profile', {
        withCredentials: true,
      })
      .pipe(map((response) => this.mapUserProfile(response.user)));
  }

  public updateUserProfile(formValue: UserProfileFormValue): Observable<void> {
    const payload = this.buildUpdateProfilePayload(formValue);
    return this.http.put<void>(API_URL + '/users/me/update', payload, {
      withCredentials: true,
    });
  }

  public logout() {
    return this.http
      .get(API_URL + '/users/logout', {
        withCredentials: true,
      })
      .pipe(
        tap(() => {
          this.user.set(null);
          sessionStorage.removeItem('userId');
        }),
      );
  }

  public readonly routePrefix = computed(() => {
    const userRole = this.user()?.userSettings.lastPanel;

    switch (userRole) {
      case UserRoles.PATIENT:
        return '/patient';
      case UserRoles.DOCTOR:
        return '/doctor';
      case UserRoles.ADMIN:
        return '/admin';
      default:
        return '/';
    }
  });

  public checkAuthStatus() {
    return this.getUserRoleFromApi().pipe(
      tap((response: ApiUserResponse) => {
        const lastPanel = getRoleFromCode(
          response.user.userSettings.lastPanel as number,
        );
        const normalizedSettings = this.normalizeUserSettings(
          response.user.userSettings,
        );
        const notifications = this.normalizeNotifications(
          response.user.notifications ?? [],
        );
        const userInfo: UserBasicInfo = {
          id: response.user.id,
          name: response.user.name,
          surname: response.user.surname,
          roleCode: response.user.roleCode,
          notifications,
          email: response.user.email,
          pfpImage: response.user.pfpImage,
          userSettings: {
            ...normalizedSettings,
            lastPanel,
          },
        };
        this.user.set(userInfo);
      }),
      catchError(() => {
        this.user.set(null);
        return of(null);
      }),
    );
  }

  public getUser() {
    return this.user();
  }

  public isAuthenticated() {
    return this.user() !== null;
  }

  public getRoutePrefix(): string {
    return this.routePrefix();
  }

  public getDashboardRoute(): string {
    const prefix = this.routePrefix();
    return prefix === '/' ? '/auth/login' : prefix;
  }

  public changeLastPanel(newRole: UserRoles): Observable<UserRoles | null> {
    const roleEndpoints: Partial<Record<UserRoles, number>> = {
      [UserRoles.PATIENT]: 1,
      [UserRoles.DOCTOR]: 2,
      [UserRoles.STAFF]: 4,
      [UserRoles.ADMIN]: 8,
    };

    const currentUser = this.user();
    if (!currentUser) {
      return of(null);
    }

    const updateUser = (role: UserRoles) => {
      const latestUser = this.user();
      if (!latestUser) {
        return;
      }
      this.user.set({
        ...latestUser,
        userSettings: {
          ...latestUser.userSettings,
          lastPanel: role,
        },
      });
    };

    const endpoint = roleEndpoints[newRole];
    if (endpoint === undefined) {
      updateUser(newRole);
      return of(newRole);
    }

    return this.http
      .put(
        `${API_URL}/users/me/defaultpanel/${endpoint}`,
        {},
        { withCredentials: true },
      )
      .pipe(
        tap(() => updateUser(newRole)),
        map(() => newRole),
        catchError((error) => throwError(() => error)),
      );
  }

  private mapUserProfile(user: UserProfileEntity): UserProfileFormValue {
    const address = user.address ?? {
      province: '',
      city: '',
      street: '',
      number: '',
      postalCode: '',
    };

    return {
      name: user.name ?? '',
      surname: user.surname ?? '',
      birthDate: this.formatDateTuple(user.birthDate),
      phoneNumber: user.phoneNumber ?? '',
      governmentId: user.govId ?? '',
      province: address.province ?? '',
      postalCode: address.postalCode ?? '',
      city: address.city ?? '',
      number: address.number ?? '',
      street: address.street ?? '',
    };
  }

  private buildUpdateProfilePayload(
    formValue: UserProfileFormValue,
  ): UpdateUserProfileRequest {
    return {
      name: formValue.name.trim(),
      surname: formValue.surname.trim(),
      phoneNumber: this.buildOptionalString(formValue.phoneNumber),
      birthDate: this.parseBirthDate(formValue.birthDate),

      province: formValue.province.trim(),
      city: formValue.city.trim(),
      street: formValue.street.trim(),
      number: formValue.number.trim(),
      postalCode: formValue.postalCode.trim(),
    };
  }

  private buildOptionalString(value: string): string | null {
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private normalizeUserSettings(settings: UserSettings): UserSettings {
    return {
      ...settings,
      userNotifications:
        typeof settings.userNotifications === 'boolean'
          ? settings.userNotifications
          : false,
    };
  }

  private normalizeNotifications(
    apiNotifications: ApiNotification[],
  ): Notification[] {
    if (!Array.isArray(apiNotifications)) {
      return [];
    }

    return apiNotifications
      .map((notification) => this.mapNotification(notification))
      .filter(
        (notification): notification is Notification => notification !== null,
      );
  }

  private mapNotification(
    notification: ApiNotification | null | undefined,
  ): Notification | null {
    if (!notification) {
      return null;
    }

    const createdAt = this.parseIsoDate(notification.timestamp);
    if (!createdAt) {
      return null;
    }

    const title = (notification.title ?? '').trim();
    const content = (notification.content ?? '').trim();
    const normalizedTitle = title || content || 'Notification';
    const normalizedContent = content || normalizedTitle;

    return {
      id: this.buildNotificationId(
        normalizedTitle,
        normalizedContent,
        createdAt,
      ),
      title: normalizedTitle,
      content: normalizedContent,
      type: notification.system ? 'system' : 'info',
      read: Boolean(notification.read),
      createdAt,
      system: Boolean(notification.system),
    };
  }

  private parseIsoDate(value?: string | null): Date | null {
    if (!value) {
      return null;
    }

    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  private buildNotificationId(
    title: string,
    content: string,
    createdAt: Date,
  ): string {
    const base = `${createdAt.getTime()}|${title}|${content}`;
    return `notif-${createdAt.getTime()}-${this.hashString(base)}`;
  }

  private hashString(value: string): string {
    let hash = 0;
    for (let index = 0; index < value.length; index += 1) {
      hash = (hash << 5) - hash + value.charCodeAt(index);
      hash |= 0;
    }
    return Math.abs(hash).toString(36);
  }

  private parseBirthDate(value: string): LocalDateTuple | null {
    const normalized = value.trim();
    if (!normalized) {
      return null;
    }

    const parts = normalized.split('-');
    if (parts.length !== 3) {
      return null;
    }

    const [year, month, day] = parts.map((part) => Number(part));
    if ([year, month, day].some((part) => Number.isNaN(part))) {
      return null;
    }

    return [year, month, day];
  }

  private formatDateTuple(date?: LocalDateTuple | null): string {
    if (!date) {
      return '';
    }

    const [year, month, day] = date;
    if (!year || !month || !day) {
      return '';
    }

    return `${year}-${this.padNumber(month)}-${this.padNumber(day)}`;
  }

  private padNumber(value: number): string {
    return value.toString().padStart(2, '0');
  }
}
