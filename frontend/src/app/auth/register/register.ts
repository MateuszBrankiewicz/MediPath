import { Component, signal, inject, computed } from '@angular/core';
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
import { Router } from '@angular/router';

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
    
],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {


  protected readonly citiesOptions = signal<SelectOption[]>([])  

  public translationService = inject(TranslationService);
  private readonly router = inject(Router);
  protected readonly isLoading = this.translationService.isLoading;
  private readonly authService = inject(AuthenticationService);

  protected readonly hasError = signal({
    haveError: false,
    errorMessage: '',
  });

  protected readonly passwordMismatchError = signal<boolean>(false);
  protected readonly checkBoxNotChecked = signal(false);
  public registerFormGroup = new FormGroup({
    name: new FormControl('', [Validators.required]),
    surname: new FormControl('', [Validators.required]),
    govID: new FormControl('', [Validators.required]),
    birthDate: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\d{2}-\d{2}-\d{4}$/)
    ]),
    province: new FormControl('', [Validators.required]),
    postalCode: new FormControl('', [
      Validators.required,
      Validators.pattern(/^\d{2}-\d{3}$/)
    ]),
    city: new FormControl('', [Validators.required]),
    number: new FormControl('', [Validators.required]),
    street: new FormControl(''), 
    phoneNumber: new FormControl('', [
      Validators.required,
      Validators.pattern(/^[0-9]{9}$/)
    ]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
    ]),
    confirmPassword: new FormControl('' ),
    termsChecked: new FormControl(false, [Validators.requiredTrue])
  });

  protected readonly date = signal<Date | null>(null);
  constructor(){
    this.authService.getCities('').subscribe(val => {
      this.citiesOptions.set(val as SelectOption[])
    })
  }
 protected readonly confirmPasswordHasError = computed(() => {
    return this.passwordMismatchError() || 
           (this.registerFormGroup.controls.confirmPassword.invalid && 
            (this.registerFormGroup.controls.confirmPassword.dirty || 
             this.registerFormGroup.controls.confirmPassword.touched));
  });
  
  onRegisterFormSubmit() {
    this.registerFormGroup.markAllAsTouched();
          this.passwordMismatchError.set(false);

    this.passwordMismatchError.set(false);
    this.hasError.set({ haveError: false, errorMessage: '' });

    const formValue = { ...this.registerFormGroup.value };
    if (formValue.password !== formValue.confirmPassword) {
      this.passwordMismatchError.set(true);
      return;
    }
    if(formValue.termsChecked === false){
      this.checkBoxNotChecked.set(true);
      return
    }
    if (this.registerFormGroup.valid) {
      delete formValue.confirmPassword;
      delete formValue.termsChecked;
      
      this.isLoading.set(true);

      this.authService
        .registerUser(formValue as RegisterUser)
        .subscribe({
          next: (res) => {
            this.router.navigate(['/auth/login']);
            this.isLoading.set(false);
          },
          error: (err) => {
            console.error(err);
            this.hasError.set({
              haveError: true,
              errorMessage: 'Wystąpił błąd podczas rejestracji. Spróbuj ponownie.',
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
  }
}
