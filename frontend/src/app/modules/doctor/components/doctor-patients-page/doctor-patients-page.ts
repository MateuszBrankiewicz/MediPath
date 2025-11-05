import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { map } from 'rxjs';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Patient } from '../../models/patient.model';
import { PatientCardComponent } from '../patient-card/patient-card';

@Component({
  selector: 'app-doctor-patients-page',
  imports: [PatientCardComponent, ProgressSpinnerModule],
  templateUrl: './doctor-patients-page.html',
  styleUrl: './doctor-patients-page.scss',
})
export class DoctorPatientsPage implements OnInit {
  translationService = inject(TranslationService);
  private destroyRef = inject(DestroyRef);
  private doctorService = inject(DoctorService);
  private router = inject(Router);
  protected readonly patients = signal<Patient[]>([]);
  protected readonly isDataLoading = signal<boolean>(true);
  filteredPatients = this.patients;
  ngOnInit(): void {
    this.doctorService
      .getDoctorPatients()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((response) => {
          return response.map((patient) => ({
            id: patient.id,
            firstName: patient.name,
            lastName: patient.surname,
            lastVisitDate: '',
          }));
        }),
      )
      .subscribe({
        next: (patients) => {
          this.patients.set(patients);
          this.isDataLoading.set(false);
        },
        error: (error) => {
          this.isDataLoading.set(false);
          console.error('Błąd podczas pobierania pacjentów:', error);
        },
      });
  }

  onViewPatientProfile(patient: Patient) {
    this.router.navigate(['/doctor/patient-profile', patient.id]);
  }
}
