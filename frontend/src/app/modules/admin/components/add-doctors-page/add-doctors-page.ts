import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { AddDoctorRequest } from '../../../../core/models/add-docotr.model';
import { Specialisation } from '../../../../core/models/specialisation.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { SpecialisationService } from '../../../../core/services/specialisation/specialisation.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { getCorrectDayFormat } from '../../../../utils/dateFormatter';
import { DoctorAddressFormComponent } from '../shared/doctor-address-form/doctor-address-form';
import { DoctorPersonalInfoFormComponent } from '../shared/doctor-personal-info-form/doctor-personal-info-form';
import { DoctorProfessionalInfoFormComponent } from '../shared/doctor-professional-info-form/doctor-professional-info-form';
import { InputTextModule } from 'primeng/inputtext';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { FullDoctorInfo } from '../../../../core/models/doctor.model';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { MultiSelectChangeEvent } from 'primeng/multiselect';

@Component({
  selector: 'app-add-doctors-page',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    CardModule,
    ButtonModule,
    DividerModule,
    DoctorPersonalInfoFormComponent,
    DoctorProfessionalInfoFormComponent,
    DoctorAddressFormComponent,
    InputTextModule,
    FormsModule,
    ProgressSpinnerModule,
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
  private authentication = inject(AuthenticationService);
  private destroyRef = inject(DestroyRef);
  private activatedRoute = inject(ActivatedRoute);
  private readonly institutionId = signal('');
  protected doctorForm!: FormGroup;
  protected isSubmitting = signal<boolean>(false);
  private specialisationService = inject(SpecialisationService);
  protected cities = signal<string[]>([]);
  protected provinces = signal<string[]>([]);
  protected govId = new FormControl('');
  private userId = signal('');
  protected roleOptions = [
    {
      id: 1,
      name: 'Admin',
      roleCode: 8,
    },
    { id: 2, name: 'Staff', roleCode: 4 },
    { id: 3, name: 'Doctor', roleCode: 2 },
  ];
  private doctorService = inject(DoctorService);
  protected specialisations = signal<Specialisation[]>([]);
  protected userExist = signal(false);
  ngOnInit(): void {
    this.loadCities();
    this.loadProvinces();
    this.loadAvailableSpecialisations();
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
      roleCode: [[0], Validators.required],
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
    if (this.userExist()) {
      this.addEmployeeToInstitution();
      return;
    }

    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      this.toastService.showError(
        this.translationService.translate('admin.addDoctor.validation.fillAll'),
      );
      return;
    }
    this.isSubmitting.set(true);
    const formValue = this.doctorForm.value;
    const role = this.doctorForm.value.roleCode.reduce(
      (acc: number, element: number) => acc + element,
    );
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
      roleCode: role,
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

  protected isFieldInvalid = (fieldPath: string): boolean => {
    const control = this.getControl(fieldPath);
    return !!(control?.invalid && control?.touched);
  };

  protected getFieldErrorFn = (fieldPath: string): string => {
    return this.getFieldError(fieldPath);
  };

  protected roleChanged(event: MultiSelectChangeEvent) {
    if (event.value.includes(2)) {
      this.doctorForm.controls['specialisation'].enable();
      this.doctorForm.controls['pwzNumber'].enable();
    }
  }

  protected get residentialAddressForm(): FormGroup {
    return this.doctorForm.get('residentialAddress') as FormGroup;
  }
  private loadAvailableSpecialisations(): void {
    this.specialisationService
      .getSpecialisations(false)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (specialisations) => {
          this.specialisations.set(specialisations);
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.addDoctor.error.loadSpecialisations',
            ),
          );
        },
      });
  }
  private loadCities() {
    this.authentication
      .getCityWithoutSearch()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (cities) => {
          this.cities.set(cities.map((city) => city.name));
        },
        error: () => {
          this.toastService.showError('Failed to load cities');
        },
      });
  }
  private loadProvinces() {
    this.authentication
      .getProvinces()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (provinces) => {
          this.provinces.set(provinces);
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.addDoctor.error.loadProvinces',
            ),
          );
        },
      });
  }

  private patchFormValue(doctorData: FullDoctorInfo) {
    this.userExist.set(true);
    const birthDate = Array.isArray(doctorData.dateOfBirth)
      ? new Date(
          doctorData.dateOfBirth[0],
          doctorData.dateOfBirth[1] - 1,
          doctorData.dateOfBirth[2],
        )
      : null;

    this.doctorForm.patchValue({
      name: doctorData.name,
      surname: doctorData.surname,
      email: doctorData.email,
      birthDate: birthDate,
      phoneNumber: doctorData.phoneNumber,
      personalId: doctorData.govId,
      specialisation: doctorData.specialisations,
      pwzNumber: doctorData.licenceNumber,
      residentialAddress: {
        province: doctorData.address.province,
        city: doctorData.address.city,
        street: doctorData.address.street,
        number: doctorData.address.number,
        postalCode: doctorData.address.postalCode,
      },
    });
  }

  protected loadUserData() {
    this.isSubmitting.set(true);
    this.institutionService
      .findUserByGovId(this.govId.value!)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.userId.set(result.id);
          this.doctorService
            .getDoctorFullInfo(result.id)
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe((res) => {
              this.isSubmitting.set(false);
              this.patchFormValue(res);
            });
        },
        error: () => {
          this.isSubmitting.set(false);
          this.toastService.showError('User not found');
        },
      });
  }

  private addEmployeeToInstitution() {
    const role = this.doctorForm.value.roleCode.reduce(
      (acc: number, element: number) => acc + element,
    );
    this.institutionService
      .addEmployees(this.institutionId(), [
        {
          userID: this.userId(),
          rolecode: role,
          specialisations: this.doctorForm.value.specialisation,
        },
      ])
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
}
