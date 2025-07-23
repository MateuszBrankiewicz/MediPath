import { Component, signal, inject } from '@angular/core';
import { CardModule } from 'primeng/card';
import { InputForAuth } from '../../shared/input-for-auth/input-for-auth';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { LanguageSwitcher } from '../../shared/language-switcher/language-switcher';
import { TranslationService } from '../../services/translation.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AuthenticationService } from '../services/authentication/authentication';
import { SelectOption, SelectWithSearch } from "../../shared/select-with-search/select-with-search";

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
    SelectWithSearch
],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  selectDate() {
    throw new Error('Method not implemented.');
  }

  protected readonly citiesOptions = signal<SelectOption[]>([])  

  public translationService = inject(TranslationService);

  protected readonly isLoading = this.translationService.isLoading;
  private readonly authService = inject(AuthenticationService);

  protected readonly hasError = signal({
    haveError: false,
    errorMessage: 'asdzxc',
  });

  public registerFormGroup = new FormGroup({
    name: new FormControl('', Validators.required),
    surname: new FormControl('', [Validators.required]),
    govID: new FormControl('', [Validators.required]),
    birthDate: new FormControl<Date | null>(null),
    province: new FormControl(''),
    postalCode: new FormControl('', [Validators.pattern(/^\d{2}-\d{3}$/)]),
    city: new FormControl(''),
    number: new FormControl(''),
    street: new FormControl(''),
    phoneNumber: new FormControl('', [Validators.pattern(/^[0-9]{9}$/)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
    confirmPassword: new FormControl('', [Validators.required]),
  });

  protected readonly date = signal<Date | null>(null);
  constructor(){
    this.authService.getCities('').subscribe(val => {
      console.log(val);
      this.citiesOptions.set(val as SelectOption[])
    })
  }

  
  onRegisterFormSubmit() {
    this.registerFormGroup.markAllAsTouched();

    if (this.registerFormGroup.valid) {
      console.log('Form is valid:', this.registerFormGroup.value);
      this.isLoading.set(true);
      const formValue = { ...this.registerFormGroup.value };
      if (formValue.password !== formValue.password) {
        this.hasError.set({
          haveError: true,
          errorMessage: this.translationService.translate(
            'form.passwordNotMatch'
          ),
        });
        return;
      }
      delete formValue.confirmPassword
      this.authService
        .registerUser(formValue as RegisterUser)
        .subscribe({
          next: (res) => {
            console.log(res);
            this.isLoading.set(false);
            console.log(res);
          },
          error: (err) => {
            this.hasError.set({
              haveError: true,
              errorMessage: 'Nieoczekiwany blad',
            });
            this.isLoading.set(false);
          },
          complete: () => {
            this.isLoading.set(false);
          },
        });
    } else {
      console.log('Form is invalid');
      this.hasError.set({
        haveError: true,
        errorMessage: this.translationService.translate('form.invalid'),
      });
    }
  }
  public loadCities = (searchTerm: string) => {
    return this.authService.getCities(searchTerm);
  }
}
