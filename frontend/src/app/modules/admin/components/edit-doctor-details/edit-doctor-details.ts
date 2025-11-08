import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { forkJoin, of } from 'rxjs';
import { FullDoctorInfo } from '../../../../core/models/doctor.model';
import { InstitutionShortInfo } from '../../../../core/models/institution.model';
import { Specialisation } from '../../../../core/models/specialisation.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
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
  private institutionService = inject(InstitutionService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private activatedRoute = inject(ActivatedRoute);
  protected doctorForm!: FormGroup;
  protected isSubmitting = signal<boolean>(false);
  private doctor = signal<FullDoctorInfo | undefined>(undefined);
  private doctorId = signal<string>('');
  private originalInstitutions = signal<InstitutionShortInfo[]>([]);
  protected doctorInstitutions = signal<InstitutionShortInfo[]>([]);
  protected availableInstitutions = signal<InstitutionShortInfo[]>([]);
  private specialisationService = inject(SpecialisationService);
  protected specialisations = signal<Specialisation[]>([]);
  private readonly authentication = inject(AuthenticationService);
  protected cities = signal<string[]>([]);
  protected provinces = signal<string[]>([]);

  ngOnInit(): void {
    this.initializeForm();
    this.loadProvinces();
    this.loadCities();
    this.loadSpecialisations();
    this.loadAvailableInstitutions();

    const doctorId = this.activatedRoute.snapshot.paramMap.get('doctorId');
    if (doctorId) {
      this.doctorId.set(doctorId);
      this.doctorService
        .getDoctorFullInfo(doctorId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((doctorData) => {
          this.doctor.set(doctorData);
          this.originalInstitutions.set([...doctorData.institutions]);
          this.patchForm(doctorData);
        });
    }
  }

  private initializeForm(): void {
    this.doctorForm = this.fb.group({
      name: [{ value: '', disabled: true }, [Validators.required]],
      surname: [{ value: '', disabled: true }, [Validators.required]],
      email: [{ value: '', disabled: true }, [Validators.required]],
      birthDate: [{ value: '', disabled: true }, Validators.required],
      phoneNumber: [{ value: '', disabled: true }, Validators.required],
      personalId: [{ value: '', disabled: true }, [Validators.required]],
      specialisation: [[], Validators.required],
      pwzNumber: ['', [Validators.required, Validators.pattern(/^\d{7}$/)]],
      residentialAddress: this.fb.group({
        province: [{ value: '', disabled: true }, Validators.required],
        postalCode: [{ value: '', disabled: true }, Validators.required],
        city: [{ value: '', disabled: true }, Validators.required],
        number: [{ value: '', disabled: true }, Validators.required],
        street: [{ value: '', disabled: true }, Validators.required],
      }),
    });
  }

  private loadAvailableInstitutions(): void {
    this.availableInstitutions.set(
      this.institutionStoreService.institutionOptions().map((inst) => ({
        institutionId: inst.id,
        institutionName: inst.name,
      })),
    );
  }

  private patchForm(doctor: FullDoctorInfo): void {
    this.doctorForm.patchValue({
      name: doctor.name,
      surname: doctor.surname,
      email: doctor.email,
      birthDate: new Date(doctor.dateOfBirth),
      phoneNumber: '123456789', // TODO: add phoneNumber to FullDoctorInfo
      personalId: doctor.govId,
      pwzNumber: doctor.pwzNumber,
      specialisation: doctor.specialisation,
      residentialAddress: {
        province: doctor.address.province,
        postalCode: doctor.address.postalCode,
        city: doctor.address.city,
        street: doctor.address.street,
        number: doctor.address.number,
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
    const currentInstitutions = this.doctorInstitutions();
    const original = this.originalInstitutions();

    // Find institutions to add (new ones)
    const toAdd = currentInstitutions.filter(
      (curr) =>
        !original.some((orig) => orig.institutionId === curr.institutionId),
    );

    // Find institutions to remove (deleted ones)
    const toRemove = original.filter(
      (orig) =>
        !currentInstitutions.some(
          (curr) => curr.institutionId === orig.institutionId,
        ),
    );

    // Find institutions to update (existing ones - update specialisations)
    const toUpdate = currentInstitutions.filter((curr) =>
      original.some((orig) => orig.institutionId === curr.institutionId),
    );

    const requests: ReturnType<
      | typeof this.institutionService.addEmployees
      | typeof this.institutionService.deleteEmployee
      | typeof this.institutionService.updateEmployee
    >[] = [];

    // Add new institutions
    if (toAdd.length > 0) {
      toAdd.forEach((inst) => {
        requests.push(
          this.institutionService.addEmployees(inst.institutionId, [
            {
              userID: this.doctorId(),
              rolecode: inst.roleCode || 2, // Use institution-specific role code
              specialisations: formValue.specialisation,
            },
          ]),
        );
      });
    }

    // Remove institutions
    if (toRemove.length > 0) {
      toRemove.forEach((inst) => {
        requests.push(
          this.institutionService.deleteEmployee(
            inst.institutionId,
            this.doctorId(),
          ),
        );
      });
    }

    // Update existing institutions (specialisations and roleCode)
    if (toUpdate.length > 0) {
      toUpdate.forEach((inst) => {
        requests.push(
          this.institutionService.updateEmployee(inst.institutionId, {
            userID: this.doctorId(),
            roleCode: inst.roleCode || 2, // Use institution-specific role code
            specialisations: formValue.specialisation,
          }),
        );
      });
    }

    if (requests.length === 0) {
      this.isSubmitting.set(false);
      this.toastService.showInfo(
        this.translationService.translate('admin.editDoctor.noChanges'),
      );
      return;
    }

    forkJoin(requests.length > 0 ? requests : [of(null)])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.toastService.showSuccess(
            this.translationService.translate('admin.editDoctor.success'),
          );
          this.router.navigate(['/admin/doctors']);
        },
        error: (error: HttpErrorResponse) => {
          this.isSubmitting.set(false);
          if (error.status === 403) {
            this.toastService.showError(
              this.translationService.translate('admin.editDoctor.forbidden'),
            );
          } else if (error.status === 400) {
            this.toastService.showError(
              this.translationService.translate('admin.editDoctor.badRequest'),
            );
          } else {
            this.toastService.showError(
              this.translationService.translate('admin.editDoctor.error'),
            );
          }
        },
      });
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

  protected onRoleChanged(event: {
    institutionId: string;
    roleCode: number;
  }): void {
    this.doctorInstitutions.update((institutions) =>
      institutions.map((inst) =>
        inst.institutionId === event.institutionId
          ? { ...inst, roleCode: event.roleCode }
          : inst,
      ),
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

  private loadProvinces(): void {
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
              'admin.editDoctor.error.loadProvinces',
            ),
          );
        },
      });
  }

  private loadCities(): void {
    this.authentication
      .getCityWithoutSearch()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (cities) => {
          this.cities.set(cities.map((city) => city.name));
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate(
              'admin.editDoctor.error.loadCities',
            ),
          );
        },
      });
  }
}
