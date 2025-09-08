import { Component, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Button } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InputForAuth } from '../../../shared/components/forms/input-for-auth/input-for-auth';
import { ModalDialogComponent } from '../../../shared/components/ui/modal-dialog/modal-dialog';
import { ImageForAuth } from '../../../shared/components/ui/image-for-auth/image-for-auth';
import { AuthenticationService } from '../../services/authentication/authentication';

@Component({
  selector: 'app-login',
  imports: [
    InputForAuth,
    Button,
    ModalDialogComponent,
    DialogModule,
    RouterModule,
    ProgressSpinnerModule,
    ReactiveFormsModule,
    ImageForAuth,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly authService = inject(AuthenticationService);
  private readonly router = inject(Router);
  protected readonly isLoading = signal(false);

  protected loginFormGroup = new FormGroup({
    email: new FormControl('', Validators.required),
    password: new FormControl('', Validators.required),
  });
  protected readonly translationService = inject(TranslationService);
  protected readonly hasError = signal({
    haveError: false,
    errorMessage: '',
  });
  protected readonly visible = signal(false);
  protected loginForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
    ]),
  });
  protected onSubmit() {
    this.loginFormGroup.markAllAsTouched();
    this.loginFormGroup.updateValueAndValidity();
    if (this.loginFormGroup.valid) {
      const { email, password } = this.loginFormGroup.value;
      if (!email || !password) {
        console.error('Email and password are required');
        return;
      }
      this.authService.login(email, password).subscribe({
        next: () => {
          this.visible.set(true);
        },
        error: (error) => {
          console.error('Login failed', error);
          this.hasError.set({
            haveError: true,
            errorMessage: this.translationService.translate('login.error'),
          });
        },
      });
    } else {
      console.error('Form is invalid');
    }
  }

  protected redirectToLoginPage() {
    this.visible.set(false);
  }
  protected redirectToHomePage() {
    this.router.navigate(['/']);
  }
}
