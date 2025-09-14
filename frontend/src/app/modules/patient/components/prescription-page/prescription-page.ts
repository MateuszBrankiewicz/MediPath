import { Component, signal } from '@angular/core';
import { Refferal, UsedState } from '../refferals-page/refferals-page';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-prescription-page',
  imports: [ButtonModule, TableModule, DatePipe],
  templateUrl: './prescription-page.html',
  styleUrl: './prescription-page.scss',
})
export class PrescriptionPage {
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
}
