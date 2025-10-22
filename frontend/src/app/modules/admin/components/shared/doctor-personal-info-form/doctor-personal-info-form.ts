import { CommonModule } from '@angular/common';
import { Component, inject, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-doctor-personal-info-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    InputMaskModule,
    DatePickerModule,
  ],
  styleUrl: './doctor-personal-info-form.scss',
  template: `
    <div class="form-section">
      <h3 class="section-title">
        {{ translationService.translate(translationKey() + '.personalInfo') }}
      </h3>

      <div class="form-grid" [formGroup]="formGroup()">
        <div class="form-field">
          <label for="name" class="field-label">
            <i class="pi pi-user"></i>
            {{ translationService.translate(translationKey() + '.name') }}
          </label>
          <input
            pInputText
            id="name"
            formControlName="name"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.name'
              )
            "
            [class.invalid]="isFieldInvalid()('name')"
          />
          @if (isFieldInvalid()('name')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('name') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="surname" class="field-label">
            <i class="pi pi-user"></i>
            {{ translationService.translate(translationKey() + '.surname') }}
          </label>
          <input
            pInputText
            id="surname"
            formControlName="surname"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.surname'
              )
            "
            [class.invalid]="isFieldInvalid()('surname')"
          />
          @if (isFieldInvalid()('surname')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('surname') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="email" class="field-label">
            <i class="pi pi-envelope"></i>
            {{ translationService.translate(translationKey() + '.email') }}
          </label>
          <input
            pInputText
            id="email"
            type="email"
            formControlName="email"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.email'
              )
            "
            [class.invalid]="isFieldInvalid()('email')"
          />
          @if (isFieldInvalid()('email')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('email') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="birthDate" class="field-label">
            <i class="pi pi-calendar"></i>
            {{ translationService.translate(translationKey() + '.birthDate') }}
          </label>
          <p-datepicker
            inputId="birthDate"
            formControlName="birthDate"
            dateFormat="dd-mm-yy"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.birthDate'
              )
            "
            [showIcon]="true"
            iconDisplay="input"
            [class.invalid]="isFieldInvalid()('birthDate')"
          />
          @if (isFieldInvalid()('birthDate')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('birthDate') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="phoneNumber" class="field-label">
            <i class="pi pi-phone"></i>
            {{
              translationService.translate(translationKey() + '.phoneNumber')
            }}
          </label>
          <p-inputmask
            id="phoneNumber"
            formControlName="phoneNumber"
            mask="+99 999 999 999"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.phoneNumber'
              )
            "
            [class.invalid]="isFieldInvalid()('phoneNumber')"
          />
          @if (isFieldInvalid()('phoneNumber')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('phoneNumber') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="personalId" class="field-label">
            <i class="pi pi-id-card"></i>
            {{ translationService.translate(translationKey() + '.personalId') }}
          </label>
          <input
            pInputText
            id="personalId"
            formControlName="personalId"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.personalId'
              )
            "
            maxlength="11"
            [class.invalid]="isFieldInvalid()('personalId')"
          />
          @if (isFieldInvalid()('personalId')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()('personalId') }}
            </small>
          }
        </div>
      </div>
    </div>
  `,
})
export class DoctorPersonalInfoFormComponent {
  formGroup = input.required<FormGroup>();
  translationKey = input.required<string>();
  isFieldInvalid = input.required<(fieldPath: string) => boolean>();
  getFieldError = input.required<(fieldPath: string) => string>();

  protected translationService = inject(TranslationService);
}
