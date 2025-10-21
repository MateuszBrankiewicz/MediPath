import { PaginatorState } from 'primeng/paginator';

/**
 * State interface for pagination
 * Compatible with PrimeNG Paginator
 */
export interface PaginationState {
  /** Current page first record index (0-based) */
  first: number;
  /** Number of rows per page */
  rows: number;
  /** Total number of records */
  totalRecords: number;
}

/**
 * Configuration for pagination
 */
export interface PaginationConfig {
  /** Initial rows per page (default: 10) */
  defaultRows?: number;
  /** Available rows per page options */
  rowsPerPageOptions?: number[];
  /** Show page report summary */
  showPageReport?: boolean;
}

/**
 * Type alias for PrimeNG PaginatorState
 */
export type { PaginatorState };
