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
  selector: 'app-refferals-page',
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    CardModule,
    DatePipe,
    PaginatorModule,
    FilterComponent,
    ProgressSpinnerModule,
  ],
  templateUrl: './refferals-page.html',
  styleUrl: './refferals-page.scss',
  providers: [DialogService, PatientCodeDialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RefferalsPage
  extends PaginatedComponentBase<Refferal>
  implements OnInit
{
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  private codeService = inject(CodesService);
  private filterCodesService = inject(CodesFilterService);
  private readonly manageDialogCodeService = inject(PatientCodeDialogService);
  protected readonly isPrescriptionLoading = signal(false);
  protected readonly isLoading = signal(false);
  protected referrals = signal<Refferal[]>([]);

  protected readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: 'all',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  });

  protected readonly filteredPrescriptions = computed(() => {
    const filterValue = this.filters();
    const codes = this.filterCodesService.filterCodes(this.referrals() ?? [], {
      searchTerm: filterValue.searchTerm,
      status: filterValue.status,
      dateFrom: filterValue.dateFrom,
      dateTo: filterValue.dateTo,
      sortField: '',
      sortOrder: 'asc',
    });

    return codes;
  });

  protected override get sourceData() {
    return this.filteredPrescriptions();
  }

  protected override readonly totalRecords = computed(
    () => this.filteredPrescriptions().length,
  );

  protected readonly paginatedReferrals = computed(() => {
    return this.paginatedData();
  });

  ngOnInit(): void {
    this.isPrescriptionLoading.set(true);
    this.codeService
      .getPrescriptions()
      .pipe(
        map((results: Refferal[]) =>
          results.filter((code) => code.codeType?.toLowerCase() === 'referral'),
        ),
      )
      .subscribe((refferalResponse) => {
        this.referrals.set(refferalResponse);
        this.isPrescriptionLoading.set(false);
      });
  }

  protected onFiltersChange(ev: FilterParams): void {
    this.filters.set(ev);
    this.first.set(0);
  }

  protected copyToClipboard(pin: number): void {
    navigator.clipboard
      .writeText(pin.toString())
      .then(() => {
        this.toastService.showSuccess(
          this.translationService.translate('patient.common.pinCopied'),
        );
      })
      .catch(() => {
        this.toastService.showError(
          this.translationService.translate('patient.common.pinCopyFailed'),
        );
      });
  }

  protected getValidityDate(referralDate: Date): Date {
    const validityDate = new Date(referralDate);
    validityDate.setDate(validityDate.getDate() + 90);
    return validityDate;
  }

  protected markAsUsed(referral: Refferal): void {
    if (referral.status === 'USED') {
      this.toastService.showInfo(
        this.translationService.translate('patient.common.alreadyUsed'),
      );
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
              this.referrals()?.filter((refferal) => {
                if (refferal.prescriptionPin === referral.prescriptionPin) {
                  refferal.status = UsedState.USED;
                }
                return refferal;
              });
              this.toastService.showSuccess(
                this.translationService.translate(
                  'patient.common.markedAsUsed',
                ),
              );
              this.isLoading.set(false);
            });
        }
      });
  }
}
