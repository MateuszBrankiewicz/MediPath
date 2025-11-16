import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { CheckboxModule } from 'primeng/checkbox';
import { ChipModule } from 'primeng/chip';
import { Divider } from 'primeng/divider';
import { DialogService } from 'primeng/dynamicdialog';
import { FileUploadModule } from 'primeng/fileupload';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { map } from 'rxjs';
import { Institution } from '../../../../core/models/institution.model';
import { Specialisation } from '../../../../core/models/specialisation.model';
import { AddressService } from '../../../../core/services/address/address.service';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { SpecialisationService } from '../../../../core/services/specialisation/specialisation.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AcceptActionDialogComponent } from '../../../shared/components/ui/accept-action-dialog/accept-action-dialog-component';

interface Employee {
  name: string;
  surname: string;
  userId: string;
  roleCode: number;
  roleName?: string;
  pfpImage?: string;
  specialisation?: string[];
}

@Component({
  selector: 'app-edit-institution-details',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    ButtonModule,
    InputTextModule,
    InputMaskModule,
    FileUploadModule,
    TagModule,
    ChipModule,
    ProgressSpinnerModule,
    CardModule,
    MultiSelectModule,
    Divider,
    SelectModule,
    CheckboxModule,
  ],
  providers: [DialogService],
  templateUrl: './edit-institution-details.html',
  styleUrl: './edit-institution-details.scss',
})
export class EditInstitutionDetails implements OnInit {
  protected translationService = inject(TranslationService);
  private institutionService = inject(InstitutionService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  private fb = inject(FormBuilder);
  private authService = inject(AuthenticationService);
  private dialogService = inject(DialogService);

  private addressService = inject(AddressService);

  protected isLoading = signal<boolean>(false);
  protected isSaving = signal<boolean>(false);
  protected institutionForm!: FormGroup;
  protected institutionId = signal<string>('');
  protected institutionImage = signal<string>('');
  protected isEditMode = signal<boolean>(false);

  protected provinces = signal<string[]>([]);
  protected cities = signal<string[]>([]);

  protected selectedTypes = signal<string[]>([]);
  protected employees = signal<Employee[]>([]);
  protected searchQuery = signal<string>('');
  private specialisationService = inject(SpecialisationService);
  protected readonly availableTypes = signal<Specialisation[]>([]);

  ngOnInit(): void {
    this.initializeForm();
    this.loadAvailableCities();
    this.loadAvailableProvinces();
    this.loadAvailableTypes();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.institutionId.set(id);
      this.loadInstitution(id);
    } else {
      this.isEditMode.set(false);
    }
  }

