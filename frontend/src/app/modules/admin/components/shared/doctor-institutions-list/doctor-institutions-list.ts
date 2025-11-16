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
import { MultiSelectChangeEvent, MultiSelectModule } from 'primeng/multiselect';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { EditedEmployee } from '../../../../../core/models/doctor.model';
import { InstitutionShortInfo } from '../../../../../core/models/institution.model';
import { Specialisation } from '../../../../../core/models/specialisation.model';
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
    MultiSelectModule,
  ],
  templateUrl: './doctor-institutions-list.html',
  styleUrl: './doctor-institutions-list.scss',
})
export class DoctorInstitutionsListComponent {
  institutions = input.required<EditedEmployee[]>();
  availableInstitutions = input<InstitutionShortInfo[]>([]);
  translationKey = input.required<string>();

  institutionAdded = output<EditedEmployee>();
  institutionRemoved = output<string>();
  roleChanged = output<EditedEmployee>();

  public specialisations = input.required<Specialisation[]>();
  protected translationService = inject(TranslationService);
  protected selectedSpecialistaions = signal<Specialisation[]>([]);
  protected roleOptions: RoleOption[] = [
    { id: 1, name: 'roles.doctor', roleCode: 2 },
    { id: 2, name: 'roles.staff', roleCode: 4 },
    { id: 3, name: 'roles.admin', roleCode: 8 },
  ];

  protected shouldEnableSpecialisationsEdit = signal(false);
  protected selectedSpecialisationsEdit = signal<Specialisation[]>([]);
  protected displayAddDialog = signal(false);
  protected displayEditDialog = signal(false);
  protected selectedInstitution: InstitutionShortInfo | null = null;
  protected selectedRole = signal<RoleOption[] | null>(null);
  protected editingInstitution: InstitutionShortInfo | null = null;
  protected editRole = signal<RoleOption[] | null>(null);
  protected filteredInstitutions = signal<InstitutionShortInfo[]>([]);
  protected shouldEnableSpecialisations = signal(false);
  protected showAddDialog(): void {
    this.displayAddDialog.set(true);
    this.selectedInstitution = null;
    this.selectedRole.set([this.roleOptions[0]]);
  }

  protected hideAddDialog(): void {
    this.displayAddDialog.set(false);
    this.selectedInstitution = null;
    this.selectedRole.set(null);
  }

  protected showEditDialog(institution: EditedEmployee): void {
    this.editingInstitution = {
      institutionId: institution.institutionId,
      institutionName: institution.institutionName,
    };
    const institutionRoleCode = institution.roleCode;
    if (institutionRoleCode === undefined) {
      this.editRole.set([]);
    } else {
      const activeRoles = this.roleOptions.filter((roleOption) => {
        return (
          (institutionRoleCode & roleOption.roleCode) === roleOption.roleCode
        );
      });

      this.editRole.set(activeRoles);
    }
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
      const institutionWithRole: EditedEmployee = {
        institutionId: this.selectedInstitution.institutionId,
        institutionName: this.selectedInstitution.institutionName,
        roleCode:
          this.selectedRole()?.reduce((acc, role) => acc + role.roleCode, 0) ||
          0,
        specialisations: this.selectedSpecialistaions().map(
          (spec) => spec.name,
        ),
      };
      this.institutionAdded.emit(institutionWithRole);
      this.hideAddDialog();
    }
  }

  protected confirmEditRole(): void {
    if (this.editingInstitution && this.editRole()) {
      this.roleChanged.emit({
        institutionId: this.editingInstitution.institutionId,
        institutionName: this.editingInstitution.institutionName,
        roleCode:
          this.editRole()?.reduce((acc, role) => acc + role.roleCode, 0) || 0,
        specialisations: this.selectedSpecialisationsEdit().map(
          (spec) => spec.name,
        ),
      });
      this.hideEditDialog();
    }
  }

  protected removeInstitution(institutionId: string): void {
    this.institutionRemoved.emit(institutionId);
  }

  protected onRoleChange(event: MultiSelectChangeEvent) {
    if (event.value.some((role: RoleOption) => role.roleCode === 2)) {
      this.shouldEnableSpecialisations.set(false);
    } else {
      this.shouldEnableSpecialisations.set(true);
      this.selectedSpecialistaions.set([]);
    }
  }

  protected onEditRoleChange(event: MultiSelectChangeEvent) {
    if (event.value.some((role: RoleOption) => role.roleCode === 2)) {
      this.shouldEnableSpecialisationsEdit.set(false);
    } else {
      this.shouldEnableSpecialisationsEdit.set(true);
      this.selectedSpecialisationsEdit.set([]);
    }
  }

  protected getRolesDisplay(roleCode: number | undefined): string {
    if (roleCode === undefined) {
      return '';
    }

    const activeRoles = this.roleOptions.filter((roleOption) => {
      return (roleCode & roleOption.roleCode) === roleOption.roleCode;
    });

    return activeRoles
      .map((role) => this.translationService.translate(role.name))
      .join(', ');
  }

  protected getSelectedRolesDisplay(roles: RoleOption[]): string {
    if (!roles || roles.length === 0) {
      return '';
    }
    return roles
      .map((role) => this.translationService.translate(role.name))
      .join(', ');
  }
}
