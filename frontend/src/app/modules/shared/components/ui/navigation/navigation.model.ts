import { MenuItemCommandEvent } from 'primeng/api';
import { UserRoles } from '../../core/services/authorization/authorization.model';

export interface MediPathMenuItem {
  role: UserRoles;
  menuItems: MediPathMenuLinks[];
}
export interface MediPathMenuLinks {
  label?: string;
  icon?: string;
  routerLink?: string;
  badge?: string | number;
  badgeClass?: string;
  visible?: boolean;
  disabled?: boolean;
  expanded?: boolean;
  command?: (event?: MenuItemCommandEvent) => void;
}
