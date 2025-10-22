import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DatePickerModule } from 'primeng/datepicker';
import { DividerModule } from 'primeng/divider';
import { FloatLabelModule } from 'primeng/floatlabel';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { SelectChangeEvent, SelectModule } from 'primeng/select';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AddDoctorRequest } from '../../../../core/models/add-docotr.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { getCorrectDayFormat } from '../../../../utils/dateFormatter';

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
  private institutionService = inject(InstitutionService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private activatedRoute = inject(ActivatedRoute);
  private readonly institutionId = signal('');
  protected doctorForm!: FormGroup;
  protected isSubmitting = signal<boolean>(false);

  protected roleOptions = [
    {
      id: 1,
      name: 'Admin',
      roleCode: 8,
    },
    { id: 2, name: 'Staff', roleCode: 4 },
    { id: 3, name: 'Doctor', roleCode: 2 },
  ];

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
    const id = this.activatedRoute.snapshot.paramMap.get('id');
    if (id) {
      this.institutionId.set(id);
    }
    this.doctorForm.controls['specialisation'].disable();
    this.doctorForm.controls['pwzNumber'].disable();
  }

  private initializeForm(): void {
    this.doctorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      surname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      birthDate: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      personalId: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
      specialisation: [[]],
      pwzNumber: ['', [Validators.pattern(/^\d{7}$/)]],
      roleCode: [0, Validators.required],
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
    console.log(this.doctorForm.value.specialisation);
    this.isSubmitting.set(true);
    const formValue = this.doctorForm.value;
    const addEmployeeRequest: AddDoctorRequest = {
      userDetails: {
        name: formValue.name,
        surname: formValue.surname,
        email: formValue.email,
        birthDate: getCorrectDayFormat(formValue.birthDate),
        phoneNumber: formValue.phoneNumber,
        govID: formValue.personalId,
        province: formValue.residentialAddress.province,
        city: formValue.residentialAddress.city,
        postalCode: formValue.residentialAddress.postalCode,
        street: formValue.residentialAddress.street,
        number: formValue.residentialAddress.number,
      },
      doctorDetails: {
        licenceNumber: formValue.pwzNumber,
        specialisations: formValue.specialisation,
      },
      roleCode: formValue.roleCode,
    };

    this.institutionService
      .addEmployee(addEmployeeRequest, this.institutionId())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate('admin.addDoctor.success'),
          );
          this.router.navigate(['/admin/doctors']);
        },
        error: () => {
          this.isSubmitting.set(false);
          this.toastService.showError(
            this.translationService.translate('admin.addDoctor.error'),
          );
        },
        complete: () => {
          this.isSubmitting.set(false);
        },
      });
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

  protected roleChanged(event: SelectChangeEvent) {
    if (event.value === 2) {
      this.doctorForm.controls['specialisation'].enable();
      this.doctorForm.controls['pwzNumber'].enable();
    }
  }
}
