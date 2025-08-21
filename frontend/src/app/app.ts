import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { NavigationComponent } from './shared/navigation/navigation.component';

export enum UserRoles {
  ADMIN = 'admin',
  DOCTOR = 'doctor',
  PATIENT = 'patient',
  STAFF = 'staff',
}

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavigationComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly router = inject(Router);
  protected readonly title = signal('frontend');
  protected readonly userRole = signal<UserRoles>(UserRoles.PATIENT);

  constructor() {
    console.log('App initialized with role:', this.userRole());
    this.router.events.subscribe(() => {
      const currentRoute = this.router.url;
      switch (currentRoute) {
        case '/doctor':
          this.userRole.set(UserRoles.DOCTOR);
          break;
        case '/admin':
          this.userRole.set(UserRoles.ADMIN);
          break;
        case '/staf':
          this.userRole.set(UserRoles.STAFF);
          break;
        default:
          this.userRole.set(UserRoles.PATIENT);
      }
    });
    console.log('App initialized with role:', this.userRole());
  }
}
