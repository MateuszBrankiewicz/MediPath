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
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { PaginatorModule } from 'primeng/paginator';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { DoctorProfile } from '../../../../core/models/doctor.model';
import {
  FilterFieldConfig,
  FilteringService,
} from '../../../../core/services/filtering/filtering.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import {
  SortFieldConfig,
  SortingService,
} from '../../../../core/services/sorting/sorting.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { SelectInstitution } from '../shared/select-institution/select-institution';

@Component({
  selector: 'app-doctor-list',
  imports: [
    CardModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    TagModule,
    FormsModule,
    SkeletonModule,
    SelectInstitution,
    FilterComponent,
    PaginatorModule,
  ],
  templateUrl: './doctor-list.html',
  styleUrl: './doctor-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DoctorList extends PaginatedComponentBase<DoctorProfile> {
  private institutionService = inject(InstitutionService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private router = inject(Router);
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  private sortingService = inject(SortingService);
  private filteringService = inject(FilteringService);

  protected doctors = signal<DoctorProfile[]>([]);
  protected isLoading = signal<boolean>(true);

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
    sortField: 'doctorName',
    sortOrder: 'asc',
  });

  private readonly doctorFilterConfig: FilterFieldConfig<DoctorProfile> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<DoctorProfile>(
        (d) => `${d.doctorName} ${d.doctorSurname}`,
        (d) => d.licenceNumber,
        (d) => d.doctorSchedules?.[0]?.doctor?.specialisations?.join(' ') ?? '',
      ),
    );

  private readonly doctorSortConfig: SortFieldConfig<DoctorProfile>[] = [
    this.sortingService.stringField(
      'doctorName',
      (d) => `${d.doctorName} ${d.doctorSurname}`,
    ),
    this.sortingService.stringField('licenceNumber', (d) => d.licenceNumber),
    this.sortingService.numberField('rating', (d) => d.rating),
    this.sortingService.numberField(
      'schedules',
      (d) => d.doctorSchedules?.length ?? 0,
    ),
  ];

  protected readonly filteredDoctors = computed(() => {
    const filter = this.filters();
    return this.filteringService.filter(
      this.doctors(),
      {
        searchTerm: filter.searchTerm,
      },
      this.doctorFilterConfig,
    );
  });

  protected readonly sortedDoctors = computed(() => {
    const { sortField, sortOrder } = this.filters();
    return this.sortingService.sort(
      this.filteredDoctors(),
      sortField,
      sortOrder,
      this.doctorSortConfig,
    );
  });

  protected override get sourceData() {
    return this.sortedDoctors();
  }

  protected readonly paginatedDoctors = computed(() => {
    return this.paginatedData();
  });

  protected override readonly totalRecords = computed(
    () => this.sortedDoctors().length,
  );

  protected readonly sortByOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('admin.doctorList.name'),
        value: 'doctorName',
      },
      {
        label: this.translationService.translate(
          'admin.doctorList.licenseNumber',
        ),
        value: 'licenceNumber',
      },
      {
        label: this.translationService.translate('admin.doctorList.rating'),
        value: 'rating',
      },
      {
        label: this.translationService.translate('admin.doctorList.schedules'),
        value: 'schedules',
      },
    ];
  });

  protected readonly institutionName = computed(() => {
    return this.institutionStoreService.getInstitution().name;
  });

  constructor() {
    super();
    effect(() => {
      const selectedInstitution =
        this.institutionStoreService.selectedInstitution();
      if (!selectedInstitution) {
        return;
      }
      const institutionId = selectedInstitution.id;
      this.loadDoctors(institutionId);
    });
  }

  private loadDoctors(doctorId: string): void {
    this.isLoading.set(true);

    this.institutionService
      .getEmployeesForInstitution(doctorId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (doctors) => {
          this.doctors.set(doctors);
          this.isLoading.set(false);
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate('admin.doctorList.error.load'),
          );
          this.isLoading.set(false);
        },
      });
  }

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

  protected onAddDoctor(): void {
    this.router.navigate([
      `/admin/doctors/${this.institutionStoreService.selectedInstitution()?.id}/add`,
    ]);
  }

  protected onViewDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId]);
  }

  protected onEditDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId, 'edit']);
  }

  protected formatRating(rating: number): string {
    return rating.toFixed(1);
  }

  protected getRatingColor(
    rating: number,
  ): 'success' | 'info' | 'warn' | 'danger' {
    if (rating >= 4.5) return 'success';
    if (rating >= 3.5) return 'info';
    if (rating >= 2.5) return 'warn';
    return 'danger';
  }
}
