import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { PasswordReset } from './password-reset/password-reset';

export const AUTH_ROUTES: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', component: PasswordReset },
];
