import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  AppointmentCardComponent,
  AppointmentCardData,
} from '../../../shared/components/appointment-card/appointment-card.component';

@Component({
  selector: 'app-doctor-visits',
  standalone: true,
  imports: [CommonModule, FormsModule, AppointmentCardComponent],
  templateUrl: './doctor-visits.html',
  styleUrl: './doctor-visits.scss',
})
export class DoctorVisits implements OnInit {
  translationService = inject(TranslationService);
  private doctorService = inject(DoctorService);

  visits: AppointmentCardData[] = [
    {
      id: '1',
      patientName: 'Anna Nowak',
      institutionName: 'Szpital Nr 4, Lublin',
      visitDate: '20-03-2025',
      visitTime: '14:30',
      status: 'scheduled',
    },
    {
      id: '2',
      patientName: 'Jan Kowalski',
      institutionName: 'Szpital Nr 4, Lublin',
      visitDate: '13-03-2025',
      visitTime: '10:00',
      status: 'canceled',
    },
    {
      id: '3',
      patientName: 'Janina Kowalska',
      institutionName: 'Gabinet Stomatologiczny Kowalska, Lublin',
      visitDate: '20-02-2025',
      visitTime: '16:00',
      status: 'completed',
    },
    {
      id: '4',
      patientName: 'Janina Kowalska',
      institutionName: 'Gabinet Stomatologiczny Kowalska, Lublin',
      visitDate: '20-01-2025',
      visitTime: '09:30',
      status: 'scheduled',
    },
    {
      id: '5',
      patientName: 'Janina Kowalska',
      institutionName: 'Gabinet Stomatologiczny Kowalska, Lublin',
      visitDate: '15-12-2024',
      visitTime: '11:15',
      status: 'completed',
    },
    {
      id: '6',
      patientName: 'Jan Kowalski',
      institutionName: 'Szpital Nr 4, Lublin',
      visitDate: '18-11-2024',
      visitTime: '15:45',
      status: 'scheduled',
    },
  ];

  filteredVisits: AppointmentCardData[] = [];
  selectedStatus = '';
  searchTerm = '';

  ngOnInit() {
    this.initDoctorVisits();
    this.filteredVisits = [...this.visits];
  }

  filterVisits() {
    this.filteredVisits = this.visits.filter((visit) => {
      const statusMatch =
        !this.selectedStatus || visit.status === this.selectedStatus;
      const searchMatch =
        !this.searchTerm ||
        visit.patientName
          .toLowerCase()
          .includes(this.searchTerm.toLowerCase()) ||
        visit.institutionName
          .toLowerCase()
          .includes(this.searchTerm.toLowerCase());

      return statusMatch && searchMatch;
    });
  }

  onStatusChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    this.selectedStatus = target.value;
    this.filterVisits();
  }

  onSearchChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.searchTerm = target.value;
    this.filterVisits();
  }

  viewPatientDetails(visitId: string) {
    console.log('Viewing patient details for visit:', visitId);
    // Implement navigation to patient details
  }

  cancelVisit(visitId: string) {
    const visit = this.visits.find((v) => v.id === visitId);
    if (visit && visit.status === 'scheduled') {
      visit.status = 'canceled';
      this.filterVisits();
    }
  }

  private initDoctorVisits(): void {
    this.doctorService.getDoctorVisits().subscribe((visits) => {
      console.log('Fetched doctor visits:', visits);
      // Map the fetched visits to AppointmentCardData and assign to this.visits
    });
  }
}