  private initializeForm(): void {
    this.institutionForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      specialisation: [[], [Validators.required]],
      isPublic: [false],
      address: this.fb.group({
        province: ['', [Validators.required, Validators.minLength(2)]],
        postalCode: [
          '',
          [Validators.required, Validators.pattern(/^\d{2}-\d{3}$/)],
        ],
        city: ['', [Validators.required, Validators.minLength(2)]],
        number: ['', [Validators.required]],
        street: ['', [Validators.required, Validators.minLength(2)]],
      }),
    });
  }

  private loadAvailableTypes(): void {
    this.specialisationService
      .getSpecialisations(true)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((specialisations) => {
        this.availableTypes.set(specialisations);
      });
  }

  private loadInstitution(id: string): void {
    this.isLoading.set(true);

    this.institutionService
      .getInstitution(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (institution: Institution) => {
          this.institutionForm.patchValue({
            name: institution.name,
            description: '',
            specialisation: institution.specialisation || [],
            isPublic: institution.isPublic ?? false,
            address: {
              province: institution.address.province,
              city: institution.address.city,
              street: institution.address.street || '',
              number: institution.address.number,
              postalCode: institution.address.postalCode,
            },
          });

          this.institutionImage.set(institution.image || '');
          this.selectedTypes.set(institution.specialisation || []);

          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Error loading institution:', error);
          this.toastService.showError(
            this.translationService.translate(
              'admin.institution.error.loadFailed',
            ),
          );
          this.isLoading.set(false);
        },
      });
  }

  protected isFieldInvalid(fieldPath: string): boolean {
    const field = this.getField(fieldPath);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  protected getFieldError(fieldPath: string): string {
    const field = this.getField(fieldPath);
    if (!field || !field.errors) return '';

    if (field.errors['required']) {
      return this.translationService.translate(
        'admin.institution.validation.required',
      );
    }
    if (field.errors['minlength']) {
      return this.translationService.translate(
        'admin.institution.validation.minLength',
        { min: field.errors['minlength'].requiredLength },
      );
    }
    if (field.errors['pattern']) {
      return this.translationService.translate(
        'admin.institution.validation.invalidFormat',
      );
    }
    return '';
  }

  private getField(fieldPath: string) {
    return this.institutionForm.get(fieldPath);
  }

  protected getRoleName(roleCode: number): string {
    const roles: Record<number, string> = {
      1: this.translationService.translate('roles.admin'),
      2: this.translationService.translate('roles.doctor'),
      3: this.translationService.translate('roles.nurse'),
      4: this.translationService.translate('roles.receptionist'),
    };
    return roles[roleCode] || this.translationService.translate('roles.staff');
  }

  protected onImageUpload(event: { files: File[] }): void {
    const file = event.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.institutionImage.set(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  }

  protected saveInstitution(): void {
    if (this.institutionForm.invalid) {
      Object.keys(this.institutionForm.controls).forEach((key) => {
        const control = this.institutionForm.get(key);
        control?.markAsTouched();
        if (control && 'controls' in control) {
          const formGroup = control as { controls: Record<string, unknown> };
          Object.keys(formGroup.controls).forEach((nestedKey) => {
            control.get(nestedKey)?.markAsTouched();
          });
        }
      });
      this.toastService.showError(
        this.translationService.translate(
          'admin.institution.error.validationFailed',
        ),
      );
      return;
    }

    const institutionObject: Partial<Institution> = {
      ...this.institutionForm.value,
      image: this.institutionImage(),
    };
    this.isSaving.set(true);

    if (this.isEditMode()) {
      // Edit existing institution
      this.institutionService
        .editInstitution(this.institutionId(), institutionObject)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.isSaving.set(false);
            this.toastService.showSuccess(
              this.translationService.translate(
                'admin.institution.success.saved',
              ),
            );
            this.router.navigate(['/admin/institutions']);
          },
          error: (error) => {
            console.error('Error editing institution:', error);
            this.toastService.showError(
              this.translationService.translate(
                'admin.institution.error.saveFailed',
              ),
            );
            this.isSaving.set(false);
          },
        });
    } else {
      // Create new institution
      this.institutionService
        .addInstitution(institutionObject)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.isSaving.set(false);
            this.toastService.showSuccess(
              this.translationService.translate(
                'admin.institution.success.created',
              ),
            );
            this.router.navigate(['/admin/institutions']);
          },
          error: () => {
            this.toastService.showError(
              this.translationService.translate(
                'admin.institution.error.createFailed',
              ),
            );
            this.isSaving.set(false);
          },
        });
    }
  }

  protected loadAvailableCities(): void {
    this.addressService
      .getCities('')
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((cities) => cities.map((c) => c.name)),
      )
      .subscribe((cities) => this.cities.set(cities));
  }

  protected loadAvailableProvinces(): void {
    this.addressService
      .getProvinces()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((provinces) => this.provinces.set(provinces));
  }

  protected cancel(): void {
    this.router.navigate(['/admin/institutions']);
  }

  protected isAdmin(): boolean {
    return this.authService.getLastPanel() === 'admin';
  }

  protected deactivateInstitution(): void {
    const ref = this.dialogService.open(AcceptActionDialogComponent, {
      data: {
        message: this.translationService.translate(
          'confirmationDialogs.disableInstitutionMessage',
        ),
      },
      width: '400px',
    });

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((accept: boolean) => {
        if (!accept) {
          return;
        }
        this.institutionService
          .deactivateInstitution(this.institutionId())
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.toastService.showSuccess(
                this.translationService.translate(
                  'institution.disabledSuccessfully',
                ),
              );
              this.router.navigate(['/admin/institutions']);
            },
            error: () => {
              this.toastService.showError(
                this.translationService.translate('institution.disableError'),
              );
            },
          });
      });
  }
}
