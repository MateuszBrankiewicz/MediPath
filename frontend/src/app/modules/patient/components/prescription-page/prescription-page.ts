import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogService } from 'primeng/dynamicdialog';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { catchError, map } from 'rxjs';
import { FilterParams } from '../../../../core/models/filter.model';
import { Refferal, UsedState } from '../../../../core/models/refferal.model';
import { CodesService } from '../../../../core/services/codes/codes.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { CodesFilterService } from '../../services/codesFilter.service';
import { PatientCodeDialogService } from '../../services/paitent-code-dialog.service';

@Component({
  selector: 'app-prescription-page',
  imports: [
    ButtonModule,
    TableModule,
    DatePipe,
    CardModule,
    CommonModule,
    FilterComponent,
    ProgressSpinnerModule,
    PaginatorModule,
  ],
  templateUrl: './prescription-page.html',
  styleUrl: './prescription-page.scss',
  providers: [DialogService, PatientCodeDialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PrescriptionPage
  extends PaginatedComponentBase<Refferal>
  implements OnInit
{
  private toastService = inject(ToastService);
  private manageDialogCodeService = inject(PatientCodeDialogService);
  protected translationService = inject(TranslationService);
  protected codeService = inject(CodesService);
  private filterCodesService = inject(CodesFilterService);
  protected readonly isPrescriptionLoading = signal(false);
  protected readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: 'all',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  });

  protected override readonly totalRecords = computed(
    () => this.filteredPrescriptions().length,
  );
  protected readonly isLoading = signal(false);
  protected readonly filteredPrescriptions = computed(() => {
    const filterValue = this.filters();
    const codes = this.filterCodesService.filterCodes(
      this.prescriptions() ?? [],
      {
        searchTerm: filterValue.searchTerm,
        status: filterValue.status,
        dateFrom: filterValue.dateFrom,
        dateTo: filterValue.dateTo,
        sortField: '',
        sortOrder: 'asc',
      },
    );

    return codes;
  });

  protected override get sourceData() {
    return this.filteredPrescriptions();
  }

  protected readonly paginatedPrescriptions = computed(() => {
    return this.paginatedData();
  });

  ngOnInit(): void {
    this.isPrescriptionLoading.set(true);
    this.codeService
      .getPrescriptions()
      .pipe(
        map((results: Refferal[]) => {
          this.isPrescriptionLoading.set(true);
          return results.filter(
            (code) => code.codeType?.toLowerCase() === 'prescription',
          );
        }),
      )
      .subscribe((prescriptions) => {
        this.prescriptions.set(prescriptions);
        this.isPrescriptionLoading.set(false);
      });
  }

  protected statusOptions = [
    {
      label: this.translationService.translate('shared.filters.all'),
      value: 'all',
    },
    {
      label: this.translationService.translate('code.statusActive'),
      value: 'UNUSED',
    },
    {
      label: this.translationService.translate('code.statusUsed'),
      value: 'USED',
    },
  ];
  protected prescriptions = signal<Refferal[]>([]);

  protected copyToClipboard(pin: number): void {
    navigator.clipboard
      .writeText(pin.toString())
      .then(() => {
        this.toastService.showSuccess(
          this.translationService.translate('patient.common.pinCopied'),
        );
      })
      .catch((err) => {
        console.error('Failed to copy PIN:', err);
        this.toastService.showError(
          this.translationService.translate('patient.common.pinCopyFailed'),
        );
      });
  }

  protected getValidityDate(prescriptionDate: Date): Date {
    const validityDate = new Date(prescriptionDate);
    validityDate.setDate(validityDate.getDate() + 30);
    return validityDate;
  }

  protected markAsUsed(referral: Refferal): void {
    if (referral.status === 'USED') {
      this.toastService.showInfo('This referral is already marked as used.');
      return;
    }
    this.manageDialogCodeService
      .useCode({
        codeNumber: referral.prescriptionPin,
        codeType: 'referral',
      })
      .subscribe((success) => {
        if (success) {
          this.isLoading.set(true);
          this.codeService
            .useCode({
              code: referral.prescriptionPin,
              codeType: referral.codeType ?? '',
            })
            .pipe(
              catchError((err) => {
                this.isLoading.set(false);
                throw err;
              }),
            )
            .subscribe(() => {
              this.prescriptions()?.filter((prescription) => {
                if (prescription.prescriptionPin === referral.prescriptionPin) {
                  prescription.status = UsedState.USED;
                }
                return prescription;
              });
              this.isLoading.set(false);
              this.toastService.showSuccess('Referral marked as used.');
            });
        }
      });
  }

  protected deleteCode(referral: Refferal): void {
    this.manageDialogCodeService
      .deleteCode({
        codeNumber: referral.prescriptionPin,
        codeType: 'prescription',
      })
      .subscribe((success) => {
        if (success) {
          this.isLoading.set(true);
          this.codeService
            .deleteCode({
              code: referral.prescriptionPin,
              codeType: referral.codeType ?? '',
            })
            .pipe(
              catchError((err) => {
                this.isLoading.set(false);
                throw err;
              }),
            )
            .subscribe(() => {
              this.prescriptions.set(
                this.prescriptions().filter(
                  (prescription) =>
                    prescription.prescriptionPin !== referral.prescriptionPin,
                ),
              );
              this.isLoading.set(false);
              this.toastService.showSuccess(
                'Prescription deleted successfully.',
              );
            });
        }
      });
  }

  protected onFiltersChange(filterValue: FilterParams): void {
    this.filters.set(filterValue);
  }
}
