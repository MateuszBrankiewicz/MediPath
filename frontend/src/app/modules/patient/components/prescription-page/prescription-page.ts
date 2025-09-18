import { ToastService } from './../../../../core/services/toast/toast.service';
import { Component, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DatePipe, CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { Refferal, UsedState } from '../../models/refferal-page.model';

@Component({
  selector: 'app-prescription-page',
  imports: [ButtonModule, TableModule, DatePipe, CardModule, CommonModule],
  templateUrl: './prescription-page.html',
  styleUrl: './prescription-page.scss',
})
export class PrescriptionPage {
  private toastService = inject(ToastService);

  protected prescriptions = signal<Refferal[]>([
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
