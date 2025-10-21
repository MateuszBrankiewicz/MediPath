import { Injectable } from '@angular/core';

export interface FilterFieldConfig<T> {
  searchFields?: ((item: T) => string | number | null | undefined)[];
  dateField?: (item: T) => Date | string | null | undefined;
  statusField?: (item: T) => string | null | undefined;
  customFilters?: ((item: T, filterValue: unknown) => boolean)[];
}

export interface FilterOptions {
  searchTerm?: string;
  status?: string;
  dateFrom?: Date | null;
  dateTo?: Date | null;
}

@Injectable({
  providedIn: 'root',
})
export class FilteringService {
  filter<T>(
    items: T[],
    options: FilterOptions,
    config: FilterFieldConfig<T>,
  ): T[] {
    if (!items || items.length === 0) {
      return items;
    }

    return items.filter((item) => {
      if (options.searchTerm && config.searchFields) {
        if (
          !this.matchesSearchTerm(item, options.searchTerm, config.searchFields)
        ) {
          return false;
        }
      }

      if (
        options.status &&
        options.status.toLowerCase() !== 'all' &&
        config.statusField
      ) {
        if (!this.matchesStatus(item, options.status, config.statusField)) {
          return false;
        }
      }

      if ((options.dateFrom || options.dateTo) && config.dateField) {
        if (
          !this.matchesDateRange(
            item,
            options.dateFrom,
            options.dateTo,
            config.dateField,
          )
        ) {
          return false;
        }
      }

      if (config.customFilters) {
        for (const customFilter of config.customFilters) {
          if (!customFilter(item, options)) {
            return false;
          }
        }
      }

      return true;
    });
  }

  private matchesSearchTerm<T>(
    item: T,
    searchTerm: string,
    searchFields: ((item: T) => string | number | null | undefined)[],
  ): boolean {
    const term = (searchTerm ?? '').trim().toLowerCase();
    if (term.length === 0) return true;

    return searchFields.some((getField) => {
      const value = getField(item);
      if (value == null) return false;
      return String(value).toLowerCase().includes(term);
    });
  }

  private matchesStatus<T>(
    item: T,
    status: string,
    statusField: (item: T) => string | null | undefined,
  ): boolean {
    const itemStatus = statusField(item);
    if (itemStatus == null) return false;

    const filterStatus = status.toLowerCase();
    const itemStatusLower = itemStatus.toLowerCase();

    if (filterStatus === 'cancelled' && itemStatusLower === 'canceled') {
      return true;
    }
    if (filterStatus === 'canceled' && itemStatusLower === 'cancelled') {
      return true;
    }

    return itemStatusLower === filterStatus;
  }

  private matchesDateRange<T>(
    item: T,
    dateFrom: Date | null | undefined,
    dateTo: Date | null | undefined,
    dateField: (item: T) => Date | string | null | undefined,
  ): boolean {
    const itemDateValue = dateField(item);
    if (itemDateValue == null) return false;

    const itemDate =
      itemDateValue instanceof Date ? itemDateValue : new Date(itemDateValue);

    if (isNaN(itemDate.getTime())) return false;

    let from: Date | null = null;
    let to: Date | null = null;

    if (dateFrom) {
      from = new Date(dateFrom);
      from.setHours(0, 0, 0, 0);
    }

    if (dateTo) {
      to = new Date(dateTo);
      to.setHours(23, 59, 59, 999);
    }

    if (from && itemDate < from) return false;
    if (to && itemDate > to) return false;

    return true;
  }

  searchConfig<T>(
    ...fields: ((item: T) => string | number | null | undefined)[]
  ): Partial<FilterFieldConfig<T>> {
    return {
      searchFields: fields,
    };
  }

  dateRangeConfig<T>(
    dateField: (item: T) => Date | string | null | undefined,
  ): Pick<FilterFieldConfig<T>, 'dateField'> {
    return {
      dateField,
    };
  }

  statusConfig<T>(
    statusField: (item: T) => string | null | undefined,
  ): Pick<FilterFieldConfig<T>, 'statusField'> {
    return {
      statusField,
    };
  }

  combineConfigs<T>(
    ...configs: Partial<FilterFieldConfig<T>>[]
  ): FilterFieldConfig<T> {
    return configs.reduce(
      (acc, config) => ({
        ...acc,
        ...config,
        searchFields: [
          ...(acc.searchFields ?? []),
          ...(config.searchFields ?? []),
        ],
        customFilters: [
          ...(acc.customFilters ?? []),
          ...(config.customFilters ?? []),
        ],
      }),
      {} as FilterFieldConfig<T>,
    );
  }
}
