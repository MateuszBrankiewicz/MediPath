import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { filter, map, startWith } from 'rxjs';
import { AuthenticationService } from './core/services/authentication/authentication';
import { UserRoles } from './core/services/authentication/authentication.model';
import { SpinnerService } from './core/services/spinner/spinner.service';
import { NavigationComponent } from './modules/shared/components/ui/navigation/navigation.component';
import { TopBarComponent } from './modules/shared/components/ui/top-bar-component/top-bar-component';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    NavigationComponent,
    TopBarComponent,
    ToastModule,
    ProgressSpinnerModule,
  ],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly router = inject(Router);
  protected readonly authService = inject(AuthenticationService);
  protected readonly spinnerService = inject(SpinnerService);
  protected readonly userRole = computed(
    () => this.authService.getUser()?.roleCode || UserRoles.GUEST,
  );

  protected readonly isAuthenticated = computed(() =>
    this.authService.isAuthenticated(),
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
