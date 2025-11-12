import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { catchError, of } from 'rxjs';
import { InstitutionService } from '../../../../../../core/services/institution/institution.service';
import { ToastService } from '../../../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../../../core/services/visits/visits.service';
import { ChangeDoctorDialogData } from '../../../../services/change-doctor-dialog.service';

interface DoctorOption {
  value: string;
  label: string;
  specialisations: string;
}

@Component({
  selector: 'app-change-doctor-dialog',
  imports: [
    CommonModule,
    FormsModule,
    SelectModule,
    ButtonModule,
    ProgressSpinnerModule,
  ],
  template: `
    <div class="change-doctor-dialog">
      @if (isLoading()) {
        <div class="loading-container">
          <p-progressSpinner />
          <p>
            {{ translationService.translate('admin.changeDoctor.loading') }}
          </p>
        </div>
      } @else {
        <div class="dialog-content">
          <p class="dialog-description">
            {{ translationService.translate('admin.changeDoctor.description') }}
          </p>

          <div class="current-doctor-info">
            <div class="current-doctor-label">
              {{
                translationService.translate('admin.changeDoctor.currentDoctor')
              }}
            </div>
            <div class="current-doctor-value">
              <i class="pi pi-user"></i>
              <span>{{ dialogData.currentDoctorName }}</span>
            </div>
          </div>

          <div class="form-field">
            <label for="doctor-select">{{
              translationService.translate('admin.changeDoctor.selectDoctor')
            }}</label>
            <p-select
              id="doctor-select"
              [options]="doctors()"
              [(ngModel)]="selectedDoctorId"
              optionLabel="label"
              optionValue="value"
              appendTo="body"
              [placeholder]="
                translationService.translate(
                  'admin.changeDoctor.selectDoctorPlaceholder'
                )
              "
              [filter]="true"
              [showClear]="true"
              styleClass="w-full"
            >
              <ng-template let-doctor pTemplate="item">
                <div class="doctor-item">
                  <div class="doctor-name">
                    {{ doctor.label }}
                  </div>
                  <div class="doctor-specialisations">
                    {{ doctor.specialisations }}
                  </div>
                </div>
              </ng-template>
            </p-select>
          </div>

          @if (errorMessage()) {
            <div class="error-message">
              <i class="pi pi-exclamation-triangle"></i>
              <span>{{ errorMessage() }}</span>
            </div>
          }
        </div>

        <div class="dialog-actions">
          <p-button
            [label]="translationService.translate('common.cancel')"
            severity="secondary"
            [outlined]="true"
            (onClick)="onCancel()"
          />
          <p-button
            [label]="translationService.translate('admin.changeDoctor.confirm')"
            (onClick)="onConfirm()"
            [disabled]="!selectedDoctorId || isSubmitting()"
            [loading]="isSubmitting()"
          />
        </div>
      }
    </div>
  `,
  styles: [
    `
      .change-doctor-dialog {
        padding: 1rem;
      }

      .loading-container {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 2rem;
        gap: 1rem;

        p {
          margin: 0;
          color: var(--text-color-secondary);
        }
      }

      .dialog-content {
        display: flex;
        flex-direction: column;
        gap: 1.5rem;
      }

      .dialog-description {
        margin: 0;
        color: var(--text-color-secondary);
        line-height: 1.5;
      }

      .current-doctor-info {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        padding: 1rem;
        background: var(--surface-50);
        border: 1px solid var(--surface-200);
        border-radius: var(--border-radius);

        .current-doctor-label {
          font-size: 0.875rem;
          font-weight: 600;
          color: var(--text-color-secondary);
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .current-doctor-value {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          color: var(--text-color);
          font-weight: 500;
          font-size: 1.125rem;

          i {
            color: var(--primary-color);
            font-size: 1.25rem;
          }
        }
      }

      .form-field {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;

        label {
          font-weight: 600;
          color: var(--text-color);
        }
      }

      .doctor-item {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        padding: 0.5rem 0;

        .doctor-name {
          font-weight: 500;
          color: var(--text-color);
        }

        .doctor-specialisations {
          font-size: 0.875rem;
          color: var(--text-color-secondary);
        }
      }

      .error-message {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.75rem;
        background: var(--red-50);
        border: 1px solid var(--red-200);
        border-radius: var(--border-radius);
        color: var(--red-700);

        i {
          font-size: 1.25rem;
        }
      }

      .dialog-actions {
        display: flex;
        justify-content: flex-end;
        gap: 0.75rem;
        margin-top: 1.5rem;
        padding-top: 1rem;
        border-top: 1px solid var(--surface-border);
      }
    `,
  ],
})
export class ChangeDoctorDialogComponent implements OnInit {
  protected readonly translationService = inject(TranslationService);
  private readonly dialogRef = inject(DynamicDialogRef);
  private readonly config = inject(DynamicDialogConfig);
  private readonly institutionService = inject(InstitutionService);
  private readonly visitsService = inject(VisitsService);
  private readonly toastService = inject(ToastService);

  protected readonly isLoading = signal(true);
  protected readonly isSubmitting = signal(false);
  protected readonly doctors = signal<DoctorOption[]>([]);
  protected readonly errorMessage = signal<string | null>(null);
  protected selectedDoctorId: string | null = null;

  protected dialogData!: ChangeDoctorDialogData;

  ngOnInit(): void {
    this.dialogData = this.config.data as ChangeDoctorDialogData;
    this.loadDoctors();
  }

  private loadDoctors(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.institutionService
      .getDoctorsForInstitution(this.dialogData.institutionId)
      .pipe(
        catchError((error) => {
          console.error('Error loading doctors:', error);
          this.errorMessage.set(
            this.translationService.translate(
              'admin.changeDoctor.loadDoctorsError',
            ),
          );
          return of([]);
        }),
      )
      .subscribe((doctors) => {
        const filteredDoctors = doctors
          .filter(
            (doctor) => doctor.doctorId !== this.dialogData.currentDoctorId,
          )
          .map((doctor) => ({
            value: doctor.doctorId,
            label: `${doctor.doctorName} ${doctor.doctorSurname}`,
            specialisations: doctor.licenceNumber || '',
          }));

        this.doctors.set(filteredDoctors);
        this.isLoading.set(false);

        if (filteredDoctors.length === 0) {
          this.errorMessage.set(
            this.translationService.translate(
              'admin.changeDoctor.noDoctorsAvailable',
            ),
          );
        }
      });
  }

  protected onCancel(): void {
    this.dialogRef.close();
  }

  protected onConfirm(): void {
    if (!this.selectedDoctorId) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.visitsService
      .changeDoctor(this.dialogData.visitId, this.selectedDoctorId)
      .pipe(
        catchError((error) => {
          console.error('Error changing doctor:', error);
          this.errorMessage.set(
            this.translationService.translate(
              'admin.changeDoctor.changeDoctorError',
            ),
          );
          this.isSubmitting.set(false);
          return of(null);
        }),
      )
      .subscribe((result) => {
        if (result !== null) {
          this.toastService.showSuccess(
            this.translationService.translate(
              'admin.changeDoctor.changeDoctorSuccess',
            ),
          );
          this.dialogRef.close({
            success: true,
            doctorId: this.selectedDoctorId,
          });
        } else {
          this.isSubmitting.set(false);
        }
      });
  }
}
