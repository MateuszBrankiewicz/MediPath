import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { Institution } from '../../../core/models/institution.model';
import { InstitutionService } from '../../../core/services/institution/institution.service';
import { ToastService } from '../../../core/services/toast/toast.service';
import { TranslationService } from '../../../core/services/translation/translation.service';
import {
  DetailItem,
  DetailSection,
  EmployeeCard,
  type EmployeeCardData,
  InfoCard,
} from '../../shared/components/index';

@Component({
  selector: 'app-institution-view',
  imports: [
    RouterLink,
    ButtonModule,
    ChipModule,
    TagModule,
    ProgressSpinnerModule,
    EmployeeCard,
    InfoCard,
    DetailSection,
    DetailItem,
  ],
  templateUrl: './institution-view.html',
  styleUrl: './institution-view.scss',
})
export class InstitutionView implements OnInit {
  protected translationService = inject(TranslationService);
  private institutionService = inject(InstitutionService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected isLoading = signal<boolean>(false);
  protected institutionId = signal<string>('');
  protected institution = signal<Institution | null>(null);
  protected employees = signal<EmployeeCardData[]>([]);

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
          this.institution.set(institution);

          const employeeList: EmployeeCardData[] = institution.employees.map(
            (emp) => ({
              name: emp.name,
              surname: emp.surname,
              userId: emp.userId,
              roleName: this.getRoleName(emp.roleCode),
              pfpImage: emp.pfpImage,
              specialisation: emp.specialisation,
              pwz: emp.userId,
            }),
          );
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
          this.router.navigate(['/admin/institutions']);
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
    const typeMap: Record<string, string> = {
      cardiology: this.translationService.translate(
        'admin.institution.types.cardiology',
      ),
      dermatology: this.translationService.translate(
        'admin.institution.types.dermatology',
      ),
      neurology: this.translationService.translate(
        'admin.institution.types.neurology',
      ),
      pediatrics: this.translationService.translate(
        'admin.institution.types.pediatrics',
      ),
      psychiatry: this.translationService.translate(
        'admin.institution.types.psychiatry',
      ),
      general: this.translationService.translate(
        'admin.institution.types.general',
      ),
    };
    return typeMap[typeCode] || typeCode;
  }

  protected onEditInstitution(): void {
    this.router.navigate(['/admin/institutions', this.institutionId(), 'edit']);
  }

  protected onAddDoctor(): void {
    this.router.navigate(['/admin/doctors', 'add']);
  }

  protected onViewDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId]);
  }

  protected onEditDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId, 'edit']);
  }

  protected getRatingColor(
    rating: number,
  ): 'success' | 'info' | 'warn' | 'danger' {
    if (rating >= 4.5) return 'success';
    if (rating >= 4.0) return 'info';
    if (rating >= 3.0) return 'warn';
    return 'danger';
  }
}
