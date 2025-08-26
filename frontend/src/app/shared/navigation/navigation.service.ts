import { Injectable, signal } from '@angular/core';
import { UserRoles } from '../../services/authorization/authorization.model';
import { MediPathMenuItem } from './navigation.model';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  private readonly _menuItems = signal<MediPathMenuItem[]>([]);

  public readonly menuItems = this._menuItems.asReadonly();

  public updateMenuItems(items: MediPathMenuItem[]): void {
    this._menuItems.set(items);
  }

  public addBadge(
    routerLink: string,
    badge: string | number,
    badgeClass?: string,
  ): void {
    const items = this._menuItems();
    const updatedItems = this.updateItemBadge(
      items,
      routerLink,
      badge,
      badgeClass,
    );
    this._menuItems.set(updatedItems);
  }

  public removeBadge(routerLink: string): void {
    const items = this._menuItems();
    const updatedItems = this.updateItemBadge(items, routerLink, undefined);
    this._menuItems.set(updatedItems);
  }

  public getMenuItemsForRole(role: UserRoles): MediPathMenuItem[] {
    const baseItems = this._menuItems();

    switch (role) {
      case UserRoles.DOCTOR:
        return [
          {
            role: UserRoles.DOCTOR,
            menuItems: [
              {
                label: 'Dashboard',
                icon: 'pi pi-home',
                routerLink: '/doctor/dashboard',
                visible: true,
              },
              {
                label: 'Schedule',
                icon: 'pi pi-calendar',
                routerLink: '/doctor/schedule',
                visible: true,
              },
              {
                label: 'Visits',
                icon: 'pi pi-file-medical',
                routerLink: '/doctor/visits',
                visible: true,
              },
              {
                label: 'Patients',
                icon: 'pi pi-users',
                routerLink: '/doctor/patients',
                visible: true,
              },
            ],
          },
        ];

      case UserRoles.PATIENT:
        return [
          {
            role: UserRoles.PATIENT,
            menuItems: [
              {
                label: 'Dashboard',
                icon: 'pi pi-home',
                routerLink: '/patient/dashboard',
                visible: true,
              },
              {
                label: 'Visits',
                icon: 'pi pi-calendar',
                routerLink: '/patient/appointments',
                visible: true,
              },
              {
                label: 'Prescriptions',
                icon: 'pi pi-file-medical',
                routerLink: '/patient/records',
                visible: true,
              },
              {
                label: 'Referrals',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
              {
                label: 'Medical History',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
              {
                label: 'Comments',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
              {
                label: 'Reminders',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
            ],
          },
        ];

      case (UserRoles.ADMIN, UserRoles.STAFF):
        return [
          {
            role: UserRoles.ADMIN,
            menuItems: [
              {
                label: 'Dashboard',
                icon: 'pi pi-home',
                routerLink: '/patient/dashboard',
                visible: true,
              },
              {
                label: 'Schedule',
                icon: 'pi pi-calendar',
                routerLink: '/patient/appointments',
                visible: true,
              },
              {
                label: 'Visits',
                icon: 'pi pi-file-medical',
                routerLink: '/patient/records',
                visible: true,
              },
              {
                label: 'Institutions',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
              {
                label: 'Notifications',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
              {
                label: 'Doctors',
                icon: 'pi pi-user',
                routerLink: '/patient/profile',
                visible: true,
              },
            ],
          },
        ];

      default:
        return baseItems;
    }
  }

  private updateItemBadge(
    items: MediPathMenuItem[],
    routerLink: string,
    badge?: string | number,
    badgeClass?: string,
  ): MediPathMenuItem[] {
    return items.map((item) => {
      const updatedMenuItems = item.menuItems.map((menuItem) => {
        if (menuItem.routerLink === routerLink) {
          return {
            ...menuItem,
            badge,
            badgeClass: badge ? badgeClass : undefined,
          };
        }
        return menuItem;
      });

      return {
        ...item,
        menuItems: updatedMenuItems,
      };
    });
  }
}
