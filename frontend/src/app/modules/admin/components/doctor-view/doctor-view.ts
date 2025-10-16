import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ChipModule } from 'primeng/chip';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { DoctorPageModel } from '../../../../core/models/doctor.model';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  DetailItem,
  DetailSection,
  InfoCard,
} from '../../../shared/components/index';

@Component({
  selector: 'app-doctor-view',
  imports: [
    RouterLink,
    ButtonModule,
    ChipModule,
    TagModule,
    ProgressSpinnerModule,
    InfoCard,
    DetailSection,
    DetailItem,
  ],
  templateUrl: './doctor-view.html',
  styleUrl: './doctor-view.scss',
})
export class DoctorView implements OnInit {
  protected translationService = inject(TranslationService);
  private doctorService = inject(DoctorService);
  private toastService = inject(ToastService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  protected isLoading = signal<boolean>(false);
  protected doctorId = signal<string>('');
  protected doctor = signal<DoctorPageModel | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('doctorId');
    if (id) {
      this.doctorId.set(id);
      this.loadDoctor(id);
    }
  }

  private loadDoctor(id: string): void {
    this.isLoading.set(true);

    this.doctorService
      .getDoctorDetails(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (doctor: DoctorPageModel) => {
          this.doctor.set(doctor);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Error loading doctor:', error);
          this.toastService.showError(
            this.translationService.translate('admin.doctor.error.loadFailed'),
          );
          this.isLoading.set(false);
          this.router.navigate(['/admin/doctors']);
        },
      });
  }

  protected onEditDoctor(): void {
    this.router.navigate(['/admin/doctors', this.doctorId(), 'edit']);
  }

  protected onViewInstitution(institutionId: string): void {
    this.router.navigate(['/admin/institutions', institutionId]);
  }

  protected getRatingColor(
    rating: number,
  ): 'success' | 'info' | 'warn' | 'danger' {
    if (rating >= 4.5) return 'success';
    if (rating >= 4.0) return 'info';
    if (rating >= 3.0) return 'warn';
    return 'danger';
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
}
