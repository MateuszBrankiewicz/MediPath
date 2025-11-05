import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  OnInit,
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
import { Popover, PopoverModule } from 'primeng/popover';
import { SelectModule } from 'primeng/select';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MenuItemCommandEvent } from 'primeng/api';
import { AuthenticationService } from '../../../../../core/services/authentication/authentication';
import {
  getRoleFromCode,
  UserRoles,
  UserRolesNumbers,
} from '../../../../../core/services/authentication/authentication.model';
import { NotificationMessage } from '../../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../../core/services/notifications/user-notifications.service';
import { ToastService } from '../../../../../core/services/toast/toast.service';

export interface TopBarConfig {
  showSearch?: boolean;
  showNotifications?: boolean;
  pageTitle?: string;
}
interface RoleOption {
  label: string;
  value: UserRolesNumbers;
}

const ALL_POSSIBLE_ROLES: RoleOption[] = [
  { label: 'Patient', value: UserRolesNumbers.PATIENT },
  { label: 'Doctor', value: UserRolesNumbers.DOCTOR },
  { label: 'Staff', value: UserRolesNumbers.STAFF },
  { label: 'Admin', value: UserRolesNumbers.ADMIN },
];

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
    SelectModule,
    PopoverModule,
    FormsModule,
  ],
})
export class TopBarComponent implements OnInit {
  private readonly userMenu = viewChild<Popover>('userMenu');
  private readonly notificationsPopover = viewChild<Popover>(
    'notificationsPopover',
  );

  private destroyRef = inject(DestroyRef);

  private readonly router = inject(Router);

  private readonly authService = inject(AuthenticationService);

  private readonly toastService = inject(ToastService);

  protected readonly translationService = inject(TranslationService);

  protected readonly selectedSearchType = signal('institution');

  protected readonly selectedCategory = signal('');
  protected readonly locationQuery = signal('');
  protected readonly specializationQuery = signal('');
  protected readonly notifications = signal<NotificationMessage[]>([]);
  protected readonly isSearchExpanded = signal(false);

  protected readonly unreadCount = signal(1);

  private readonly notificationService = inject(UserNotificationsService);

  protected readonly categoryOptions = computed(() => [
    { label: '-- Category --', value: '' },
    {
      label: this.translationService.translate('topBar.doctor'),
      value: 'doctor',
    },
    {
      label: this.translationService.translate('topBar.institution'),
      value: 'institution',
    },
  ]);

  protected readonly roleOptions = computed<RoleOption[]>(() => {
    const user = this.authService.userChanges();
    if (!user || !user.roleCode) {
      return [];
    }

    const userRolesValue = user.roleCode as number;

    return ALL_POSSIBLE_ROLES.filter(
      (roleOption) => (userRolesValue & roleOption.value) === roleOption.value,
    );
  });

  protected readonly selectedRole = computed<UserRoles>(() => {
    const lastPanel = this.user()?.userSettings.lastPanel;
    if (typeof lastPanel === 'number') {
      return getRoleFromCode(lastPanel);
    }
    return (lastPanel as UserRoles) ?? UserRoles.PATIENT;
  });

  public readonly config = input<TopBarConfig>({
    showSearch: false,
    showNotifications: false,
  });

  protected toggleSearchExpanded(): void {
    this.isSearchExpanded.update((expanded) => !expanded);
  }

  protected search() {
    const query = this.query().trim();
    const category = this.selectedCategory();
    const location = this.locationQuery().trim();
    const specialization = this.specializationQuery().trim();

    if (!category) {
      return;
    }

    const searchParams = {
      query,
      category,
      location,
      specialization,
    };

    this.toggleSearchExpanded();

    this.router.navigate(['/search'], {
      queryParams: searchParams,
    });
  }

  protected readonly query = signal('');

  protected readonly isMenuOpen = signal(false);

  protected readonly user = computed(() => this.authService.getUser());

  protected readonly unreadBadge = computed<string | undefined>(() => {
    const count = this.notifications().filter(
      (n) =>
        !n.read &&
        n.timestamp !== undefined &&
        new Date(n.timestamp) < new Date(),
    ).length;
    if (count <= 0) {
      return undefined;
    }
    return count > 99 ? '99+' : String(count);
  });
  protected readonly isMarkingAll = signal(false);

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

  ngOnInit(): void {
    this.notificationService.notifications$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((message) => {
      const title = message?.title || 'Nowa notyfikacja';
      const content = message?.content || '';
      this.toastService.showInfo(title, content);
      this.notifications.update((notifications) => [...notifications, message]);
    });
    this.initNotifications();
  }

  private navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }

  private navigateToSettings(): void {
    this.router.navigate(['/preferences']);
  }

  protected toggleUserMenu(event: Event): void {
    const menu = this.userMenu();
    if (menu) {
      menu.toggle(event);
      this.isMenuOpen.update((open) => !open);
    }
  }

  protected onMenuItemClick(item: MenuItem): void {
    const menu = this.userMenu();
    if (menu) {
      menu.hide();
    }
    this.isMenuOpen.set(false);

    if (item.command) {
      const event: MenuItemCommandEvent = {
        originalEvent: new MouseEvent('click'),
        item: item,
      };
      item.command(event);
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

  protected onNotificationsButtonClick(event: Event): void {
    const popover = this.notificationsPopover();
    if (!popover) {
      return;
    }

    popover.toggle(event);
  }

  protected onNotificationSelect(notification: NotificationMessage): void {
    this.notificationService
      .markAsRead(notification)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.notifications.update((notifications) =>
            notifications.map((n) =>
              n.timestamp === notification.timestamp &&
              n.title === notification.title
                ? { ...n, read: true }
                : n,
            ),
          );
        },
      });
  }

  protected onMarkAllAsRead(): void {
    this.notificationService
      .markAllAsRead()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.initNotifications();
      });
  }

  protected navigateToNotifications(): void {
    const routePrefix = this.authService.getRoutePrefix();
    const targetRoute =
      routePrefix === '/patient'
        ? [routePrefix, 'reminders']
        : ['/patient', 'reminders'];

    this.router.navigate(targetRoute);
    this.notificationsPopover()?.hide();
  }

  protected translate(key: string, params?: Record<string, string | number>) {
    return this.translationService.translate(key, params);
  }

  protected onRoleChange(newRole: UserRolesNumbers | null): void {
    if (!newRole) {
      return;
    }
    const mappedRole = getRoleFromCode(newRole);
    if (!mappedRole) {
      return;
    }
    this.authService
      .changeLastPanel(mappedRole)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (role) => {
          if (!role) {
            return;
          }
          this.navigateToModule(role);
        },
        error: (error) => {
          console.error('Changing default panel failed', error);
          this.toastService.showError('toast.changePanel.error');
        },
      });
  }

  private navigateToModule(targetRole: UserRoles): void {
    switch (targetRole) {
      case UserRoles.DOCTOR:
        this.router.navigate(['/doctor']);
        break;
      case UserRoles.ADMIN:
        this.router.navigate(['/admin']);
        break;
      case UserRoles.PATIENT:
      default:
        this.router.navigate(['/patient']);
        break;
    }
  }

  private initNotifications() {
    this.notificationService.getAllNotifications();
    this.notificationService.notificationsArray$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (notifications) => {
          notifications = notifications.filter(
            (n) =>
              n.timestamp !== undefined && new Date(n.timestamp) < new Date(),
          );
          this.notifications.set(notifications);
        },
      });
  }
}
