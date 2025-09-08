import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { NavigationComponent } from './modules/shared/components/ui/navigation/navigation.component';
import { TopBarComponent } from './modules/shared/components/ui/top-bar-component/top-bar-component';
import { UserRoles } from './core/services/authentication/authentication.model';
import { AuthenticationService } from './core/services/authentication/authentication';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavigationComponent, TopBarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly router = inject(Router);
  protected readonly title = signal('frontend');
  protected readonly authService = inject(AuthenticationService);
  protected readonly userRole = signal<UserRoles>(
    this.authService.getUserRole(),
  );

  readonly navigationConfig = computed(() => ({
    showSearch: true,
    showNotifications: true,
  }));

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => (event as NavigationEnd).url),
      startWith(this.router.url),
    ),
  );
  public readonly showNavigation = computed(() => {
    const url = this.currentUrl();
    const guestRoutes = ['/auth', '/'];
    return !guestRoutes.some((route) => url?.startsWith(route));
  });
}
