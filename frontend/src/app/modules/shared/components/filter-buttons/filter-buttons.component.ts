import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
} from '@angular/core';

/**
 * Configuration for a single filter button
 */
export interface FilterButtonConfig<T = string> {
  /** Unique value for the filter */
  value: T;
  /** Translation key for the button label */
  labelKey: string;
  /** PrimeIcons icon class (e.g., 'pi-list') */
  icon: string;
}

/**
 * Reusable filter buttons component with type-safe values.
 * Uses modern Angular signals API with input() and output().
 *
 * @example
 * ```typescript
 * // In component:
 * filterConfig: FilterButtonConfig<VisitStatus>[] = [
 *   { value: 'all', labelKey: 'filter.all', icon: 'pi-list' },
 *   { value: 'booked', labelKey: 'filter.booked', icon: 'pi-user' }
 * ];
 *
 * onFilterChange(value: VisitStatus) {
 *   this.currentFilter.set(value);
 * }
 * ```
 *
 * ```html
 * <app-filter-buttons
 *   [filters]="filterConfig"
 *   [activeFilter]="currentFilter()"
 *   (filterChange)="onFilterChange($event)"
 * />
 * ```
 */
@Component({
  selector: 'app-filter-buttons',
  imports: [CommonModule],
  templateUrl: './filter-buttons.component.html',
  styleUrl: './filter-buttons.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterButtonsComponent<T = string> {
  /**
   * Array of filter button configurations
   */
  filters = input.required<FilterButtonConfig<T>[]>();

  /**
   * Currently active filter value
   */
  activeFilter = input.required<T>();

  /**
   * Event emitted when a filter button is clicked
   */
  filterChange = output<T>();

  /**
   * Computed signal to check if a filter is active
   */
  protected isActive = computed(() => {
    const active = this.activeFilter();
    return (value: T) => active === value;
  });

  /**
   * Handle filter button click
   */
  protected onFilterClick(value: T): void {
    this.filterChange.emit(value);
  }
}
