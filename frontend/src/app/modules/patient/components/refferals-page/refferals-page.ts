import { CommonModule, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { Refferal, UsedState } from '../../models/refferal-page.model';

@Component({
  selector: 'app-refferals-page',
  imports: [CommonModule, TableModule, ButtonModule, CardModule, DatePipe],
  templateUrl: './refferals-page.html',
  styleUrl: './refferals-page.scss',
})
export class RefferalsPage {
  private toastService = inject(ToastService);
  protected referrals = signal<Refferal[]>([
    {
      id: 1,
      doctorName: 'Dr. Smith',
      prescriptionPin: 12345,
      status: UsedState.UNUSED,
      date: new Date('2023-10-01'),
    },
    {
      id: 2,
      doctorName: 'Dr. Johnson',
      prescriptionPin: 67890,
      status: UsedState.USED,
      date: new Date('2023-09-15'),
    },
  ]);

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
