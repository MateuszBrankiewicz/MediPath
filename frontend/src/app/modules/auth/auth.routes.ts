import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { PasswordReset } from './password-reset/password-reset';

export const AUTH_ROUTES: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', component: PasswordReset },
  { path: 'forgot-password/:token', component: PasswordReset },
];
