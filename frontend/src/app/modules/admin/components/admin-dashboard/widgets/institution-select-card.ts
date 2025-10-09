import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';

export interface InstitutionOption {
  id: string;
  name: string;
}

@Component({
  selector: 'app-institution-select-card',
  imports: [CommonModule, CardModule, SelectModule, FormsModule],
  template: `
    <p-card class="inst-card">
      <div class="inst-header">
        <i class="pi pi-building"></i>
        <h3 class="inst-title">{{ title() }}</h3>
      </div>
      <p-select
        [options]="institutions()"
        optionLabel="name"
        optionValue="id"
        [ngModel]="selected()"
        (ngModelChange)="onChange($event)"
        styleClass="inst-select"
      ></p-select>
    </p-card>
  `,
  styles: [
    `
      .inst-card {
        height: 215px;
      }
      .inst-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 8px;
      }
      .inst-title {
        margin: 0;
        font-size: 16px;
      }
      .inst-select {
        width: 100%;
      }
    `,
  ],
})
export class InstitutionSelectCard {
  readonly title = input<string>('Select Institution');
  readonly institutions = input<InstitutionOption[]>([]);
  readonly selected = input<string | null>(null);
  readonly changed = output<string>();

  onChange(id: unknown): void {
    this.changed.emit(String(id ?? ''));
  }
}
