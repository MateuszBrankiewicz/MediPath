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
import { TranslationService } from '../../services/translation.service';
import { ImageForAuth } from '../../shared/image-for-auth/image-for-auth';
import { InputForAuth } from '../../shared/input-for-auth/input-for-auth';
import { ModalDialogComponent } from '../../shared/modal-dialog/modal-dialog';
import { AuthenticationService } from '../services/authentication/authentication';

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
      this.visible.set(true);
      if (!this.emailSentForm.value.email) {
        this.visible.set(false);
        return;
      }
      this.authenticationService
        .resetPassword(this.emailSentForm.value.email)
        .subscribe({
          next: (response) => {
            console.log(response.message);
            console.log('tutaj');
            this.visible.set(false);
          },
          error: (error) => {
            console.error(error);
            this.visible.set(false);
          },
        });
    }
  }
  passwordReset() {
    this.forgotPasswordForm.markAllAsTouched();
    this.forgotPasswordForm.updateValueAndValidity();
    // if (this.forgotPasswordForm.valid) {
    //   this.visible.set(true);

    //   this.authenticationService
    //     .resetPassword(this.forgotPasswordForm.value.email)
    //     .subscribe({
    //       next: (response) => {
    //         console.log(response.message);
    //         console.log('tutaj');
    //         this.visible.set(false);
    //       },
    //       error: (error) => {
    //         console.error(error);
    //         this.visible.set(false);
    //       },
    //     });
    // }
  }
  protected redirectToLoginPage() {
    this.visible.set(false);
  }
}
