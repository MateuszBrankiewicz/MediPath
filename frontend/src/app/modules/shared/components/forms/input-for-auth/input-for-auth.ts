
import { Component, computed, inject, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TranslationService } from '../../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-input-for-auth',
  imports: [ReactiveFormsModule],
  templateUrl: './input-for-auth.html',
  styleUrl: './input-for-auth.scss'
})
export class InputForAuth {
  private translationService = inject(TranslationService);

  public hasCustomError = input<boolean>(false);
  public customErrorMessage = input<string>('');

  public readonly label = input("");
  public readonly labelKey = input("");

  public control = input.required<FormControl>();

  public readonly type = input<'text' | 'password' | 'email' | 'number'>('text');

  public readonly placeholder = input('');
  public readonly placeholderKey = input('');

  public readonly name = input('')


  getTranslatedLabel = computed(() => {
    const key = this.labelKey();
    const fallback = this.label();
    return key ? this.translationService.translate(key) : fallback;
  });

  getTranslatedPlaceholder = computed(() => {
    const key = this.placeholderKey();
    const fallback = this.placeholder();
    return key ? this.translationService.translate(key) : fallback;
  });

  getErrorMessage = computed(() => {
    if (this.hasCustomError() && this.customErrorMessage()) {
      return this.customErrorMessage();
    }
    const control = this.control();
    if (control && control.errors && (control.touched || control.dirty)) {
      if (control.errors['required']) {
        const field = this.getTranslatedLabel();
        return this.translationService.translate('validation.required', { field });
      }
      if (control.errors['email']) {
        return this.translationService.translate('validation.email');
      }
      if (control.errors['minlength']) {
        const length = control.errors['minlength'].requiredLength;
        return this.translationService.translate('validation.minLength', { length });
      }
      if (control.errors['pattern']) {
        const label = this.getTranslatedLabel().toLowerCase();
        if (label.includes('postal') || label.includes('pocztowy')) {
          return this.translationService.translate('validation.postalCodeFormat');
        }
        if (label.includes('phone') || label.includes('telefon')) {
          return this.translationService.translate('validation.phoneFormat');
        }
        if (label.includes('birth') || label.includes('urodzenia')) {
          return this.translationService.translate('validation.birthDateFormat');
        }
      }
    }
    return '';
  });

  get hasError(): boolean {
    if (this.hasCustomError()) {
      return true;
    }
    const control = this.control();
    return !!(control && control.errors && (control.touched || control.dirty));
  }

}
