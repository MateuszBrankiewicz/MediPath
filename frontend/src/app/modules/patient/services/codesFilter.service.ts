import { Injectable } from '@angular/core';
import { FilterParams } from '../models/filter.model';
import { Refferal, UsedState } from '../models/refferal-page.model';

@Injectable({
  providedIn: 'root',
})
export class CodesFilterService {
  public filterCodes(codes: Refferal[], filters: FilterParams) {
    if (!codes) return [];
    const search = filters.searchTerm?.toLowerCase().trim() ?? '';
    const statusFilter = (filters.status ?? 'all').toUpperCase();

    const from = filters.dateFrom ? new Date(filters.dateFrom) : null;
    const to = filters.dateTo ? new Date(filters.dateTo) : null;

    codes.filter((value) => {
      if (
        statusFilter !== 'all'.toUpperCase() &&
        value.status !== (statusFilter as UsedState)
      ) {
        return false;
      }

      if (from && value.date < from) return false;
      if (to && value.date > to) return false;
      if (search) {
        const hay =
          `${value.doctorName} ${value.status} ${value.prescriptionPin}`
            .toLowerCase()
            .trim();
        if (!hay.includes(search)) return false;
      }
      return true;
    });
    codes = this.sortCodes(codes, filters.sortField, filters.sortOrder);
    return codes;
  }

  public sortCodes(
    codes: Refferal[],
    sortField: string,
    sortOrder: 'asc' | 'desc',
  ) {
    const dir = sortOrder === 'asc' ? 1 : -1;
    codes.sort((firstValue, secondValue) => {
      let aValue: number | string = 0;
      let bValue: number | string = 0;
      switch (sortField) {
        case 'doctorName':
          aValue = firstValue.doctorName?.toLowerCase() ?? '';
          bValue = secondValue.doctorName?.toLowerCase() ?? '';
          break;

        case 'status':
          aValue = String(firstValue.status).toLowerCase();
          bValue = String(secondValue.status).toLowerCase();
          break;
        case 'date':
        default:
          aValue = firstValue.date?.getTime?.() ?? 0;
          bValue = secondValue.date?.getTime?.() ?? 0;
      }
      if (aValue < bValue) return -1 * dir;
      if (aValue > bValue) return 1 * dir;
      return 0;
    });
    return codes;
  }
}
