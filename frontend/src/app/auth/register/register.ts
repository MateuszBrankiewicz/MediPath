import { Component } from '@angular/core';
import { CardModule } from 'primeng/card';
import { InputForAuth } from "../../shared/input-for-auth/input-for-auth";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'app-register',
  imports: [CardModule, InputForAuth,ReactiveFormsModule,ButtonModule,NgOptimizedImage],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {


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

  get name() { return this.registerFormGroup.get('name') as FormControl; }
get surname() { return this.registerFormGroup.get('surname') as FormControl; }
get govermentId() { return this.registerFormGroup.get('govermentId') as FormControl; }
get birhDate() { return this.registerFormGroup.get('birhDate') as FormControl; }
get province() { return this.registerFormGroup.get('province') as FormControl; }
get postalCode() { return this.registerFormGroup.get('postalCode') as FormControl; }
get city() { return this.registerFormGroup.get('city') as FormControl; }
get number() { return this.registerFormGroup.get('number') as FormControl; }
get street() { return this.registerFormGroup.get('street') as FormControl; }
get phoneNumber() { return this.registerFormGroup.get('phoneNumber') as FormControl; }
get email() { return this.registerFormGroup.get('email') as FormControl; }
get password() { return this.registerFormGroup.get('password') as FormControl; }
get confirmPassword() { return this.registerFormGroup.get('confirmPassword') as FormControl; }


  onRegisterFormSubmit() {
    console.log(this.registerFormGroup.value)
}
}
