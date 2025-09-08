import { Component, computed, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';
import { ToastModule } from 'primeng/toast';
import { NavigationComponent } from './modules/shared/components/ui/navigation/navigation.component';
import { TopBarComponent } from './modules/shared/components/ui/top-bar-component/top-bar-component';
import { UserRoles } from './core/services/authentication/authentication.model';
import { AuthenticationService } from './core/services/authentication/authentication';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavigationComponent, TopBarComponent, ToastModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected readonly router = inject(Router);
  protected readonly authService = inject(AuthenticationService);

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

  ngOnInit() {
    this.authService.checkAuthStatus().subscribe({
      next: () => {
        console.log('Auth status checked on app start');
      },
      error: () => {
        console.log('Auth check failed - user not authenticated');
      },
    });
  }
}
