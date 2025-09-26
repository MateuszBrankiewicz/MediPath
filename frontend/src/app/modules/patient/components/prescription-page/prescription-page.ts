import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Refferal } from '../../models/refferal-page.model';
import { PatientCodesService } from '../../services/patient-codes.service';
import { ToastService } from './../../../../core/services/toast/toast.service';

@Component({
  selector: 'app-prescription-page',
  imports: [ButtonModule, TableModule, DatePipe, CardModule, CommonModule],
  templateUrl: './prescription-page.html',
  styleUrl: './prescription-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PrescriptionPage {
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  protected codeService = inject(PatientCodesService);

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
}
