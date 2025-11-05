import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { switchMap, tap } from 'rxjs';
import { DoctorPageModel } from '../../../../core/models/doctor.model';
import { InstitutionShortInfo } from '../../../../core/models/institution.model';
import { Specialisation } from '../../../../core/models/specialisation.model';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { SpecialisationService } from '../../../../core/services/specialisation/specialisation.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { DoctorAddressFormComponent } from '../shared/doctor-address-form/doctor-address-form';
import { DoctorInstitutionsListComponent } from '../shared/doctor-institutions-list/doctor-institutions-list';
import { DoctorPersonalInfoFormComponent } from '../shared/doctor-personal-info-form/doctor-personal-info-form';
import { DoctorProfessionalInfoFormComponent } from '../shared/doctor-professional-info-form/doctor-professional-info-form';

@Component({
  selector: 'app-edit-doctor-details',
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
    DoctorInstitutionsListComponent,
  ],
  templateUrl: './edit-doctor-details.html',
  styleUrls: ['./edit-doctor-details.scss'],
})
export class EditDoctorDetailsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);
  private doctorService = inject(DoctorService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private activatedRoute = inject(ActivatedRoute);
  protected doctorForm!: FormGroup;
  protected isSubmitting = signal<boolean>(false);
  private doctor = signal<DoctorPageModel | undefined>(undefined);
  protected doctorInstitutions = signal<InstitutionShortInfo[]>([]);
  protected availableInstitutions = signal<InstitutionShortInfo[]>([]);
  private specialisationService = inject(SpecialisationService);
  protected specialisations = signal<Specialisation[]>([]);

  ngOnInit(): void {
    this.initializeForm();
    const doctorId = this.activatedRoute.snapshot.paramMap.get('doctorId');

    if (doctorId) {
      this.specialisationService
        .getSpecialisations(false)
        .pipe(
          tap((specialisations) => {
            this.specialisations.set(specialisations);
          }),
          switchMap(() => this.doctorService.getDoctorDetails(doctorId)),
          takeUntilDestroyed(this.destroyRef),
        )
        .subscribe((doctor) => {
          this.doctor.set(doctor);
          this.patchForm(doctor);
        });
    } else {
      this.loadSpecialisations();
    }

    this.loadAvailableInstitutions();
  }

  private initializeForm(): void {
    this.doctorForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      surname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      birthDate: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      personalId: [
        { value: '', disabled: true },
        [Validators.required, Validators.pattern(/^\d{11}$/)],
      ],
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

  private loadAvailableInstitutions(): void {
    console.log(this.institutionStoreService.institutionOptions());
    this.availableInstitutions.set(
      this.institutionStoreService.institutionOptions().map((inst) => ({
        institutionId: inst.id,
        institutionName: inst.name,
      })),
    );
  }

  private patchForm(doctor: DoctorPageModel): void {
    this.doctorForm.patchValue({
      name: doctor.name,
      surname: doctor.surname,
      email: 'email@cos.pl',
      birthDate: new Date(),
      phoneNumber: 'doctor.phoneNumber',
      personalId: 'doctor.govId',
      pwzNumber: 'doctor.pwzNumber',
      specialisation: doctor.specialisation[0]
        .split(',')
        .map((spec) => spec.trim()),
      residentialAddress: {
        province: 'doctor.province',
        postalCode: 'doctor.postalCode',
        city: 'doctor.city',
        street: 'doctor.street',
        number: 'doctor.number',
      },
    });
    this.doctorInstitutions.set(doctor.institutions);
  }

  protected onSubmit(): void {
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      this.toastService.showError(
        this.translationService.translate(
          'admin.editDoctor.validation.fillAll',
        ),
      );
      return;
    }

    this.isSubmitting.set(true);
    const formValue = this.doctorForm.getRawValue();
    const updatedDoctorData = { ...formValue };

    console.log('Updated doctor data:', updatedDoctorData);
    setTimeout(() => {
      this.isSubmitting.set(false);
      this.toastService.showSuccess(
        this.translationService.translate('admin.editDoctor.success'),
      );
      this.router.navigate(['/admin/doctors']);
    }, 1000);
  }

  protected onCancel(): void {
    this.router.navigate(['/admin/doctors']);
  }

  protected onAddInstitution(institution: InstitutionShortInfo): void {
    this.doctorInstitutions.update((institutions) => [
      ...institutions,
      institution,
    ]);
  }

  protected onRemoveInstitution(_institutionId: string): void {
    this.doctorInstitutions.update((institutions) =>
      institutions.filter((i) => i.institutionId !== _institutionId),
    );
  }

  private loadSpecialisations(): void {
    this.specialisationService
      .getSpecialisations(false)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((specialisations) => {
        this.specialisations.set(specialisations);
      });
  }

  protected getFieldError(fieldPath: string): string {
    const control = this.doctorForm.get(fieldPath);
    if (!control?.errors || !control.touched) return '';
    if (control.errors['required'])
      return this.translationService.translate(
        'admin.editDoctor.validation.required',
      );
    if (control.errors['email'])
      return this.translationService.translate(
        'admin.editDoctor.validation.email',
      );
    if (control.errors['minlength']) {
      const requiredLength = control.errors['minlength'].requiredLength;
      return this.translationService
        .translate('admin.editDoctor.validation.minLength')
        .replace('{length}', requiredLength.toString());
    }
    if (control.errors['pattern']) {
      if (fieldPath === 'personalId')
        return this.translationService.translate(
          'admin.editDoctor.validation.personalId',
        );
      if (fieldPath === 'pwzNumber')
        return this.translationService.translate(
          'admin.editDoctor.validation.pwzNumber',
        );
      if (fieldPath.includes('postalCode'))
        return this.translationService.translate(
          'admin.editDoctor.validation.postalCode',
        );
    }
    return '';
  }

  protected isFieldInvalid = (fieldPath: string): boolean => {
    const control = this.doctorForm.get(fieldPath);
    return !!(control?.invalid && control?.touched);
  };

  protected getFieldErrorFn = (fieldPath: string): string => {
    return this.getFieldError(fieldPath);
  };

  protected get residentialAddressForm(): FormGroup {
    return this.doctorForm.get('residentialAddress') as FormGroup;
  }
}
