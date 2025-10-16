import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DividerModule } from 'primeng/divider';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectModule } from 'primeng/select';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

interface Specialisation {
  code: string;
  name: string;
}

@Component({
  selector: 'app-add-doctors-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    CardModule,
    InputTextModule,
    MultiSelectModule,
    InputMaskModule,
    ButtonModule,
    FloatLabelModule,
    DatePickerModule,
    SelectModule,
    DividerModule,
  ],
  templateUrl: './add-doctors-page.html',
  styleUrl: './add-doctors-page.scss',
})
export class AddDoctorsPage implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);

  protected doctorForm!: FormGroup;
  protected isSubmitting = signal<boolean>(false);

  protected specialisations: Specialisation[] = [
    {
      code: 'cardiology',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.cardiology',
      ),
    },
    {
      code: 'cardio_surgery',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.cardio_surgery',
      ),
    },
    {
      code: 'dermatology',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.dermatology',
      ),
    },
    {
      code: 'neurology',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.neurology',
      ),
    },
    {
      code: 'orthopedics',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.orthopedics',
      ),
    },
    {
      code: 'pediatrics',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.pediatrics',
      ),
    },
    {
      code: 'psychiatry',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.psychiatry',
      ),
    },
    {
      code: 'general',
      name: this.translationService.translate(
        'admin.addDoctor.specialisations.general',
      ),
    },
  ];

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.doctorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      surname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      birthDate: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      personalId: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      specialisation: [[], Validators.required],
      pwzNumber: ['', [Validators.required, Validators.pattern(/^\d{7}$/)]],
      residentialAddress: this.fb.group({
        province: ['', Validators.required],
        postalCode: [
          '',
          [Validators.required, Validators.pattern(/^\d{2}-\d{3}$/)],
        ],
        city: ['', Validators.required],
        number: ['', Validators.required],
        street: ['', Validators.required],
      }),
    });
  }

  protected onSubmit(): void {
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      this.toastService.showError(
        this.translationService.translate('admin.addDoctor.validation.fillAll'),
      );
      return;
    }

    this.isSubmitting.set(true);

    // Tutaj będzie integracja z API
    console.log('Doctor data:', this.doctorForm.value);

    // Symulacja zapisu
    setTimeout(() => {
      this.isSubmitting.set(false);
      this.toastService.showSuccess(
        this.translationService.translate('admin.addDoctor.success'),
      );
      this.router.navigate(['/admin/doctors']);
    }, 1000);
  }

  protected onCancel(): void {
    this.router.navigate(['/admin/doctors']);
  }

  protected getFieldError(fieldPath: string): string {
    const control = this.getControl(fieldPath);
    if (!control?.errors || !control.touched) return '';

    if (control.errors['required'])
      return this.translationService.translate(
        'admin.addDoctor.validation.required',
      );
    if (control.errors['email'])
      return this.translationService.translate(
        'admin.addDoctor.validation.email',
      );
    if (control.errors['minlength']) {
      const requiredLength = control.errors['minlength'].requiredLength;
      return this.translationService
        .translate('admin.addDoctor.validation.minLength')
        .replace('{length}', requiredLength.toString());
    }
    if (control.errors['pattern']) {
      if (fieldPath === 'personalId')
        return this.translationService.translate(
          'admin.addDoctor.validation.personalId',
        );
      if (fieldPath === 'pwzNumber')
        return this.translationService.translate(
          'admin.addDoctor.validation.pwzNumber',
        );
      if (fieldPath.includes('postalCode'))
        return this.translationService.translate(
          'admin.addDoctor.validation.postalCode',
        );
    }
    return '';
  }

  private getControl(fieldPath: string) {
    const path = fieldPath.split('.');
    let control = this.doctorForm;

    for (const key of path) {
      control = control.get(key) as FormGroup;
      if (!control) return null;
    }

    return control;
  }

  protected isFieldInvalid(fieldPath: string): boolean {
    const control = this.getControl(fieldPath);
    return !!(control?.invalid && control?.touched);
  }
}
