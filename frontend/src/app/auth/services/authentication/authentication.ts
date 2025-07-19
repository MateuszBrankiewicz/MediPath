import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private http = inject(HttpClient);
  public registerUser(userToRegister : RegisterUser){
    console.log(userToRegister)
    return this.http.post('api', userToRegister)
  }
}
