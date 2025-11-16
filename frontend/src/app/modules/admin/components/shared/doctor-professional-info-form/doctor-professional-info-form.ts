import { CommonModule } from '@angular/common';
import { Component, inject, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectChangeEvent, MultiSelectModule } from 'primeng/multiselect';
import { SelectChangeEvent, SelectModule } from 'primeng/select';
import { Specialisation } from '../../../../../core/models/specialisation.model';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

interface RoleOption {
  id: number;
  name: string;
  roleCode: number;
}

@Component({
  selector: 'app-doctor-professional-info-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    MultiSelectModule,
    SelectModule,
  ],
  styleUrl: './doctor-professional-info-form.scss',
  template: `
    <div class="form-section">
      <h3 class="section-title">
        {{
          translationService.translate(translationKey() + '.professionalInfo')
        }}
      </h3>

      <div class="form-grid-professional" [formGroup]="formGroup()">
        @if (showRoleSelect()) {
          <div class="form-field">
            <label for="role" class="field-label">
              <i class="pi pi-address-book"></i>
              {{ translationService.translate(translationKey() + '.role') }}
            </label>
            <p-multiselect
              id="role"
              [options]="roleOptions()"
              formControlName="roleCode"
              (onChange)="handleRoleChange($event)"
              optionLabel="name"
              optionValue="roleCode"
            >
            </p-multiselect>
          </div>
        }

        @if (showSpecialisations()) {
          <div class="form-field">
            <label for="specialisation" class="field-label">
              <i class="pi pi-briefcase"></i>
              {{
                translationService.translate(
                  translationKey() + '.specialisation'
                )
              }}
            </label>
            <p-multiselect
              inputId="specialisation"
              formControlName="specialisation"
              [options]="specialisations()"
              optionLabel="name"
              optionValue="name"
              [placeholder]="
                translationService.translate(
                  translationKey() + '.placeholders.specialisation'
                )
              "
              display="chip"
              [showToggleAll]="false"
              [class.invalid]="isFieldInvalid()('specialisation')"
            />
            @if (isFieldInvalid()('specialisation')) {
              <small class="error-message">
                <i class="pi pi-exclamation-circle"></i>
                {{ getFieldError()('specialisation') }}
              </small>
            }
          </div>
        }
        <div class="form-field">
          <label for="pwzNumber" class="field-label">
            <i class="pi pi-shield"></i>
            {{ translationService.translate(translationKey() + '.pwzNumber') }}
          </label>
          <input
            pInputText
            id="pwzNumber"
            formControlName="pwzNumber"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.pwzNumber'
              )
            "
            maxlength="7"
            [class.invalid]="isFieldInvalid()('pwzNumber')"
          />
          @if (isFieldInvalid()('pwzNumber')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('pwzNumber') }}
            </small>
          }
        </div>
      </div>
    </div>
  `,
})
export class DoctorProfessionalInfoFormComponent {
  formGroup = input.required<FormGroup>();
  translationKey = input.required<string>();
  specialisations = input.required<Specialisation[]>();
  roleOptions = input<RoleOption[]>([]);
  showRoleSelect = input<boolean>(false);
  isFieldInvalid = input.required<(fieldPath: string) => boolean>();
  getFieldError = input.required<(fieldPath: string) => string>();
  public showSpecialisations = input<boolean>(true);
  roleChanged = output<SelectChangeEvent>();

  protected translationService = inject(TranslationService);

  protected handleRoleChange(event: MultiSelectChangeEvent): void {
    this.roleChanged.emit(event);
  }
}
