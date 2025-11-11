import { Component, DestroyRef, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogRef } from 'primeng/dynamicdialog';
import { PasswordModule } from 'primeng/password';
import { MessageModule } from 'primeng/message';
import { AuthenticationService } from '../../../../../../core/services/authentication/authentication';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { ToastService } from '../../../../../../core/services/toast/toast.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-change-password-dialog',
  imports: [
    ReactiveFormsModule,
    PasswordModule,
    ButtonModule,
    MessageModule,
    ProgressSpinnerModule,
  ],
  templateUrl: './change-password-dialog.html',
  styleUrl: './change-password-dialog.scss',
})
export class ChangePasswordDialog {
  private toastService = inject(ToastService);
  private destroyRef = inject(DestroyRef);
  private ref = inject(DynamicDialogRef<ChangePasswordDialog | undefined>);
  private authService = inject(AuthenticationService);
  protected isLoading = signal(false);
  protected changePasswordForm = new FormGroup({
    currentPassword: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(32),
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(8),
      Validators.maxLength(32),
    ]),
    repeatPassword: new FormControl('', [
      Validators.required,
      this.matchPasswordValidator.bind(this),
    ]),
  });

  protected onAccept() {
    this.changePasswordForm.markAllAsTouched();
    if (!this.changePasswordForm.valid) {
      return;
    }
    this.isLoading.set(true);
    this.authService
      .changePassword(
        this.changePasswordForm.value.currentPassword!,
        this.changePasswordForm.value.password!,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.toastService.showSuccess('Password changed succesfully');
          this.ref.close();
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading.set(false);
          if (err.status === 401) {
            this.toastService.showError('Current password is invalid');
          }
        },
      });
  }

  protected onClose() {
    this.ref.close();
  }

  private matchPasswordValidator(
    control: AbstractControl,
  ): ValidationErrors | null {
    const password = this.changePasswordForm?.get('password')?.value;
    return control.value === password ? null : { passwordMismatch: true };
  }
}
