import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { InputTextModule } from 'primeng/inputtext';
import { Menu, MenuModule } from 'primeng/menu';
import { SelectModule } from 'primeng/select';
import { AuthenticationService } from '../../../../../core/services/authentication/authentication';
import { ToastService } from '../../../../../core/services/toast/toast.service';

export interface TopBarConfig {
  showSearch?: boolean;
  showNotifications?: boolean;
  pageTitle?: string;
}
interface RoleOption {
  label: string;
  value: string;
}
@Component({
  selector: 'app-top-bar-component',
  templateUrl: './top-bar-component.html',
  styleUrl: './top-bar-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    InputTextModule,
    InputGroupModule,
    InputGroupAddonModule,
    ButtonModule,
    MenuModule,
    SelectModule,
    FormsModule,
  ],
})
export class TopBarComponent {
  private readonly userMenu = viewChild<Menu>('userMenu');

  private destroyRef = inject(DestroyRef);

  private readonly router = inject(Router);

  private readonly authService = inject(AuthenticationService);

  private readonly toastService = inject(ToastService);

  readonly roleOptions = computed<RoleOption[]>(() => [
    { label: 'Doctor', value: 'doctor' },
    { label: 'Nurse', value: 'nurse' },
    { label: 'Admin', value: 'admin' },
    { label: 'Patient', value: 'patient' },
  ]);

  readonly config = input<TopBarConfig>({
    showSearch: false,
    showNotifications: false,
  });

  readonly isMenuOpen = signal(false);

  readonly user = computed(() => this.authService.getUser());

  readonly menuItems = computed<MenuItem[]>(() => [
    {
      label: 'Edit profile',
      icon: 'pi pi-user',
      command: () => this.navigateToProfile(),
    },
    {
      separator: true,
    },
    {
      label: 'Settings',
      icon: 'pi pi-cog',
      command: () => this.navigateToSettings(),
    },
    {
      separator: true,
    },
    {
      label: 'Logout',
      icon: 'pi pi-sign-out',
      styleClass: 'logout-item',
      command: () => this.logout(),
    },
  ]);

  private navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }

  private navigateToSettings(): void {
    this.router.navigate(['/settings']);
  }

  toggleUserMenu(event: Event): void {
    const menu = this.userMenu();
    if (menu) {
      menu.toggle(event);
      this.isMenuOpen.update((open) => !open);
    }
  }

  private logout(): void {
    this.authService
      .logout()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess('toast.logout.success');
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Logout failed', error);
          this.toastService.showError('toast.logout.error');
        },
      });
  }
}
