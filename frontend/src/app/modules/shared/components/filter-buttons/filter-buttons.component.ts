import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';

export interface FilterButtonConfig<T = string> {
  value: T;
  labelKey: string;
  icon: string;
}

@Component({
  selector: 'app-filter-buttons',
  imports: [CommonModule],
  templateUrl: './filter-buttons.component.html',
  styleUrl: './filter-buttons.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterButtonsComponent<T = string> {
  filters = input.required<FilterButtonConfig<T>[]>();

  activeFilter = input.required<T>();

  filterChange = output<T>();

  protected isActive = computed(() => {
    const active = this.activeFilter();
    return (value: T) => active === value;
  });

  protected onFilterClick(value: T): void {
    this.filterChange.emit(value);
  }
}
