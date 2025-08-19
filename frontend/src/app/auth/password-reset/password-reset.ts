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
import { TranslationService } from '../../services/translation.service';
import { ImageForAuth } from '../../shared/image-for-auth/image-for-auth';
import { InputForAuth } from '../../shared/input-for-auth/input-for-auth';
import { ModalDialogComponent } from '../../shared/modal-dialog/modal-dialog';

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
  protected readonly isLoading = signal(false);
  protected readonly visible = signal(false);

  protected readonly forgotPasswordForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  protected onSubmit() {
    this.forgotPasswordForm.markAllAsTouched();
    this.forgotPasswordForm.updateValueAndValidity();
    if (this.forgotPasswordForm.valid) {
      // UI-only flow for now: show success dialog
      this.visible.set(true);
    }
  }

  protected redirectToLoginPage() {
    this.visible.set(false);
    this.router.navigate(['/auth/login']);
  }
}
