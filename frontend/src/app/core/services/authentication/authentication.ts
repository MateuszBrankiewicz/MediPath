import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { of } from 'rxjs';
import {
  ForgotPasswordRequest,
  RegisterUser,
} from '../../../modules/auth/models/auth.constants';
import { API_URL } from '../../../utils/constants';
import { SelectOption } from '../../../modules/shared/components/forms/input-for-auth/select-with-search/select-with-search';
import { UserRoles } from './authentication.model';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  private http = inject(HttpClient);

  private readonly userRole = signal<UserRoles>(UserRoles.GUEST);

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
    return this.http.post(
      API_URL + '/users/login',
      { email, password },
      { withCredentials: true },
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

  public getUserRole(): UserRoles {
    return this.userRole();
  }

  public setUserRole(userRole: UserRoles) {
    this.userRole.set(userRole);
  }

  public getUserRoleFromApi() {
    return this.http.get(API_URL + '/users/profile', { withCredentials: true });
  }
}
