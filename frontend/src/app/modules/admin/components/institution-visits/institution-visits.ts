import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import {
  FilterFieldConfig,
  FilteringService,
} from '../../../../core/services/filtering/filtering.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import {
  SortFieldConfig,
  SortingService,
} from '../../../../core/services/sorting/sorting.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  AppointmentCardComponent,
  AppointmentCardData,
} from '../../../shared/components/appointment-card/appointment-card.component';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { Router } from '@angular/router';
import { SelectInstitution } from '../shared/select-institution/select-institution';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { ToastService } from '../../../../core/services/toast/toast.service';

@Component({
  selector: 'app-institution-visits',
  imports: [
    AppointmentCardComponent,
    FilterComponent,
    PaginatorModule,
    ProgressSpinnerModule,
    SelectInstitution,
  ],
  templateUrl: './institution-visits.html',
  styleUrl: './institution-visits.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionVisits extends PaginatedComponentBase<AppointmentCardData> {
  protected readonly translationService = inject(TranslationService);
  private readonly institutionService = inject(InstitutionService);
  private readonly institutionStore = inject(InstitutionStoreService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly sortingService = inject(SortingService);
  private readonly filteringService = inject(FilteringService);
  private readonly router = inject(Router);
  protected readonly visits = signal<AppointmentCardData[]>([]);
  protected readonly isLoading = signal<boolean>(true);
  protected readonly visitsService = inject(VisitsService);
  protected readonly toastService = inject(ToastService);
  protected readonly selectedInstitution = computed(() => {
    return this.institutionStore.selectedInstitution();
  });

  protected readonly filters = signal<{
    searchTerm: string;
    status: string;
    dateFrom: Date | null;
    dateTo: Date | null;
    sortField: string;
    sortOrder: 'asc' | 'desc';
  }>({
    searchTerm: '',
    status: 'all',
    dateFrom: null,
    dateTo: null,
    sortField: 'visitDate',
    sortOrder: 'desc',
  });

  constructor() {
    super();
    effect(() => {
      const selectedInstitution = this.selectedInstitution();
      if (!selectedInstitution) {
        return;
      }
      const institutionId = selectedInstitution.id;
      this.loadVisits(institutionId);
    });
  }

  private readonly visitFilterConfig: FilterFieldConfig<AppointmentCardData> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<AppointmentCardData>(
        (v) => v.patientName,
        (v) => v.institutionName,
      ),
      this.filteringService.statusConfig<AppointmentCardData>((v) => v.status),
      this.filteringService.dateRangeConfig<AppointmentCardData>((v) =>
        this.parseVisitDate(v.visitDate),
      ),
    );

  private readonly visitSortConfig: SortFieldConfig<AppointmentCardData>[] = [
    this.sortingService.dateField('visitDate', (v) =>
      this.parseVisitDate(v.visitDate),
    ),
    this.sortingService.stringField('patientName', (v) => v.patientName),
    this.sortingService.stringField('status', (v) => v.status),
  ];

  protected readonly filteredVisits = computed(() => {
    const filter = this.filters();
    return this.filteringService.filter(
      this.visits(),
      {
        searchTerm: filter.searchTerm,
        status: filter.status,
        dateFrom: filter.dateFrom,
        dateTo: filter.dateTo,
      },
      this.visitFilterConfig,
    );
  });

  protected readonly sortedVisits = computed(() => {
    const { sortField, sortOrder } = this.filters();
    return this.sortingService.sort(
      this.filteredVisits(),
      sortField,
      sortOrder,
      this.visitSortConfig,
    );
  });

  protected override get sourceData() {
    return this.sortedVisits();
  }

  protected readonly paginatedVisits = computed(() => {
    return this.paginatedData();
  });

  protected override readonly totalRecords = computed(
    () => this.sortedVisits().length,
  );

  protected readonly sortByOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('admin.visits.date'),
        value: 'visitDate',
      },
      {
        label: this.translationService.translate('admin.visits.patient'),
        value: 'patientName',
      },
      {
        label: this.translationService.translate('admin.visits.status'),
        value: 'status',
      },
    ];
  });

  protected readonly statusOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('shared.filters.all'),
        value: 'all',
      },
      {
        label: this.translationService.translate(
          'patient.visits.statusScheduled',
        ),
        value: 'scheduled',
      },
      {
        label: this.translationService.translate(
          'patient.visits.statusCompleted',
        ),
        value: 'completed',
      },
      {
        label: this.translationService.translate(
          'patient.visits.statusCanceled',
        ),
        value: 'canceled',
      },
    ];
  });

  protected onFiltersChange(ev: {
    searchTerm: string;
    status: string;
    dateFrom: Date | null;
    dateTo: Date | null;
    sortField: string;
    sortOrder: 'asc' | 'desc';
  }): void {
    this.filters.set(ev);
    this.resetPagination();
  }

  private parseVisitDate(dateStr: string): Date {
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      const day = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1;
      const year = parseInt(parts[2], 10);
      return new Date(year, month, day);
    }
    return new Date(dateStr);
  }

  private loadVisits(institutionId: string): void {
    this.isLoading.set(true);
    this.institutionService
      .getVisitsForInstitution(institutionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (visits) => {
          const mappedVisits = (Array.isArray(visits) ? visits : []).map(
            (visit) => ({
              id: visit.id,
              patientName: visit.patient
                ? `${visit.patient.name} ${visit.patient.surname}`
                : '',
              institutionName: visit.institution?.institutionName || '',
              visitDate: visit.time?.startTime
                ? this.formatDate(visit.time.startTime)
                : '',
              visitTime: visit.time?.startTime
                ? this.formatTime(visit.time.startTime)
                : '',
              status:
                visit.status === 'Upcoming'
                  ? 'scheduled'
                  : visit.status === 'Completed'
                    ? 'completed'
                    : visit.status === 'Cancelled'
                      ? 'canceled'
                      : ('scheduled' as 'completed' | 'scheduled' | 'canceled'),
              patientId: visit.patient?.userId,
            }),
          );
          this.visits.set(mappedVisits);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        },
      });
  }
  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleDateString('pl-PL', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
        })
      : '';
  }

  private formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleTimeString('pl-PL', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        })
      : '';
  }
  protected redirectToVisitInfo(visitId: string): void {
    this.router.navigate(['/admin/visits', visitId]);
  }
  protected onCancelVisit(_visitId: string): void {
    this.visitsService.cancelVisit(_visitId.toString()).subscribe(() => {
      this.toastService.showSuccess('Visit cancelled successfully.');
      this.visits.set(
        this.visits().map((visit) => {
          if (visit.id === _visitId) {
            visit.status = 'canceled';
          }
          return visit;
        }),
      );
    });
  }
}
