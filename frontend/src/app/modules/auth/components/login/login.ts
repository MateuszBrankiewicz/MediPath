import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InputForAuth } from '../../../shared/components/forms/input-for-auth/input-for-auth';
import { ImageForAuth } from '../../../shared/components/ui/image-for-auth/image-for-auth';
import { ModalDialogComponent } from '../../../shared/components/ui/modal-dialog/modal-dialog';

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
  private readonly toastService = inject(ToastService);
  protected readonly isLoading = signal(false);

  private destroyRef = inject(DestroyRef);

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

      this.isLoading.set(true);

      this.authService
        .login(email, password)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.isLoading.set(false);
            this.toastService.showSuccess('toast.login.success');
            const dashboardRoute = this.authService.getDashboardRoute();
            this.router.navigate([dashboardRoute]);
          },
          error: (error) => {
            this.isLoading.set(false);
            console.error('Login failed', error);
            this.toastService.showError('login.error');
            this.hasError.set({
              haveError: true,
              errorMessage: this.translationService.translate('login.error'),
            });
          },
        });
    }
  }

  protected redirectToHomePage() {
    const dashboardRoute = this.authService.getDashboardRoute();
    this.router.navigate([dashboardRoute]);
    this.visible.set(false);
  }
}
