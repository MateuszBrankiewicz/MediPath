import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { FileUploadModule } from 'primeng/fileupload';
import { InputTextModule } from 'primeng/inputtext';
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
    FormsModule,
    ButtonModule,
    InputTextModule,
    FileUploadModule,
    TagModule,
    ChipModule,
    ProgressSpinnerModule,
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

  protected isLoading = signal<boolean>(false);
  protected isSaving = signal<boolean>(false);

  // Institution data
  protected institutionId = signal<string>('');
  protected institutionName = signal<string>('');
  protected description = signal<string>('');
  protected institutionImage = signal<string>('');
  protected isPublic = signal<boolean>(true);

  // Address
  protected province = signal<string>('');
  protected city = signal<string>('');
  protected street = signal<string>('');
  protected number = signal<string>('');
  protected postalCode = signal<string>('');

  // Types and employees
  protected selectedTypes = signal<string[]>([]);
  protected selectedInstitutionType = signal<string>('');
  protected employees = signal<Employee[]>([]);
  protected searchQuery = signal<string>('');

  protected availableTypes: InstitutionType[] = [
    { name: 'Cardiologist', code: 'cardiology' },
    { name: 'Dermatologist', code: 'dermatology' },
    { name: 'Neurologist', code: 'neurology' },
    { name: 'Pediatrician', code: 'pediatrics' },
    { name: 'Psychiatrist', code: 'psychiatry' },
    { name: 'General Practice', code: 'general' },
  ];

  // New employee form
  protected showAddEmployee = signal<boolean>(false);
  protected newEmployee = signal<{
    name: string;
    surname: string;
    userId: string;
    roleCode: number;
    pfpImage?: string;
  }>({
    name: '',
    surname: '',
    userId: '',
    roleCode: 2, // Default to doctor
    pfpImage: '',
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.institutionId.set(id);
      this.loadInstitution(id);
    }
  }

  private loadInstitution(id: string): void {
    this.isLoading.set(true);

    this.institutionService
      .getInstitution(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (institution: Institution) => {
          this.institutionName.set(institution.name);
          this.institutionImage.set(institution.image || '');
          this.isPublic.set(institution.isPublic);

          // Address
          this.province.set(institution.address.province);
          this.city.set(institution.address.city);
          this.street.set(institution.address.street || '');
          this.number.set(institution.address.number);
          this.postalCode.set(institution.address.postalCode);

          // Types
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
          this.toastService.showError('institution.error.load');
          this.isLoading.set(false);
        },
      });
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

  protected getTypeName(typeCode: string): string {
    const type = this.availableTypes.find((t) => t.code === typeCode);
    return type ? type.name : typeCode;
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
    this.toastService.showSuccess('institution.success.employee_added');
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

  protected onEmployeeImageUpload(event: { files: File[] }): void {
    const file = event.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.newEmployee.update((emp) => ({
          ...emp,
          pfpImage: e.target?.result as string,
        }));
      };
      reader.readAsDataURL(file);
    }
  }

  protected addEmployee(): void {
    const emp = this.newEmployee();
    if (emp.name && emp.surname && emp.userId) {
      const newEmp: Employee = {
        ...emp,
        roleName: this.getRoleName(emp.roleCode),
      };
      this.employees.update((list) => [...list, newEmp]);
      this.resetEmployeeForm();
      this.showAddEmployee.set(false);
      this.toastService.showSuccess('institution.success.employee_added');
    } else {
      this.toastService.showError('institution.error.employee_incomplete');
    }
  }

  protected removeEmployee(userId: string): void {
    this.employees.update((list) =>
      list.filter((emp) => emp.userId !== userId),
    );
    this.toastService.showSuccess('institution.success.employee_removed');
  }

  private resetEmployeeForm(): void {
    this.newEmployee.set({
      name: '',
      surname: '',
      userId: '',
      roleCode: 2,
      pfpImage: '',
    });
  }

  protected cancelAddEmployee(): void {
    this.resetEmployeeForm();
    this.showAddEmployee.set(false);
  }

  protected saveInstitution(): void {
    if (!this.validateForm()) {
      this.toastService.showError('institution.error.validation');
      return;
    }

    this.isSaving.set(true);

    // TODO: Implement actual save to backend
    setTimeout(() => {
      this.isSaving.set(false);
      this.toastService.showSuccess('institution.success.saved');
      this.router.navigate(['/admin/institutions']);
    }, 1000);
  }

  private validateForm(): boolean {
    return !!(
      this.institutionName() &&
      this.city() &&
      this.province() &&
      this.postalCode() &&
      this.number()
    );
  }

  protected cancel(): void {
    this.router.navigate(['/admin/institutions']);
  }
}
