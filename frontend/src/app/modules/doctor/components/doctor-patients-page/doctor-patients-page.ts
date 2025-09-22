import { Component, inject, signal } from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Patient } from '../../models/patient.model';
import { PatientCardComponent } from '../patient-card/patient-card';

@Component({
  selector: 'app-doctor-patients-page',
  imports: [PatientCardComponent],
  templateUrl: './doctor-patients-page.html',
  styleUrl: './doctor-patients-page.scss',
})
export class DoctorPatientsPage {
  translationService = inject(TranslationService);

  // Mock data - podobne do tego z obrazka
  patients = signal<Patient[]>([
    {
      id: '1',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
    {
      id: '2',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
    {
      id: '3',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
    {
      id: '4',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
    {
      id: '5',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
    {
      id: '6',
      firstName: 'Monika',
      lastName: 'Nowak',
      lastVisitDate: '06.03.2025',
    },
  ]);

  filteredPatients = this.patients;

  onViewPatientProfile(patient: Patient) {
    console.log('Viewing profile for:', patient);
    // Tutaj będzie logika przekierowania do profilu pacjenta
  }
}
