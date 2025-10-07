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
import { TooltipModule } from 'primeng/tooltip';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { FilterParams } from '../../models/filter.model';
import { MedicalRecord } from '../../models/medical-history.model';
import { PatientMedicalHistoryService } from '../../services/patient-medical-history.service';
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

  private readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: '',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  });

  private readonly medicalHistoryService = inject(PatientMedicalHistoryService);
  ngOnInit(): void {
    this.loadMedicalHistory();
  }

  protected filteredRecords = computed(() => {
    let records = this.medicalRecords();
    const filter = this.filters();

    if (filter.searchTerm) {
      const searchTermLower = filter.searchTerm.toLowerCase();
      records = records.filter(
        (record) =>
          record.title.toLowerCase().includes(searchTermLower) ||
          record.notes?.toLowerCase().includes(searchTermLower) ||
          `${record.doctor.doctorName} ${record.doctor.doctorSurname}`
            .toLowerCase()
            .includes(searchTermLower),
      );
    }

    if (filter.dateFrom) {
      records = records.filter(
        (record) => record.date >= (filter.dateFrom as Date),
      );
    }

    if (filter.dateTo) {
      records = records.filter(
        (record) => record.date <= (filter.dateTo as Date),
      );
    }

    if (filter.sortField) {
      records = records.sort((a, b) => {
        const fieldA = a[filter.sortField as keyof MedicalRecord];
        const fieldB = b[filter.sortField as keyof MedicalRecord];

        if (fieldA == null && fieldB == null) {
          return 0;
        }
        if (fieldA == null) {
          return filter.sortOrder === 'asc' ? 1 : -1;
        }
        if (fieldB == null) {
          return filter.sortOrder === 'asc' ? -1 : 1;
        }
        if (fieldA < fieldB) {
          return filter.sortOrder === 'asc' ? -1 : 1;
        }
        if (fieldA > fieldB) {
          return filter.sortOrder === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return records;
  });

  protected viewRecord(record: MedicalRecord): void {
    const ref = this.dialogService.open(MedicalHistoryDialog, {
      header: 'Medical Record Details',
      width: '50%',
      data: {
        mode: 'edit',
        record,
      },
      closable: true,
    });

    if (!ref) {
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
    });

    if (!ref) {
      return;
    }

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated) => {
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
              doctorName: record.doctor.doctorName,
              doctorSurname: record.doctor.doctorSurname,
            },
            notes: record.note,
          }));
        }),
      )
      .subscribe((records) => {
        this.medicalRecords.set(records);
      });
  }
}
