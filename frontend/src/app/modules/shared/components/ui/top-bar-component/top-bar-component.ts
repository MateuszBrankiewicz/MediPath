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

  protected readonly selectedSearchType = signal('institution');

  protected readonly roleOptions = computed<RoleOption[]>(() => [
    { label: 'Doctor', value: 'doctor' },
    { label: 'Nurse', value: 'nurse' },
    { label: 'Admin', value: 'admin' },
    { label: 'Patient', value: 'patient' },
  ]);

  public readonly config = input<TopBarConfig>({
    showSearch: false,
    showNotifications: false,
  });

  protected readonly query = signal('');

  protected readonly isMenuOpen = signal(false);

  protected readonly user = computed(() => this.authService.getUser());

  protected readonly menuItems = computed<MenuItem[]>(() => [
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

  searchMenuItems: MenuItem[] = [
    {
      label: 'Szukaj po instytucji',
      icon: 'pi pi-building',
      command: () => this.onSearchType('institution'),
    },
    {
      label: 'Szukaj po lekarzu',
      icon: 'pi pi-user',
      command: () => this.onSearchType('doctor'),
      style: { 'font-size': '1.1rem' },
    },
  ];

  private navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }

  private navigateToSettings(): void {
    this.router.navigate(['/settings']);
  }

  protected toggleUserMenu(event: Event): void {
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

  protected onSearchType(type: string): void {
    this.selectedSearchType.set(type);
    console.log(type);
  }

  protected getSearchPlaceholder(): string {
    switch (this.selectedSearchType()) {
      case 'institution':
        return 'Szukaj instytucji...';
      case 'doctor':
        return 'Szukaj lekarza...';
      default:
        return 'Szukaj...';
    }
  }

  protected updateMenuItems() {
    this.searchMenuItems = this.searchMenuItems.map((item) => ({
      ...item,
      styleClass:
        item.command && item.label?.includes(this.getTypeLabel())
          ? 'active-menu-item'
          : '',
    }));
  }

  protected search() {
    const query = this.query().trim();

    if (!query) {
      return;
    }

    const route = ['/search', this.selectedSearchType(), query];

    this.router
      .navigate(route)
      .then((success) => {
        console.log('Navigation success:', success);
        if (!success) {
          console.error('Navigation failed - route might not exist');
        }
      })
      .catch((error) => {
        console.error('Navigation error:', error);
      });
  }

  private getTypeLabel(): string {
    switch (this.selectedSearchType()) {
      case 'institution':
        return 'instytucji';
      case 'doctor':
        return 'lekarzu';
      default:
        return 'Wszystkie';
    }
  }
}
