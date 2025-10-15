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
import { ChipModule } from 'primeng/chip';
import { Divider } from 'primeng/divider';
import { FileUploadModule } from 'primeng/fileupload';
import { InputMaskModule } from 'primeng/inputmask';
import { InputTextModule } from 'primeng/inputtext';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { Institution } from '../../../../core/models/institution.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

interface Employee {
  name: string;
  surname: string;
  userId: string;
  roleCode: number;
  roleName?: string;
  pfpImage?: string;
  specialisation?: string[];
}

interface InstitutionType {
  name: string;
  code: string;
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
  ],
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

  protected isLoading = signal<boolean>(false);
  protected isSaving = signal<boolean>(false);
  protected institutionForm!: FormGroup;
  protected institutionId = signal<string>('');
  protected institutionImage = signal<string>('');

  // Types and employees
  protected selectedTypes = signal<string[]>([]);
  protected employees = signal<Employee[]>([]);
  protected searchQuery = signal<string>('');

  protected availableTypes: InstitutionType[] = [];

  ngOnInit(): void {
    this.initializeForm();
    this.loadAvailableTypes();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.institutionId.set(id);
      this.loadInstitution(id);
    }
  }

  private initializeForm(): void {
    this.institutionForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      specialisation: [[], [Validators.required]],
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
    this.availableTypes = [
      {
        name: this.translationService.translate(
          'admin.institution.types.cardiology',
        ),
        code: 'cardiology',
      },
      {
        name: this.translationService.translate(
          'admin.institution.types.dermatology',
        ),
        code: 'dermatology',
      },
      {
        name: this.translationService.translate(
          'admin.institution.types.neurology',
        ),
        code: 'neurology',
      },
      {
        name: this.translationService.translate(
          'admin.institution.types.pediatrics',
        ),
        code: 'pediatrics',
      },
      {
        name: this.translationService.translate(
          'admin.institution.types.psychiatry',
        ),
        code: 'psychiatry',
      },
      {
        name: this.translationService.translate(
          'admin.institution.types.general',
        ),
        code: 'general',
      },
    ];
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

          // Employees
          const employeeList: Employee[] = institution.employees.map((emp) => ({
            name: emp.name,
            surname: emp.surname,
            userId: emp.userId,
            roleCode: emp.roleCode,
            roleName: this.getRoleName(emp.roleCode),
            pfpImage: emp.pfpImage,
            specialisation: emp.specialisation,
          }));
          this.employees.set(employeeList);

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

  protected addEmployeeFromSearch(): void {
    const query = this.searchQuery().trim();
    if (!query) return;

    // Mock: w rzeczywistości tutaj byłoby zapytanie do API
    // Dla przykładu dodajemy z PWZ jako userId
    const newEmp: Employee = {
      name: 'Jan',
      surname: 'Kowalski',
      userId: query,
      roleCode: 2,
      roleName: this.getRoleName(2),
    };

    this.employees.update((list) => [...list, newEmp]);
    this.searchQuery.set('');
    this.toastService.showSuccess(
      this.translationService.translate(
        'admin.institution.success.employeeAdded',
      ),
    );
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

  protected removeEmployee(userId: string): void {
    this.employees.update((list) =>
      list.filter((emp) => emp.userId !== userId),
    );
    this.toastService.showSuccess(
      this.translationService.translate(
        'admin.institution.success.employeeRemoved',
      ),
    );
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

    this.isSaving.set(true);

    // TODO: Implement actual save to backend
    setTimeout(() => {
      this.isSaving.set(false);
      this.toastService.showSuccess(
        this.translationService.translate('admin.institution.success.saved'),
      );
      this.router.navigate(['/admin/institutions']);
    }, 1000);
  }

  protected cancel(): void {
    this.router.navigate(['/admin/institutions']);
  }
}
