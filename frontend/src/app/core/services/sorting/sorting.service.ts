import { Injectable } from '@angular/core';

export interface SortFieldConfig<T> {
  field: string;
  getValue: (item: T) => string | number | Date | boolean | null | undefined;
  compare?: (a: T, b: T) => number;
}

@Injectable({
  providedIn: 'root',
})
export class SortingService {
  sort<T>(
    items: T[],
    sortField: string,
    sortOrder: 'asc' | 'desc',
    fieldConfigs: SortFieldConfig<T>[],
  ): T[] {
    if (!items || items.length === 0) {
      return items;
    }

    const config = fieldConfigs.find((c) => c.field === sortField);
    if (!config) {
      console.warn(`No sort configuration found for field: ${sortField}`);
      return items;
    }

    const direction = sortOrder === 'asc' ? 1 : -1;
    const sorted = [...items];

    if (config.compare) {
      sorted.sort((a, b) => config.compare!(a, b) * direction);
      return sorted;
    }

    sorted.sort((a, b) => {
      const aValue = config.getValue(a);
      const bValue = config.getValue(b);

      return this.compareValues(aValue, bValue) * direction;
    });

    return sorted;
  }

  private compareValues(
    a: string | number | Date | boolean | null | undefined,
    b: string | number | Date | boolean | null | undefined,
  ): number {
    if (a == null && b == null) return 0;
    if (a == null) return -1;
    if (b == null) return 1;

    if (a instanceof Date && b instanceof Date) {
      return a.getTime() - b.getTime();
    }

    if (typeof a === 'number' && typeof b === 'number') {
      return a - b;
    }

    if (typeof a === 'boolean' && typeof b === 'boolean') {
      return a === b ? 0 : a ? 1 : -1;
    }

    const aStr = String(a);
    const bStr = String(b);
    return aStr.localeCompare(bStr);
  }

  dateField<T>(
    field: string,
    getValue: (item: T) => Date | string | null | undefined,
  ): SortFieldConfig<T> {
    return {
      field,
      getValue: (item: T) => {
        const value = getValue(item);
        if (!value) return 0;
        if (value instanceof Date) return value.getTime();
        return new Date(value).getTime();
      },
    };
  }

  stringField<T>(
    field: string,
    getValue: (item: T) => string | null | undefined,
  ): SortFieldConfig<T> {
    return {
      field,
      getValue: (item: T) => getValue(item)?.toLowerCase() ?? '',
    };
  }

  numberField<T>(
    field: string,
    getValue: (item: T) => number | null | undefined,
  ): SortFieldConfig<T> {
    return {
      field,
      getValue: (item: T) => getValue(item) ?? 0,
    };
  }

  booleanField<T>(
    field: string,
    getValue: (item: T) => boolean | null | undefined,
  ): SortFieldConfig<T> {
    return {
      field,
      getValue: (item: T) => (getValue(item) ? 1 : 0),
    };
  }
}
