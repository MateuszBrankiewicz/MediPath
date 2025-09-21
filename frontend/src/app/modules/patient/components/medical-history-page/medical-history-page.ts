import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { DataViewModule } from 'primeng/dataview';
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { TooltipModule } from 'primeng/tooltip';
import { MedicalRecord } from '../../models/medical-history.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-medical-history-page',
  imports: [DataViewModule, DatePipe, ButtonModule, PanelModule, TooltipModule],
  templateUrl: './medical-history-page.html',
  styleUrl: './medical-history-page.scss',
})
export class MedicalHistoryPage {
  protected readonly medicalRecords = signal<MedicalRecord[]>([]);
  protected translationService = inject(TranslationService);

  constructor() {
    this.medicalRecords.set([
      { id: 1, title: 'Diagnostic visit', date: new Date('2024-10-10') },
      { id: 2, title: 'Hearth Surgeon', date: new Date('2023-10-01') },
      { id: 3, title: 'Teeth removal', date: new Date('2023-10-01') },
    ]);
  }

  viewRecord(record: MedicalRecord): void {
    console.log('Viewing record:', record);
    // TODO: Implement view functionality
  }

  deleteRecord(recordId: number): void {
    const currentRecords = this.medicalRecords();
    this.medicalRecords.set(
      currentRecords.filter((record) => record.id !== recordId),
    );
  }

  addNewEntry(): void {
    console.log('Adding new entry');
    // TODO: Implement add new entry functionality
  }
}
