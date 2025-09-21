import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { ToastService } from '../../../../core/services/toast/toast.service';

export interface UserProfile {
  name: string;
  surname: string;
  email: string;
  birthDate: string;
  phoneNumber: string;
  governmentId: string;
  province: string;
  postalCode: string;
  city: string;
  number: string;
  street: string;
}

@Component({
  selector: 'app-edit-user-profile',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-user-profile.html',
  styleUrl: './edit-user-profile.scss',
})
export class EditUserProfile implements OnInit {
  public translationService = inject(TranslationService);
  private toastService = inject(ToastService);

  public isSubmitting = signal(false);

  // Form controls with mock data - in real app this would come from a service
  public profileFormGroup = new FormGroup({
    name: new FormControl('Jan', [
      Validators.required,
      Validators.minLength(2),
    ]),
    surname: new FormControl('Kowalski', [
      Validators.required,
      Validators.minLength(2),
    ]),
    email: new FormControl('jankowalski@gmail.com', [
      Validators.required,
      Validators.email,
    ]),
    birthDate: new FormControl('08-04-1984', [Validators.required]),
    phoneNumber: new FormControl('+48 999 999 999', [Validators.required]),
    governmentId: new FormControl('84123456789'),
    province: new FormControl('lubelskie', [Validators.required]),
    postalCode: new FormControl('20-950', [Validators.required]),
    city: new FormControl('Lublin', [Validators.required]),
    number: new FormControl('99', [Validators.required]),
    street: new FormControl('Nadbystrzycka', [Validators.required]),
  });

  ngOnInit() {
    this.profileFormGroup.controls.governmentId.disable();
    console.log('EditUserProfile initialized');
  }

  public onSubmit() {
    if (this.profileFormGroup.valid) {
      this.isSubmitting.set(true);

      // Simulate API call
      setTimeout(() => {
        this.toastService.showSuccess(
          this.translationService.translate('editProfile.success'),
        );
        this.isSubmitting.set(false);
      }, 1000);
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
    Object.keys(this.profileFormGroup.controls).forEach((field) => {
      const control = this.profileFormGroup.get(field);
      control?.markAsTouched({ onlySelf: true });
    });
  }
}
