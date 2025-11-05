import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { SkeletonModule } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { DoctorProfile } from '../../../../core/models/doctor.model';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InstitutionStoreService } from '../../services/institution/institution-store.service';
import { SelectInstitution } from '../shared/select-institution/select-institution';

@Component({
  selector: 'app-doctor-list',
  imports: [
    CardModule,
    ButtonModule,
    InputTextModule,
    IconFieldModule,
    InputIconModule,
    TagModule,
    FormsModule,
    SkeletonModule,
    SelectInstitution,
    ProgressSpinner,
  ],
  templateUrl: './doctor-list.html',
  styleUrl: './doctor-list.scss',
})
export class DoctorList {
  private institutionService = inject(InstitutionService);
  private institutionStoreService = inject(InstitutionStoreService);
  private destroyRef = inject(DestroyRef);
  private router = inject(Router);
  private toastService = inject(ToastService);
  protected translationService = inject(TranslationService);

  protected doctors = signal<DoctorProfile[]>([]);
  protected searchQuery = signal<string>('');
  protected isLoading = signal<boolean>(true);

  protected filteredDoctors = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    const allDoctors = this.doctors();

    if (!query) {
      return allDoctors;
    }

    return allDoctors.filter((doctor) => {
      const fullName =
        `${doctor.doctorName} ${doctor.doctorSurname}`.toLowerCase();
      const licenceNumber = doctor.licenceNumber.toLowerCase();
      return fullName.includes(query) || licenceNumber.includes(query);
    });
  });

  protected readonly institutionName = computed(() => {
    return this.institutionStoreService.getInstitution().name;
  });

  constructor() {
    effect(() => {
      const selectedInstitution =
        this.institutionStoreService.selectedInstitution();
      if (!selectedInstitution) {
        return;
      }
      const institutionId = selectedInstitution.id;
      this.loadDoctors(institutionId);
    });
  }

  private loadDoctors(doctorId: string): void {
    this.isLoading.set(true);

    this.institutionService
      .getDoctorsForInstitution(doctorId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (doctors) => {
          this.doctors.set(doctors);
          this.isLoading.set(false);
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate('admin.doctorList.error.load'),
          );
          this.isLoading.set(false);
        },
      });
  }

  protected onAddDoctor(): void {
    this.router.navigate(['/admin/add-doctor']);
  }

  protected onViewDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId]);
  }

  protected onEditDoctor(doctorId: string): void {
    this.router.navigate(['/admin/doctors', doctorId, 'edit']);
  }

  protected formatRating(rating: number): string {
    return rating.toFixed(1);
  }

  protected getRatingColor(
    rating: number,
  ): 'success' | 'info' | 'warn' | 'danger' {
    if (rating >= 4.5) return 'success';
    if (rating >= 3.5) return 'info';
    if (rating >= 2.5) return 'warn';
    return 'danger';
  }
}
