import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Institution } from '../../../../core/models/institution.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
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
import { AcceptActionDialogComponent } from '../../../shared/components/ui/accept-action-dialog/accept-action-dialog-component';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import {
  Hospital,
  HospitalCardComponent,
} from '../../../shared/components/ui/search-result.component/components/hospital-card.component/hospital-card.component';

@Component({
  selector: 'app-institution-list',
  templateUrl: './institution-list.html',
  styleUrl: './institution-list.scss',
  imports: [
    ProgressSpinnerModule,
    HospitalCardComponent,
    ButtonModule,
    FilterComponent,
    PaginatorModule,
  ],
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InstitutionList
  extends PaginatedComponentBase<Institution>
  implements OnInit
{
  protected readonly translationService = inject(TranslationService);
  protected readonly institutions = signal<Institution[]>([]);
  private readonly institutionService = inject(InstitutionService);
  private readonly destroyRef = inject(DestroyRef);
  protected isLoading = signal<boolean>(true);
  private authService = inject(AuthenticationService);
  private router = inject(Router);
  private sortingService = inject(SortingService);
  private filteringService = inject(FilteringService);
  private dialogService = inject(DialogService);
  private toastService = inject(ToastService);
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
    sortField: 'name',
    sortOrder: 'asc',
  });

  private readonly institutionFilterConfig: FilterFieldConfig<Institution> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<Institution>(
        (i) => i.name,
        (i) =>
          i.address
            ? `${i.address.street} ${i.address.number} ${i.address.city} ${i.address.province}`
            : '',
        (i) => i.specialisation?.join(' ') ?? '',
      ),
      this.filteringService.statusConfig<Institution>((i) =>
        i.isPublic ? 'public' : 'private',
      ),
    );

  private readonly institutionSortConfig: SortFieldConfig<Institution>[] = [
    this.sortingService.stringField('name', (i) => i.name),
    this.sortingService.stringField('city', (i) => i.address?.city ?? ''),
    this.sortingService.stringField('type', (i) =>
      i.isPublic ? 'public' : 'private',
    ),
  ];

  protected readonly filteredInstitutions = computed(() => {
    const filter = this.filters();
    return this.filteringService.filter(
      this.institutions(),
      {
        searchTerm: filter.searchTerm,
        status: filter.status,
      },
      this.institutionFilterConfig,
    );
  });

  protected readonly sortedInstitutions = computed(() => {
    const { sortField, sortOrder } = this.filters();
    return this.sortingService.sort(
      this.filteredInstitutions(),
      sortField,
      sortOrder,
      this.institutionSortConfig,
    );
  });

  protected override get sourceData() {
    return this.sortedInstitutions();
  }

  protected readonly paginatedInstitutions = computed(() => {
    return this.paginatedData();
  });

  protected override readonly totalRecords = computed(
    () => this.sortedInstitutions().length,
  );

  protected readonly sortByOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('admin.institutionList.name'),
        value: 'name',
      },
      {
        label: this.translationService.translate('admin.institutionList.city'),
        value: 'city',
      },
      {
        label: this.translationService.translate('admin.institutionList.type'),
        value: 'type',
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
          'admin.institutionList.public',
        ),
        value: 'public',
      },
      {
        label: this.translationService.translate(
          'admin.institutionList.private',
        ),
        value: 'private',
      },
    ];
  });

  ngOnInit(): void {
    this.loadInstitutions();
  }

  protected roleCode = computed(() => {
    return this.authService.getLastPanel();
  });

  private loadInstitutions(): void {
    this.isLoading.set(true);
    this.institutionService
      .getInstitutionsForAdmin(this.authService.getLastPanel())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (institutions) => {
          this.institutions.set(institutions);

          this.isLoading.set(false);
        },
        error: (error) => {
          console.error(
            this.translationService.translate('institutionList.loadError'),
            error,
          );
          this.isLoading.set(false);
        },
        complete: () => this.isLoading.set(false),
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

  protected getDataForInstitutionCard = computed<Hospital[]>(() => {
    const institutions = this.paginatedInstitutions();
    return institutions.map((inst) => ({
      id: inst?.id ?? '',
      name: inst?.name ?? '',
      address: inst?.address
        ? `${inst.address.street ?? ''} ${inst.address.number} ${inst.address.postalCode} ${inst.address.city}, ${inst.address.province}`.trim()
        : '',
      specialisation: inst?.types ?? [],
      isPublic: inst?.isPublic ?? false,
      imageUrl: inst.image ?? '',
    }));
  });

  protected redirectToInstitutionView(institutionId: string): void {
    this.router.navigate(['/admin/institutions', institutionId]);
  }

  protected isAdminForThisInstitution(): boolean {
    if (this.authService.getLastPanel() !== 'admin') {
      return false;
    }
    return true;
  }

  protected redirectToInstitutionEdit(institutionId: string): void {
    this.router.navigate([`/admin/institutions/${institutionId}/edit`]);
  }

  protected redirectToAddInstitution(): void {
    this.router.navigate(['/admin/institutions/add']);
  }

  protected disableInstitution(institutionId: string): void {
    const ref = this.dialogService.open(AcceptActionDialogComponent, {
      data: {
        message: this.translationService.translate(
          'confirmationDialogs.disableInstitutionMessage',
        ),
      },
      width: '400px',
    });

    ref.onClose.subscribe((accept: boolean) => {
      if (!accept) {
        return;
      }
      this.institutionService.deactivateInstitution(institutionId).subscribe({
        next: () => {
          this.toastService.showSuccess('institution.disabledSuccessfully');
          this.institutions.set(
            this.institutions().filter((inst) => inst.id !== institutionId),
          );
        },
        error: () => {
          this.toastService.showError('institution.disableError');
        },
      });
    });
  }
}
