import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { map } from 'rxjs';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Refferal } from '../../models/refferal-page.model';
import { PatientCodesService } from '../../services/patient-codes.service';

@Component({
  selector: 'app-refferals-page',
  imports: [CommonModule, TableModule, ButtonModule, CardModule, DatePipe],
  templateUrl: './refferals-page.html',
  styleUrl: './refferals-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RefferalsPage {
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  private codeService = inject(PatientCodesService);
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
}
