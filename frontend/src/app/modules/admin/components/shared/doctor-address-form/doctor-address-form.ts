import { CommonModule } from '@angular/common';
import { Component, inject, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-doctor-address-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    InputMaskModule,
  ],
  styleUrl: './doctor-address-form.scss',
  template: `
    <div class="form-section">
      <h3 class="section-title">
        {{
          translationService.translate(translationKey() + '.residentialAddress')
        }}
      </h3>

      <div class="form-grid" [formGroup]="formGroup()">
        <div class="form-field">
          <label for="province" class="field-label">
            <i class="pi pi-map"></i>
            {{ translationService.translate(translationKey() + '.province') }}
          </label>
          <input
            pInputText
            id="province"
            formControlName="province"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.province'
              )
            "
            [class.invalid]="isFieldInvalid()(formGroupName() + '.province')"
          />
          @if (isFieldInvalid()(formGroupName() + '.province')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()(formGroupName() + '.province') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="postalCode" class="field-label">
            <i class="pi pi-inbox"></i>
            {{ translationService.translate(translationKey() + '.postalCode') }}
          </label>
          <p-inputmask
            id="postalCode"
            formControlName="postalCode"
            mask="99-999"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.postalCode'
              )
            "
            [class.invalid]="isFieldInvalid()(formGroupName() + '.postalCode')"
          />
          @if (isFieldInvalid()(formGroupName() + '.postalCode')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()(formGroupName() + '.postalCode') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="city" class="field-label">
            <i class="pi pi-building"></i>
            {{ translationService.translate(translationKey() + '.city') }}
          </label>
          <input
            pInputText
            id="city"
            formControlName="city"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.city'
              )
            "
            [class.invalid]="isFieldInvalid()(formGroupName() + '.city')"
          />
          @if (isFieldInvalid()(formGroupName() + '.city')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()(formGroupName() + '.city') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="number" class="field-label">
            <i class="pi pi-hashtag"></i>
            {{ translationService.translate(translationKey() + '.number') }}
          </label>
          <input
            pInputText
            id="number"
            formControlName="number"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.number'
              )
            "
            [class.invalid]="isFieldInvalid()(formGroupName() + '.number')"
          />
          @if (isFieldInvalid()(formGroupName() + '.number')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()(formGroupName() + '.number') }}
            </small>
          }
        </div>

        <div class="form-field">
          <label for="street" class="field-label">
            <i class="pi pi-map-marker"></i>
            {{ translationService.translate(translationKey() + '.street') }}
          </label>
          <input
            pInputText
            id="street"
            formControlName="street"
            [placeholder]="
              translationService.translate(
                translationKey() + '.placeholders.street'
              )
            "
            [class.invalid]="isFieldInvalid()(formGroupName() + '.street')"
          />
          @if (isFieldInvalid()(formGroupName() + '.street')) {
            <small class="error-message">
              <i class="pi pi-exclamation-circle"></i>
              {{ getFieldError()(formGroupName() + '.street') }}
            </small>
          }
        </div>
      </div>
    </div>
  `,
})
export class DoctorAddressFormComponent {
  formGroup = input.required<FormGroup>();
  translationKey = input.required<string>();
  formGroupName = input<string>('residentialAddress');
  isFieldInvalid = input.required<(fieldPath: string) => boolean>();
  getFieldError = input.required<(fieldPath: string) => string>();

  protected translationService = inject(TranslationService);
}
