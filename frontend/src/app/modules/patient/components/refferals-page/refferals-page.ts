import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogService } from 'primeng/dynamicdialog';
import { TableModule } from 'primeng/table';
import { map } from 'rxjs';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Refferal } from '../../models/refferal-page.model';
import { PatientCodeDialogService } from '../../services/paitent-code-dialog.service';
import { PatientCodesService } from '../../services/patient-codes.service';

@Component({
  selector: 'app-refferals-page',
  imports: [CommonModule, TableModule, ButtonModule, CardModule, DatePipe],
  templateUrl: './refferals-page.html',
  styleUrl: './refferals-page.scss',
  providers: [DialogService, PatientCodeDialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RefferalsPage {
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  private codeService = inject(PatientCodesService);

  private readonly manageDialogCodeService = inject(PatientCodeDialogService);

  protected referrals = toSignal<Refferal[]>(
    this.codeService
      .getPrescriptions()
      .pipe(
        map((results: Refferal[]) =>
          results.filter((code) => code.codeType?.toLowerCase() === 'referral'),
        ),
      ),
  );

  protected copyToClipboard(pin: number): void {
    navigator.clipboard
      .writeText(pin.toString())
      .then(() => {
        console.log('PIN copied to clipboard:', pin);
        this.toastService.showSuccess('PIN copied to clipboard');
      })
      .catch(() => {
        this.toastService.showError('Failed to copy PIN');
      });
  }

  protected getValidityDate(referralDate: Date): Date {
    const validityDate = new Date(referralDate);
    validityDate.setDate(validityDate.getDate() + 90);
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
              codeNumber: referral.prescriptionPin,
              codeType: 'referral',
            })
            .subscribe((res) => {
              console.log(res);
              this.referrals();
              this.toastService.showSuccess('Referral marked as used.');
            });
        }
      });
  }
}
