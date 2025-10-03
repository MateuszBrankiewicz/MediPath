import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import {
  catchError,
  finalize,
  map,
  Observable,
  of,
  tap,
  throwError,
} from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  DEFAULT_LANGUAGE,
  fromBackendLanguage,
  SupportedLanguage,
  toBackendLanguage,
} from '../../services/translation/language.model';
import {
  UpdateUserSettingsRequest,
  UserSettings,
  UserSettingsResponse,
} from './user-settings.model';

const DEFAULT_SETTINGS: UserSettings = {
  language: DEFAULT_LANGUAGE,
  systemNotifications: true,
  userNotifications: false,
};

@Injectable({
  providedIn: 'root',
})
export class UserSettingsService {
  private http = inject(HttpClient);

  private readonly settingsSignal = signal<UserSettings | null>(null);
  private readonly loadingSignal = signal(false);

  readonly settings = this.settingsSignal.asReadonly();
  readonly isLoading = this.loadingSignal.asReadonly();
  readonly language = computed<SupportedLanguage>(
    () => this.settingsSignal()?.language ?? DEFAULT_LANGUAGE,
  );

  ensureSettingsLoaded(): Observable<UserSettings> {
    const current = this.settingsSignal();
    if (current) {
      return of(current);
    }

    this.loadingSignal.set(true);
    return this.http
      .get<UserSettingsResponse>(`${API_URL}/users/me/settings`, {
        withCredentials: true,
      })
      .pipe(
        map((response) => this.mapResponse(response)),
        tap((settings) => this.settingsSignal.set(settings)),
        catchError(() => {
          this.settingsSignal.set(DEFAULT_SETTINGS);
          return of(DEFAULT_SETTINGS);
        }),
        finalize(() => this.loadingSignal.set(false)),
      );
  }

  updateSettings(settings: UserSettings): Observable<UserSettings> {
    this.loadingSignal.set(true);
    const payload = this.mapRequest(settings);

    return this.http
      .put<UserSettingsResponse>(`${API_URL}/users/me/settings`, payload, {
        withCredentials: true,
      })
      .pipe(
        map((response) => this.mapResponse(response)),
        tap((updated) => this.settingsSignal.set(updated)),
        catchError((error) => {
          return throwError(() => error);
        }),
        finalize(() => this.loadingSignal.set(false)),
      );
  }

  setLocalSettings(settings: UserSettings) {
    this.settingsSignal.set(settings);
  }

  private mapResponse(
    response: UserSettingsResponse | null | undefined,
  ): UserSettings {
    const fallback = { ...DEFAULT_SETTINGS };

    if (!response) {
      return fallback;
    }

    return {
      language: fromBackendLanguage(response.language),
      systemNotifications:
        response.systemNotifications ?? fallback.systemNotifications,
      userNotifications:
        response.userNotifications ?? fallback.userNotifications,
    };
  }

  private mapRequest(settings: UserSettings): UpdateUserSettingsRequest {
    return {
      language: toBackendLanguage(settings.language),
      systemNotifications: settings.systemNotifications,
      userNotifications: settings.userNotifications,
    };
  }
}
