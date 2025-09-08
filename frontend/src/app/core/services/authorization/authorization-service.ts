import { Injectable } from '@angular/core';
import { UserRoles } from './authorization.model';

@Injectable({
  providedIn: 'root',
})
export class AuthorizationService {
  public userRole() {
    return UserRoles.GUEST;
  }
  public userName() {
    return 'John Doe';
  }
}
