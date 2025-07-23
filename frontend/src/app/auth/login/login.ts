import { Component } from '@angular/core';
import { InputForAuth } from "../../shared/input-for-auth/input-for-auth";
import { Button } from "primeng/button";
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Password } from 'primeng/password';


@Component({
  selector: 'app-login',
  imports: [InputForAuth, Button],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  protected loginFormGroup = new FormGroup({
    email: new FormControl('',Validators.required),
    Password: new FormControl('', Validators.required),
  })

}
