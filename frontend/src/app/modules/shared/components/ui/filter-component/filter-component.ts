import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { DrawerModule } from 'primeng/drawer';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TranslationService } from '../../../../../core/services/translation/translation.service';
import { FilterParams } from '../../../../patient/models/filter.model';

@Component({
  selector: 'app-filter-component',
  imports: [
    InputText,
    SelectModule,
    FormsModule,
    ButtonModule,
    DatePicker,
    DrawerModule,
    FloatLabelModule,
  ],
  templateUrl: './filter-component.html',
  styleUrl: './filter-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FilterComponent {
  public readonly translationService = inject(TranslationService);

  readonly showSearch = input<boolean>(true);
  readonly showStatus = input<boolean>(true);
  readonly showDates = input<boolean>(true);
  readonly showSort = input<boolean>(true);
  readonly showActions = input<boolean>(true);
  readonly autoApply = input<boolean>(false);

  protected readonly showFiltersDrawer = signal(false);

  protected toggleFilters(): void {
    this.showFiltersDrawer.update((v) => !v);
  }

  readonly statusOptionsInput = input<
    { label: string; value: string }[] | null
  >(null);
  readonly sortByOptionsInput = input<
    { label: string; value: string }[] | null
  >(null);
  readonly sortOrderOptionsInput = input<
    { label: string; value: 'asc' | 'desc' }[] | null
  >(null);
  readonly searchPlaceholder = input<string | null>(null);

  readonly defaults = input<FilterParams | null>(null);
  readonly syncWithDefaults = input<boolean>(false);

  readonly defaultStatusOptions = computed(() => {
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
        value: 'cancelled',
      },
    ];
  });
  readonly statusOptions = computed(
    () => this.statusOptionsInput() ?? this.defaultStatusOptions(),
  );
  selectedStatus = signal<string>('all');
  selectedDate = signal<Date | null>(null);
  searchTerm = signal<string>('');
  selectedEndDate = signal<Date | null>(null);

  sortField = signal<string>('date');
  sortOrder = signal<'asc' | 'desc'>('desc');

  private readonly defaultStatus = 'ALL';

  protected readonly activeFiltersCount = computed(() => {
    let count = 0;

    if ((this.searchTerm()?.toString() ?? '').trim().length > 0) {
      count++;
    }

    if (
      this.selectedStatus() !== null &&
      this.selectedStatus() !== undefined &&
      this.selectedStatus().toLowerCase() !== this.defaultStatus.toLowerCase()
    ) {
      count++;
    }

    if (this.selectedDate() instanceof Date) count++;
    if (this.selectedEndDate() instanceof Date) count++;

    return count;
  });

  protected areFiltersApplied(): boolean {
    return this.activeFiltersCount() > 0;
  }

  readonly defaultSortByOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('shared.filters.date'),
        value: 'date',
      },
      {
        label: this.translationService.translate('patient.visits.doctor'),
        value: 'doctorName',
      },
      {
        label: this.translationService.translate('patient.visits.institution'),
        value: 'institution',
      },
      {
        label: this.translationService.translate('patient.visits.status'),
        value: 'status',
      },
    ];
  });
  readonly sortByOptions = computed(
    () => this.sortByOptionsInput() ?? this.defaultSortByOptions(),
  );

  readonly defaultSortOrderOptions = computed(() => {
    this.translationService.language();
    return [
      {
        label: this.translationService.translate('shared.filters.ascending'),
        value: 'asc',
      },
      {
        label: this.translationService.translate('shared.filters.descending'),
        value: 'desc',
      },
    ];
  });
  readonly sortOrderOptions = computed(
    () => this.sortOrderOptionsInput() ?? this.defaultSortOrderOptions(),
  );

  readonly filtersChange = output<FilterParams>();

  private emitChange(): void {
    this.filtersChange.emit({
      searchTerm: this.searchTerm(),
      status: this.selectedStatus(),
      dateFrom: this.selectedDate(),
      dateTo: this.selectedEndDate(),
      sortField: this.sortField(),
      sortOrder: this.sortOrder(),
    });
  }

  clearFilters(): void {
    const d = this.defaults();
    this.searchTerm.set(d?.searchTerm ?? '');
    this.selectedStatus.set(d?.status ?? 'all');
    this.selectedDate.set(d?.dateFrom ?? null);
    this.selectedEndDate.set(d?.dateTo ?? null);
    this.sortField.set(d?.sortField ?? 'date');
    this.sortOrder.set(d?.sortOrder ?? 'desc');
    this.emitChange();
    this.showFiltersDrawer.set(false);
  }

  applyFilters(): void {
    this.showFiltersDrawer.set(false);
    this.emitChange();
  }

  private _initializedFromDefaults = false;
  readonly _syncDefaults = effect(() => {
    const d = this.defaults();
    const sync = this.syncWithDefaults();
    if (!this._initializedFromDefaults || sync) {
      this.searchTerm.set(d?.searchTerm ?? this.searchTerm());
      this.selectedStatus.set(d?.status ?? this.selectedStatus());
      this.selectedDate.set(d?.dateFrom ?? this.selectedDate());
      this.selectedEndDate.set(d?.dateTo ?? this.selectedEndDate());
      this.sortField.set(d?.sortField ?? this.sortField());
      this.sortOrder.set(d?.sortOrder ?? this.sortOrder());
      this._initializedFromDefaults = true;
    }
  });

  readonly _autoEmit = effect(() => {
    if (!this.autoApply()) return;
    this.searchTerm();
    this.selectedStatus();
    this.selectedDate();
    this.selectedEndDate();
    this.sortField();
    this.sortOrder();
    this.emitChange();
  });
}
