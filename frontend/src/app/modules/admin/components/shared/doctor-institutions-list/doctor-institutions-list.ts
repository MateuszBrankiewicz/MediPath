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
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { InstitutionShortInfo } from '../../../../../core/models/institution.model';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

interface RoleOption {
  id: number;
  name: string;
  roleCode: number;
}

@Component({
  selector: 'app-doctor-institutions-list',
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    DialogModule,
    AutoCompleteModule,
    SelectModule,
    TagModule,
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
  roleChanged = output<{ institutionId: string; roleCode: number }>();

  protected translationService = inject(TranslationService);

  protected roleOptions: RoleOption[] = [
    { id: 1, name: 'Doctor', roleCode: 2 },
    { id: 2, name: 'Staff', roleCode: 4 },
    { id: 3, name: 'Admin', roleCode: 8 },
  ];

  protected displayAddDialog = signal(false);
  protected displayEditDialog = signal(false);
  protected selectedInstitution: InstitutionShortInfo | null = null;
  protected selectedRole = signal<RoleOption | null>(null);
  protected editingInstitution: InstitutionShortInfo | null = null;
  protected editRole = signal<RoleOption | null>(null);
  protected filteredInstitutions = signal<InstitutionShortInfo[]>([]);

  protected showAddDialog(): void {
    this.displayAddDialog.set(true);
    this.selectedInstitution = null;
    this.selectedRole.set(this.roleOptions[0]); // Default to Doctor
  }

  protected hideAddDialog(): void {
    this.displayAddDialog.set(false);
    this.selectedInstitution = null;
    this.selectedRole.set(null);
  }

  protected showEditDialog(institution: InstitutionShortInfo): void {
    this.editingInstitution = institution;
    const currentRole = this.roleOptions.find(
      (r) => r.roleCode === institution.roleCode,
    );
    this.editRole.set(currentRole || this.roleOptions[0]);
    this.displayEditDialog.set(true);
  }

  protected hideEditDialog(): void {
    this.displayEditDialog.set(false);
    this.editingInstitution = null;
    this.editRole.set(null);
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
    if (this.selectedInstitution && this.selectedRole()) {
      const institutionWithRole: InstitutionShortInfo = {
        ...this.selectedInstitution,
        roleCode: this.selectedRole()!.roleCode,
      };
      this.institutionAdded.emit(institutionWithRole);
      this.hideAddDialog();
    }
  }

  protected confirmEditRole(): void {
    if (this.editingInstitution && this.editRole()) {
      this.roleChanged.emit({
        institutionId: this.editingInstitution.institutionId,
        roleCode: this.editRole()!.roleCode,
      });
      this.hideEditDialog();
    }
  }

  protected removeInstitution(institutionId: string): void {
    this.institutionRemoved.emit(institutionId);
  }
}
