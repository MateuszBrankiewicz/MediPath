import { inject, Injectable, signal } from '@angular/core';
import { UserRoles } from '../../../../../core/services/authentication/authentication.model';
import { TranslationService } from '../../../../../core/services/translation/translation.service';
import { MediPathMenuItem } from './navigation.model';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  private readonly _menuItems = signal<MediPathMenuItem[]>([]);
  private translationService = inject(TranslationService);

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
                label: this.translationService.translate(
                  'navigation.dashboard',
                ),
                icon: 'pi pi-home',
                routerLink: '/doctor/dashboard',
                visible: true,
              },
              {
                label: this.translationService.translate('navigation.schedule'),
                icon: 'pi pi-calendar',
                routerLink: '/doctor/schedule',
                visible: true,
              },
              {
                label: this.translationService.translate('navigation.visits'),
                icon: 'pi pi-file-medical',
                routerLink: '/doctor/visits',
                visible: true,
              },
              {
                label: this.translationService.translate('navigation.patients'),
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
                label: this.translationService.translate(
                  'navigation.dashboard',
                ),
                icon: 'pi pi-home',
                routerLink: '/patient/dashboard',
                visible: true,
              },
              {
                label: this.translationService.translate('navigation.visits'),
                icon: 'pi pi-list',
                routerLink: '/patient/visits',
                visible: true,
              },
              {
                label: this.translationService.translate(
                  'navigation.prescriptions',
                ),
                icon: 'pi pi-receipt',
                routerLink: '/patient/prescriptions',
                visible: true,
              },
              {
                label: this.translationService.translate(
                  'navigation.referrals',
                ),
                icon: 'pi pi-file-plus',
                routerLink: '/patient/referrals',
                visible: true,
              },
              {
                label: this.translationService.translate(
                  'navigation.medicalHistory',
                ),
                icon: 'pi pi-history',
                routerLink: '/patient/medical-history',
                visible: true,
              },
              {
                label: this.translationService.translate('navigation.comments'),
                icon: 'pi pi-comments',
                routerLink: '/patient/comments',
                visible: true,
              },
              {
                label: this.translationService.translate(
                  'navigation.notifications',
                ),
                icon: 'pi pi-bell',
                routerLink: '/patient/reminders',
                visible: true,
              },
            ],
          },
        ];

      case UserRoles.ADMIN:
      case UserRoles.STAFF:
        console.log('Admin menu items');
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
                routerLink: '/admin/add-schedule',
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
                routerLink: 'admin/institutions',
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
