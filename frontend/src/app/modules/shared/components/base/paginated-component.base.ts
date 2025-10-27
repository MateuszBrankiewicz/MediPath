import { Directive, computed, signal } from '@angular/core';
import { PaginatorState } from 'primeng/paginator';

@Directive()
export abstract class PaginatedComponentBase<T> {
  protected readonly first = signal(0);

  protected readonly rows = signal(10);

  protected abstract get sourceData(): T[];

  protected readonly totalRecords = computed(() => this.sourceData.length);

  protected readonly paginatedData = computed(() => {
    const start = this.first();
    const end = start + this.rows();
    return this.sourceData.slice(start, end);
  });

  protected onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
  }

  protected resetPagination(): void {
    this.first.set(0);
  }

  protected setRowsPerPage(rows: number): void {
    this.rows.set(rows);
    this.resetPagination();
  }

  protected goToPage(pageNumber: number): void {
    if (pageNumber < 1) return;
    const maxPage = Math.ceil(this.totalRecords() / this.rows());
    if (pageNumber > maxPage) return;
    this.first.set((pageNumber - 1) * this.rows());
  }

  protected readonly currentPage = computed(
    () => Math.floor(this.first() / this.rows()) + 1,
  );

  protected readonly totalPages = computed(() =>
    Math.ceil(this.totalRecords() / this.rows()),
  );

  protected readonly hasNextPage = computed(
    () => this.currentPage() < this.totalPages(),
  );

  protected readonly hasPreviousPage = computed(() => this.currentPage() > 1);
}
