import { Component, input, output } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ChipModule } from 'primeng/chip';

export interface EmployeeCardData {
  userId: string;
  name: string;
  surname: string;
  roleName?: string;
  pfpImage?: string;
  specialisation?: string[];
  rating?: number;
  pwz?: string;
}

@Component({
  selector: 'app-employee-card',
  imports: [CardModule, ButtonModule, ChipModule],
  templateUrl: './employee-card.html',
  styleUrl: './employee-card.scss',
})
export class EmployeeCard {
  employee = input.required<EmployeeCardData>();
  showViewButton = input<boolean>(true);
  showEditButton = input<boolean>(true);
  viewLabel = input<string>('');
  editLabel = input<string>('');

  view = output<string>();
  edit = output<string>();

  onView(): void {
    this.view.emit(this.employee().userId);
  }

  onEdit(): void {
    this.edit.emit(this.employee().userId);
  }
}
