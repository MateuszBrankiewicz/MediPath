import { CommonModule } from '@angular/common';
import { Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  AutoCompleteCompleteEvent,
  AutoCompleteModule,
} from 'primeng/autocomplete';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InstitutionShortInfo } from '../../../../../core/models/institution.model';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-doctor-institutions-list',
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    DialogModule,
    AutoCompleteModule,
  ],
  templateUrl: './doctor-institutions-list.html',
  styleUrl: './doctor-institutions-list.scss',
})
export class DoctorInstitutionsListComponent {
  institutions = input.required<InstitutionShortInfo[]>();
  availableInstitutions = input<InstitutionShortInfo[]>([]);
  translationKey = input.required<string>();

  institutionAdded = output<InstitutionShortInfo>();
  institutionRemoved = output<string>();

  protected translationService = inject(TranslationService);

  protected displayAddDialog = signal(false);
  protected selectedInstitution: InstitutionShortInfo | null = null;
  protected filteredInstitutions = signal<InstitutionShortInfo[]>([]);

  protected showAddDialog(): void {
    this.displayAddDialog.set(true);
    this.selectedInstitution = null;
  }

  protected hideAddDialog(): void {
    this.displayAddDialog.set(false);
    this.selectedInstitution = null;
  }

  protected searchInstitution(event: AutoCompleteCompleteEvent): void {
    const query = event.query.toLowerCase();
    const available = this.availableInstitutions();
    const currentIds = this.institutions().map((i) => i.institutionId);

    this.filteredInstitutions.set(
      available.filter(
        (inst) =>
          !currentIds.includes(inst.institutionId) &&
          inst.institutionName.toLowerCase().includes(query),
      ),
    );
  }

  protected confirmAddInstitution(): void {
    if (this.selectedInstitution) {
      this.institutionAdded.emit(this.selectedInstitution);
      this.hideAddDialog();
    }
  }

  protected removeInstitution(institutionId: string): void {
    this.institutionRemoved.emit(institutionId);
  }
}
