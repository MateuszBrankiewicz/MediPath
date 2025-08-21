import { CommonModule } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MenuItem, MenuItemCommandEvent } from 'primeng/api';
import { MenuModule } from 'primeng/menu';
import { UserRoles } from '../../app';
import { MediPathMenuItem, MediPathMenuLinks } from './navigation.model';
import { NavigationService } from './navigation.service';

@Component({
  selector: 'app-navigation',
  imports: [CommonModule, RouterModule, MenuModule],
  templateUrl: './navigation.component.html',
  styleUrl: './navigation.component.scss',
})
export class NavigationComponent {
  private readonly router = inject(Router);

  private readonly navigationService = inject(NavigationService);

  protected readonly sidebarVisible = signal(false);

  protected readonly userName = signal('Jan Kowalski');

  public readonly userRole = input<UserRoles>();

  protected readonly menuItems: MenuItem[] = this.convertToPrimeMenuItems(
    this.navigationService.getMenuItemsForRole(
      this.userRole() ?? UserRoles.PATIENT,
    ),
  );

  private convertToPrimeMenuItems(items: MediPathMenuItem[]): MenuItem[] {
    return items.flatMap((menuItem) =>
      menuItem.menuItems.map((item) => ({
        label: item.label,
        icon: item.icon,
        routerLink: item.routerLink,
        badge: item.badge?.toString(),
        badgeStyleClass: item.badgeClass,
        visible: item.visible !== false,
        disabled: item.disabled,
        command: (event: MenuItemCommandEvent) => {
          if (item.routerLink) {
            this.router.navigate([item.routerLink]);
            this.sidebarVisible.set(false);
          }
          if (item.command) {
            item.command(event);
          }
        },
      })),
    );
  }

  protected onMenuItemClick(item: MediPathMenuLinks): void {
    if (item.routerLink) {
      this.router.navigate([item.routerLink]);
      this.sidebarVisible.set(false);
    }
  }

  protected toggleSidebar(): void {
    this.sidebarVisible.update((visible) => !visible);
  }

  protected navigateToProfile(): void {
    this.router.navigate(['/profile']);
  }

  protected logout(): void {
    this.router.navigate(['/auth/login']);
  }

  protected trackByIndex(index: number): number {
    return index;
  }
}
