import { DatePipe } from '@angular/common';
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
import { ButtonModule } from 'primeng/button';
import { DataViewModule } from 'primeng/dataview';
import { DialogService } from 'primeng/dynamicdialog';
import { PanelModule } from 'primeng/panel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TooltipModule } from 'primeng/tooltip';
import { map } from 'rxjs';
import { FilterParams } from '../../../../core/models/filter.model';
import { MedicalRecord } from '../../../../core/models/medical-history.model';
import {
  FilterFieldConfig,
  FilteringService,
} from '../../../../core/services/filtering/filtering.service';
import { MedicalHistoryService } from '../../../../core/services/medical-history/medical-history.service';
import {
  SortFieldConfig,
  SortingService,
} from '../../../../core/services/sorting/sorting.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { MedicalHistoryDialog } from './components/medical-history-dialog/medical-history-dialog';

@Component({
  selector: 'app-medical-history-page',
  imports: [
    DataViewModule,
    DatePipe,
    ButtonModule,
    PanelModule,
    TooltipModule,
    FilterComponent,
    ProgressSpinnerModule,
  ],
  templateUrl: './medical-history-page.html',
  providers: [DialogService],
  styleUrl: './medical-history-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MedicalHistoryPage implements OnInit {
  protected readonly medicalRecords = signal<MedicalRecord[]>([]);
  protected translationService = inject(TranslationService);
  private destroyRef = inject(DestroyRef);
  private dialogService = inject(DialogService);
  private sortingService = inject(SortingService);
  private filteringService = inject(FilteringService);
  protected readonly isLoading = signal(false);

  protected readonly isSending = signal(false);
  private readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: '',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  });

  private readonly medicalHistoryService = inject(MedicalHistoryService);

  private readonly recordFilterConfig: FilterFieldConfig<MedicalRecord> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<MedicalRecord>(
        (r) => r.title,
        (r) => r.notes || '',
        (r) => `${r.doctor.doctorName} ${r.doctor.doctorSurname}`,
      ),
      this.filteringService.dateRangeConfig<MedicalRecord>((r) => r.date),
    );

  private readonly recordSortConfig: SortFieldConfig<MedicalRecord>[] = [
    this.sortingService.dateField('date', (r) => r.date),
    this.sortingService.stringField('title', (r) => r.title),
    this.sortingService.stringField(
      'doctor',
      (r) => `${r.doctor.doctorName} ${r.doctor.doctorSurname}`,
    ),
  ];

  ngOnInit(): void {
    this.isLoading.set(true);
    this.loadMedicalHistory();
  }

  protected filteredRecords = computed(() => {
    const records = this.medicalRecords();
    const filter = this.filters();

    const filtered = this.filteringService.filter(
      records,
      {
        searchTerm: filter.searchTerm,
        dateFrom: filter.dateFrom,
        dateTo: filter.dateTo,
      },
      this.recordFilterConfig,
    );

    return this.sortingService.sort(
      filtered,
      filter.sortField,
      filter.sortOrder,
      this.recordSortConfig,
    );
  });

  protected viewRecord(record: MedicalRecord): void {
    const hasDoctorInfo =
      !!record.doctor.doctorName && !!record.doctor.doctorSurname;
    const ref = this.dialogService.open(MedicalHistoryDialog, {
      header: 'Medical Record Details',
      width: '50%',
      data: {
        mode: hasDoctorInfo ? 'view' : 'edit',
        record,
      },
      closable: true,
      modal: true,
    });

    if (!ref && ref === null) {
      return;
    }

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated) => {
        if (updated) {
          this.loadMedicalHistory();
        }
      });
  }

  protected deleteRecord(recordId: string): void {
    const currentRecords = this.medicalRecords();
    this.medicalRecords.set(
      currentRecords.filter((record) => record.id !== recordId),
    );
  }

  protected addNewEntry(): void {
    const ref = this.dialogService.open(MedicalHistoryDialog, {
      header: 'Medical Record Details',
      width: '50%',
      data: {
        mode: 'edit',
        record: undefined,
      },
      closable: true,
      modal: true,
    });

    if (!ref) {
      return;
    }

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated) => {
        this.isSending.set(true);
        this.medicalHistoryService
          .addMedicalHistoryEntry({
            date: updated?.record.date,
            title: updated?.record.title ?? '',
            note: updated?.record.note ?? '',
            doctor: {
              doctorName: updated?.record.doctor.doctorName ?? '',
              doctorSurname: updated?.record.doctor.doctorSurname ?? '',
            },
          })
          .subscribe({
            next: () => {
              if (updated) {
                this.loadMedicalHistory();
              }
            },
            error: (err) => {
              console.error('Failed to add medical history entry:', err);
            },
          });
      });
  }

  protected onFiltersChange(filter: FilterParams): void {
    this.filters.set(filter);
  }

  private loadMedicalHistory(): void {
    this.medicalHistoryService
      .getMyMedicalHistory()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((response): MedicalRecord[] => {
          const medicalhistories = Array.isArray(response.medicalhistories)
            ? response.medicalhistories
            : [];
          return medicalhistories.map((record) => ({
            id: record.id,
            title: record.title,
            date: new Date(record.date),
            doctor: {
              doctorName: record.doctor?.doctorName || '',
              doctorSurname: record.doctor?.doctorSurname || '',
            },
            notes: record.note,
          }));
        }),
      )
      .subscribe((records) => {
        this.medicalRecords.set(records);
        this.isLoading.set(false);
        this.isSending.set(false);
      });
  }
}
