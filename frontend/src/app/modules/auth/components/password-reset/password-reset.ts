import { Component, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Button } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { InputForAuth } from '../../../shared/components/forms/input-for-auth/input-for-auth';
import { ModalDialogComponent } from '../../../shared/components/ui/modal-dialog/modal-dialog';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { ImageForAuth } from '../../../shared/components/ui/image-for-auth/image-for-auth';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';

@Component({
  selector: 'app-password-reset',
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
  templateUrl: './password-reset.html',
  styleUrl: './password-reset.scss',
})
export class PasswordReset {
  protected readonly translationService = inject(TranslationService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  protected readonly isLoading = signal(false);
  protected readonly visible = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isTokenExpired = signal(false);
  private readonly authenticationService = inject(AuthenticationService);
  protected readonly token = signal<string | null>(null);
  protected readonly forgotPasswordForm = new FormGroup({
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
    ]),
    confirmPassword: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
    ]),
  });
  protected readonly emailSentForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  protected readonly passwordSended = signal(false);

  constructor() {
    this.activatedRoute.params.subscribe((params) => {
      const token = params['token'];
      this.token.set(token || null);
    });
  }

  protected emailSubmit() {
    this.emailSentForm.markAllAsTouched();
    this.emailSentForm.updateValueAndValidity();
    if (this.emailSentForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);

      if (!this.emailSentForm.value.email) {
        this.isLoading.set(false);
        return;
      }

      this.authenticationService
        .resetPassword(this.emailSentForm.value.email)
        .subscribe({
          next: () => {
            this.isLoading.set(false);
            this.visible.set(true);
          },
          error: (error) => {
            this.isLoading.set(false);
            this.errorMessage.set(error.error?.message || 'An error occurred');
          },
        });
    }
  }

  passwordReset() {
    this.forgotPasswordForm.markAllAsTouched();
    this.forgotPasswordForm.updateValueAndValidity();

    if (this.forgotPasswordForm.valid) {
      this.isLoading.set(true);
      this.errorMessage.set(null);
      this.isTokenExpired.set(false);
      console.log({
        password: this.forgotPasswordForm.value.password ?? '',
        token: this.token() ?? '',
      });
      this.passwordSended.set(true);
      this.authenticationService
        .sentPasswordWithToken({
          password: this.forgotPasswordForm.value.password ?? '',
          token: this.token() ?? '',
        })
        .subscribe({
          next: () => {
            this.isLoading.set(false);
            this.visible.set(true);
          },
          error: (error) => {
            this.isLoading.set(false);

            console.error(error);

            if (
              error.status === 410 &&
              error.error?.message === 'token invalid or expired'
            ) {
              this.isTokenExpired.set(true);
              this.errorMessage.set('passwordReset.error.tokenExpired');
            } else {
              this.errorMessage.set(
                error.error?.message || 'passwordReset.error.generic',
              );
            }
          },
        });
    }
  }

  protected redirectToLoginPage() {
    this.visible.set(false);
    this.passwordSended.set(false);
    this.router.navigate(['/auth/login']);
  }

  protected redirectToForgotPassword() {
    this.router.navigate(['/auth/forgot-password']);
  }
}
