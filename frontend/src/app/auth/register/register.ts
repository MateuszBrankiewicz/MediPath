import { Component, signal } from '@angular/core';
import { CardModule } from 'primeng/card';
import { InputForAuth } from "../../shared/input-for-auth/input-for-auth";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';


@Component({
  selector: 'app-register',
  imports: [CardModule, InputForAuth,ReactiveFormsModule,ButtonModule],
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {

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


  onRegisterFormSubmit() {

    this.registerFormGroup.markAllAsTouched();
 
    
    if (this.registerFormGroup.valid) {
      console.log('Form is valid:', this.registerFormGroup.value);
      
      //TODO backend connect implementation

    } else {
      console.log('Form is invalid');
      this.hasError.set({
        haveError: true,
        errorMessage: "Form invalid"
      })
      
    }
  }
}
