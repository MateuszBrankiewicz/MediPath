import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { UserProfileFormValue } from '../../../../core/services/authentication/profile.model';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

interface ProfileFormControls {
  name: FormControl<string>;
  surname: FormControl<string>;
  birthDate: FormControl<string>;
  phoneNumber: FormControl<string>;
  governmentId: FormControl<string>;
  province: FormControl<string>;
  postalCode: FormControl<string>;
  city: FormControl<string>;
  number: FormControl<string>;
  street: FormControl<string>;
}

@Component({
  selector: 'app-edit-user-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-user-profile.html',
  styleUrl: './edit-user-profile.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditUserProfile implements OnInit {
  public translationService = inject(TranslationService);
  private toastService = inject(ToastService);
  private authService = inject(AuthenticationService);
  private destroyRef = inject(DestroyRef);
  public isSubmitting = signal(false);

  public profileFormGroup = new FormGroup<ProfileFormControls>({
    name: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2)],
    }),
    surname: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2)],
    }),

    birthDate: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    phoneNumber: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    governmentId: new FormControl<string>('', { nonNullable: true }),
    province: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    postalCode: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    city: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    number: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    street: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit() {
    this.profileFormGroup.controls.governmentId.disable({ emitEvent: false });

    this.authService
      .getUserProfile()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (profile) => {
          this.profileFormGroup.patchValue(profile, {
            emitEvent: false,
            onlySelf: true,
          });
          this.profileFormGroup.markAsPristine();
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate('editProfile.error.load'),
          );
        },
      });
  }

  public onSubmit() {
    if (this.profileFormGroup.valid) {
      this.isSubmitting.set(true);
      const formValue =
        this.profileFormGroup.getRawValue() as UserProfileFormValue;

      this.authService
        .updateUserProfile(formValue)
        .pipe(
          takeUntilDestroyed(this.destroyRef),
          finalize(() => this.isSubmitting.set(false)),
        )
        .subscribe({
          next: () => {
            this.toastService.showSuccess('editProfile.success');
            this.profileFormGroup.markAsPristine();
          },
          error: () => {
            this.toastService.showError(
              this.translationService.translate('editProfile.error.save'),
            );
          },
        });
    } else {
      this.markFormGroupTouched();
      this.toastService.showError(
        this.translationService.translate('editProfile.error.validation'),
      );
    }
  }

  public onChangePassword() {
    // Navigate to change password component or open dialog
    console.log('Change password clicked');
  }

  private markFormGroupTouched() {
    Object.values(this.profileFormGroup.controls).forEach((control) => {
      control.markAsTouched({ onlySelf: true });
    });
  }
}
