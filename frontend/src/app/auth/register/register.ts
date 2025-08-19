import { Component, computed, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { map } from 'rxjs';
import { TranslationService } from '../../services/translation.service';
import { InputForAuth } from '../../shared/input-for-auth/input-for-auth';
import { LanguageSwitcher } from '../../shared/language-switcher/language-switcher';
import { ModalDialogComponent } from '../../shared/modal-dialog/modal-dialog';
import {
  SelectOption,
  SelectWithSearch,
} from '../../shared/select-with-search/select-with-search';
import { RegisterUser } from '../auth.constants';
import { AuthenticationService } from '../services/authentication/authentication';
import { ImageForAuth } from "../../shared/image-for-auth/image-for-auth";

@Component({
  selector: 'app-register',
  imports: [
    CardModule,
    InputForAuth,
    ReactiveFormsModule,
    ButtonModule,
    DatePickerModule,
    FormsModule,
    LanguageSwitcher,
    ProgressSpinnerModule,
    SelectWithSearch,
    ModalDialogComponent,
    DialogModule,
    ImageForAuth
],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  protected readonly citiesOptions = signal<SelectOption[]>([]);
  protected readonly proviceOptions = signal<SelectOption[]>([]);

  public translationService = inject(TranslationService);
  private readonly router = inject(Router);
  protected readonly isLoading = this.translationService.isLoading;
  private readonly authService = inject(AuthenticationService);

  protected readonly hasError = signal({
    haveError: false,
    errorMessage: '',
  });
  protected readonly visible = signal(false);

  protected readonly passwordMismatchError = signal<boolean>(false);
  protected readonly checkBoxNotChecked = signal<boolean>(false);

  protected readonly confirmPasswordHasError = computed(() => {
    return (
      this.passwordMismatchError() ||
      (this.registerFormGroup.controls.confirmPassword.invalid &&
        (this.registerFormGroup.controls.confirmPassword.dirty ||
          this.registerFormGroup.controls.confirmPassword.touched))
    );
  });

  protected readonly checkboxHasError = computed(() => {
    return (
      this.checkBoxNotChecked() ||
      (this.registerFormGroup.controls.termsChecked.invalid &&
        (this.registerFormGroup.controls.termsChecked.dirty ||
          this.registerFormGroup.controls.termsChecked.touched))
    );
  });
  public registerFormGroup = new FormGroup({
    name: new FormControl('', [Validators.required]),
    surname: new FormControl('', [Validators.required]),
    govID: new FormControl('', [
      Validators.required,
      Validators.pattern(/^[0-9]{11}$/),
    ]),
    birthDate: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\d{2}-\d{2}-\d{4}$/),
    ]),
    province: new FormControl('', [Validators.required]),
    postalCode: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\d{2}-\d{3}$/),
    ]),
    city: new FormControl('', [Validators.required]),
    number: new FormControl('', [Validators.required]),
    street: new FormControl(''),
    phoneNumber: new FormControl('', [
      Validators.required,
      Validators.pattern(/^[0-9]{9}$/),
    ]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
    confirmPassword: new FormControl(''),
    termsChecked: new FormControl(false, [Validators.requiredTrue]),
  });

  constructor() {
    this.authService.getCities('').subscribe((val) => {
      this.citiesOptions.set(val as SelectOption[]);
    });
    this.authService
      .getProvinces()
      .pipe(
        map((provinceArray) => {
          return provinceArray.map((provinceValue) => {
            return { name: provinceValue };
          });
        }),
      )
      .subscribe((provinceValue) => {
        this.proviceOptions.set(provinceValue);
      });
  }

  onRegisterFormSubmit() {
    this.registerFormGroup.markAllAsTouched();

    this.passwordMismatchError.set(false);
    this.checkBoxNotChecked.set(false);
    this.hasError.set({ haveError: false, errorMessage: '' });

    const formValue = { ...this.registerFormGroup.value };

    if (formValue.password !== formValue.confirmPassword) {
      this.passwordMismatchError.set(true);
      return;
    }

    if (formValue.termsChecked === false) {
      this.checkBoxNotChecked.set(true);
      return;
    }

    if (this.registerFormGroup.valid) {
      delete formValue.confirmPassword;
      delete formValue.termsChecked;

      this.isLoading.set(true);

      this.authService.registerUser(formValue as RegisterUser).subscribe({
        next: () => {
          this.visible.set(true);
          this.isLoading.set(false);
        },
        error: (err) => {
          let errorMessage = 'register.error.backendError';

          if (err.status === 409) {
            errorMessage = 'register.error.userExists';
          }

          this.hasError.set({
            haveError: true,
            errorMessage: this.translationService.translate(errorMessage),
          });
          this.isLoading.set(false);
        },
        complete: () => {
          this.isLoading.set(false);
        },
      });
    }
  }
  public loadCities = (searchTerm: string) => {
    return this.authService.getCities(searchTerm);
  };
  protected redirectToLoginPage() {
    this.visible.set(false);
    this.router.navigate(['/auth/login']);
  }
}
