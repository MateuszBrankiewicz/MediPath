import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem, MenuItemCommandEvent } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { RippleModule } from 'primeng/ripple';
import { MediPathMenuItem } from './navigation.model';
import { NavigationService } from './navigation.service';
import { AuthorizationService } from '../../../../../core/services/authorization/authorization-service';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MenuModule,
    ButtonModule,
    RippleModule,
  ],
})
export class NavigationComponent {
  private readonly router = inject(Router);
  private readonly navigationService = inject(NavigationService);
  private readonly authService = inject(AuthorizationService);

  readonly sidebarVisible = signal(false);

  readonly userRole = computed(() => this.authService.userRole());
  readonly userName = computed(() => this.authService.userName());

  readonly menuItems = computed(() => {
    const items = this.navigationService.getMenuItemsForRole(this.userRole());
    return this.convertToPrimeMenuItems(items);
  });

  toggleSidebar(): void {
    this.sidebarVisible.update((visible) => !visible);
  }

  navigateToProfile(): void {
    this.router.navigate(['/profile']);
    this.sidebarVisible.set(false);
  }

  private convertToPrimeMenuItems(items: MediPathMenuItem[]): MenuItem[] {
    return items.flatMap((item) =>
      item.menuItems.map((menuItem) => ({
        label: menuItem.label,
        icon: menuItem.icon,
        routerLink: menuItem.routerLink,
        badge: menuItem.badge?.toString(),
        badgeStyleClass: menuItem.badgeClass,
        visible: menuItem.visible !== false,
        disabled: menuItem.disabled,
        command: (event: MenuItemCommandEvent) => {
          if (menuItem.routerLink) {
            this.router.navigate([menuItem.routerLink]);
            this.sidebarVisible.set(false);
          }
          if (menuItem.command) {
            menuItem.command(event);
          }
        },
      })),
    );
  }
}
