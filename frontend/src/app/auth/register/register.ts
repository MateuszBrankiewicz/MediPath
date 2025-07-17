import { Component, signal, inject } from '@angular/core';
import { CardModule } from 'primeng/card';
import { InputForAuth } from "../../shared/input-for-auth/input-for-auth";
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import {  DatePickerModule } from 'primeng/datepicker';
import { LanguageSwitcher } from '../../shared/language-switcher/language-switcher';
import { TranslationService } from '../../services/translation.service';


@Component({
  selector: 'app-register',
  imports: [CardModule, InputForAuth,ReactiveFormsModule,ButtonModule, DatePickerModule,FormsModule, LanguageSwitcher],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {
selectDate() {
throw new Error('Method not implemented.');
}

  public translationService = inject(TranslationService);

  protected readonly hasError = signal({
    haveError: false,
    errorMessage: ""
  });

  public registerFormGroup = new FormGroup({
    name : new FormControl('',Validators.required),
    surname: new FormControl('',[Validators.required]),
    govermentId: new FormControl('',[Validators.required]),
    birhDate: new FormControl<Date | null>(null),
    province : new FormControl(''),
    postalCode : new FormControl('', [Validators.pattern(/^\d{2}-\d{3}$/)]),
    city: new FormControl(''),
    number: new FormControl(''),
    street: new FormControl(''),
    phoneNumber: new FormControl('', [Validators.pattern(/^[0-9]{9}$/)]),
    email: new FormControl('', [Validators.required,Validators.email]),
    password: new FormControl('',[Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required])   

  })

  protected readonly date = signal<Date | null>(null);


  onRegisterFormSubmit() {

    this.registerFormGroup.markAllAsTouched();
 
    
    if (this.registerFormGroup.valid) {
      console.log('Form is valid:', this.registerFormGroup.value);
      
      //TODO backend connect implementation

    } else {
      console.log('Form is invalid');
      this.hasError.set({
        haveError: true,
        errorMessage: this.translationService.translate('form.invalid')
      });
      
    }
  }
}
