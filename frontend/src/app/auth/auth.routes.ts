import { Routes } from '@angular/router';
import { Login } from './login/login';
import { PasswordReset } from './password-reset/password-reset';
import { Register } from './register/register';

export const AUTH_ROUTES: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', component: PasswordReset },
  { path: 'forgot-password/:token', component: PasswordReset },
];
