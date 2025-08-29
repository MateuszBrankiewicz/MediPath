import { Injectable } from '@angular/core';
import { UserRoles } from './authorization.model';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationService {
  public userRole() {
    return UserRoles.PATIENT;
  }
  public userName() {
    return 'John Doe';
  }
}
