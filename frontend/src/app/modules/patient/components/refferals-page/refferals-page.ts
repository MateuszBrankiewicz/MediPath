import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';

export enum UsedState {
  USED = 'USED',
  UNUSED = 'UNUSED',
}

export interface Refferal {
  id: number;
  doctorName: string;
  prescriptionPin: number;
  status: UsedState;
  date: Date;
}

@Component({
  selector: 'app-refferals-page',
  imports: [CommonModule, TableModule, ButtonModule],
  templateUrl: './refferals-page.html',
  styleUrl: './refferals-page.scss',
})
export class RefferalsPage {
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
}
