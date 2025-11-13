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
import { ProfileFormControls } from '../../../../core/models/user-profile.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { UserProfileFormValue } from '../../../../core/services/authentication/profile.model';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { DialogService } from 'primeng/dynamicdialog';
import { ChangePasswordDialog } from './components/change-password-dialog/change-password-dialog';
import { FileUploadModule } from 'primeng/fileupload';

@Component({
  selector: 'app-edit-user-profile',
  imports: [CommonModule, ReactiveFormsModule, FileUploadModule],
  templateUrl: './edit-user-profile.html',
  styleUrl: './edit-user-profile.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditUserProfile implements OnInit {
  public translationService = inject(TranslationService);
  private toastService = inject(ToastService);
  private authService = inject(AuthenticationService);
  private destroyRef = inject(DestroyRef);
  public isSubmitting = signal(false);
  private dialogService = inject(DialogService);
  private profilePicture = signal('');
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
      const formValue = {
        ...(this.profileFormGroup.getRawValue() as UserProfileFormValue),
        pfpImage: this.profilePicture(),
      };
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
    this.dialogService.open(ChangePasswordDialog, {
      width: '50%',
      height: '50%',
      header: 'Change password',
      modal: true,
      closable: true,
    });
  }

  protected onImageUpload(event: { files: File[] }): void {
    const file = event.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.profilePicture.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  private markFormGroupTouched() {
    Object.values(this.profileFormGroup.controls).forEach((control) => {
      control.markAsTouched({ onlySelf: true });
    });
  }
}
