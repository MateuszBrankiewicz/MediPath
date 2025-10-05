import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogService } from 'primeng/dynamicdialog';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { TableModule } from 'primeng/table';
import { catchError, map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { Refferal, UsedState } from '../../models/refferal-page.model';
import { CodesFilterService } from '../../services/codesFilter.service';
import { PatientCodesService } from '../../services/patient-codes.service';
import { ToastService } from './../../../../core/services/toast/toast.service';
import { PatientCodeDialogService } from './../../services/paitent-code-dialog.service';

@Component({
  selector: 'app-prescription-page',
  imports: [
    ButtonModule,
    TableModule,
    DatePipe,
    CardModule,
    CommonModule,
    FilterComponent,
    PaginatorModule,
  ],
  templateUrl: './prescription-page.html',
  styleUrl: './prescription-page.scss',
  providers: [DialogService, PatientCodeDialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PrescriptionPage {
  private toastService = inject(ToastService);
  private manageDialogCodeService = inject(PatientCodeDialogService);
  protected translationService = inject(TranslationService);
  protected codeService = inject(PatientCodesService);
  private filterCodesService = inject(CodesFilterService);

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
    sortField: 'date',
    sortOrder: 'desc',
  });
  protected readonly first = signal(0);
  protected readonly rows = signal(10);

  protected readonly totalRecords = computed(
    () => this.prescriptions()?.length ?? 0,
  );

  protected readonly filteredPrescriptions = computed(() => {
    const filterValue = this.filters();
    let codes = this.filterCodesService.filterCodes(
      this.prescriptions() ?? [],
      {
        searchTerm: filterValue.searchTerm,
        status: filterValue.status,
        dateFrom: filterValue.dateFrom,
        dateTo: filterValue.dateTo,
      },
    );

    codes = this.filterCodesService.sortCodes(
      codes,
      filterValue.sortField,
      filterValue.sortOrder,
    );
    return codes;
  });

  protected readonly paginatedPrescriptions = computed(() => {
    const first = this.first();
    const rows = this.rows();
    return this.filteredPrescriptions().slice(first, first + rows);
  });

  protected onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
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
  protected prescriptions = toSignal<Refferal[]>(
    this.codeService
      .getPrescriptions()
      .pipe(
        map((results: Refferal[]) =>
          results.filter(
            (code) => code.codeType?.toLowerCase() === 'prescription',
          ),
        ),
      ),
  );

  protected copyToClipboard(pin: number): void {
    navigator.clipboard
      .writeText(pin.toString())
      .then(() => {
        this.toastService.showSuccess('PIN copied to clipboard');
      })
      .catch((err) => {
        console.error('Failed to copy PIN:', err);
      });
  }

  protected getValidityDate(prescriptionDate: Date): Date {
    const validityDate = new Date(prescriptionDate);
    validityDate.setDate(validityDate.getDate() + 30);
    return validityDate;
  }

  protected markAsUsed(referral: Refferal): void {
    console.log('Attempting to mark referral as used:', referral);
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
          this.codeService
            .useCode({
              code: referral.prescriptionPin,
              codeType: referral.codeType ?? '',
            })
            .pipe(
              catchError((err) => {
                console.log(err);
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
              this.toastService.showSuccess('Referral marked as used.');
            });
        }
      });
  }

  protected onFiltersChange(filterValue: {
    searchTerm: string;
    status: string;
    dateFrom: Date | null;
    dateTo: Date | null;
    sortField: string;
    sortOrder: 'asc' | 'desc';
  }) {
    this.filters.set(filterValue);
  }
}
