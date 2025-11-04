import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VisitResponse } from '../../../../core/models/visit.model';
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
  private router = inject(Router);
  visits: AppointmentCardData[] = [];

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
    this.router.navigate(['/doctor/patient-profile', visitId]);
  }

  cancelVisit(visitId: string) {
    const visit = this.visits.find((v) => v.id === visitId);
    if (visit && visit.status === 'scheduled') {
      visit.status = 'canceled';
      this.filterVisits();
    }
  }

  private initDoctorVisits(): void {
    this.doctorService
      .getDoctorVisits()
      .subscribe((visits: VisitResponse[]) => {
        console.log('Fetched doctor visits:', visits);
        this.visits = (Array.isArray(visits) ? visits : []).map((visit) => ({
          id: visit.id,
          patientName: visit.patient
            ? `${visit.patient.name} ${visit.patient.surname}`
            : '',
          institutionName: visit.institution?.institutionName || '',
          visitDate: visit.time?.startTime
            ? this.formatDate(visit.time.startTime)
            : '',
          visitTime: visit.time?.startTime
            ? this.formatTime(visit.time.startTime)
            : '',
          status:
            visit.status === 'Upcoming'
              ? 'scheduled'
              : visit.status === 'Completed'
                ? 'completed'
                : 'canceled',
          patientId: visit.patient?.userId,
        }));
        this.filteredVisits = [...this.visits];
      });
  }

  private formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleDateString('pl-PL', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
        })
      : '';
  }

  private formatTime(dateStr: string): string {
    const d = new Date(dateStr);
    return d
      ? d.toLocaleTimeString('pl-PL', {
          hour: '2-digit',
          minute: '2-digit',
          hour12: false,
        })
      : '';
  }
}
