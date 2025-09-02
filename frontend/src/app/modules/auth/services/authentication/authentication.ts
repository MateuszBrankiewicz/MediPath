import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { of } from 'rxjs';
import { API_URL } from '../../../../utils/constants';
import { SelectOption } from '../../../shared/components/forms/input-for-auth/select-with-search/select-with-search';
import {
  ForgotPasswordRequest,
  RegisterUser,
} from '../../models/auth.constants';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {
  private http = inject(HttpClient);
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
}
