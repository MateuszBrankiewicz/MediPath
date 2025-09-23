import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, of, switchMap, tap } from 'rxjs';
import {
  ForgotPasswordRequest,
  RegisterUser,
} from '../../../modules/auth/models/auth.constants';
import { API_URL } from '../../../utils/constants';
import { SelectOption } from '../../../modules/shared/components/forms/input-for-auth/select-with-search/select-with-search';
import {
  ApiUserResponse,
  getRoleFromCode,
  UserBasicInfo,
  UserRoles,
} from './authentication.model';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  private http = inject(HttpClient);

  private readonly user = signal<UserBasicInfo | null>(null);

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
          const userInfo: UserBasicInfo = {
            id: response.user.id,
            name: response.user.name,
            surname: response.user.surname,
            roleCode: getRoleFromCode(response.user.roleCode),
            notifications: response.user.notifications,
            email: response.user.email,
            userSettings: response.user.userSettings,
          };
          userInfo.userSettings.lastPanel = lastPanel;
          this.user.set(userInfo);
        }),
        catchError((error) => {
          this.user.set(null);
          throw error;
        }),
      );
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

  public logout() {
    return this.http
      .get(API_URL + '/users/logout', {
        withCredentials: true,
      })
      .pipe(
        tap(() => {
          console.log('User logged out successfully');
          this.user.set(null);
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
        const userInfo: UserBasicInfo = {
          id: response.user.id,
          name: response.user.name,
          surname: response.user.surname,
          roleCode: getRoleFromCode(response.user.roleCode),
          notifications: response.user.notifications,
          email: response.user.email,
          userSettings: response.user.userSettings,
        };
        userInfo.userSettings.lastPanel = lastPanel;
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

  public setNewRole(role: string) {
    if (!this.user()) {
      return;
    }
    switch (role) {
      case 'doctor':
        this.user()!.userSettings.lastPanel = UserRoles.DOCTOR;
        break;

      case 'admin':
        this.user()!.userSettings.lastPanel = UserRoles.ADMIN;
        break;
      case 'patient':
        this.user()!.userSettings.lastPanel = UserRoles.PATIENT;
        break;
      default:
        this.user()!.userSettings.lastPanel = UserRoles.PATIENT;
        break;
    }
    const currentUser = this.user();
    if (currentUser) {
      this.user.set({ ...currentUser });
    }
  }
}
