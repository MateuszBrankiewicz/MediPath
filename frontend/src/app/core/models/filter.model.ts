export interface FilterParams {
  searchTerm: string;
  status: string;
  dateFrom: Date | null;
  dateTo: Date | null;
  sortField: string;
  sortOrder: 'asc' | 'desc';
}
