import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

import { TabsModule } from 'primeng/tabs';
import { TagModule } from 'primeng/tag';
import { DoctorService } from '../../../../core/services/doctor/doctor.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

interface MedicalHistoryRecord {
  userId: string;
  title: string;
  note: string;
  date: string;
  doctor: { doctorName: string; doctorSurname: string } | null;
}

interface PatientVisit {
  id: string;
  note: string;
  institution: string;
  codes: { codeType: string; code: string; active: boolean }[];
  patientRemarks: string;
  startTime: number[];
  endTime: number[];
  status: 'Upcoming' | 'Completed' | 'Cancelled';
}

export interface PatientForDoctor {
  name: string;
  surname: string;
  phoneNumber: string;
  govId: string;
  birthDate: number[];
  pfp: string;
  medicalHistory: MedicalHistoryRecord[];
}

@Component({
  selector: 'app-patient-profile',
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    AvatarModule,
    DividerModule,
    TabsModule,
    TagModule,
    DatePipe,
    ProgressSpinnerModule,
  ],
  templateUrl: './patient-profile.html',
  styleUrl: './patient-profile.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientProfile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly toastService = inject(ToastService);
  protected readonly translationService = inject(TranslationService);
  private readonly doctorService = inject(DoctorService);

  protected readonly isLoading = signal(false);
  protected readonly patientData = signal<PatientForDoctor | null>(null);
  protected readonly patientVisits = signal<PatientVisit[]>([]);

  protected readonly fullName = computed(() => {
    const data = this.patientData();
    if (!data) return '';
    return `${data.name} ${data.surname}`;
  });

  protected readonly birthDateFormatted = computed(() => {
    const data = this.patientData();
    if (!data?.birthDate) return '';
    const [year, month, day] = data.birthDate;
    return new Date(year, month - 1, day).toLocaleDateString('pl-PL');
  });

  protected readonly age = computed(() => {
    const data = this.patientData();
    if (!data?.birthDate) return 0;
    const [year, month, day] = data.birthDate;
    const birthDate = new Date(year, month - 1, day);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (
      monthDiff < 0 ||
      (monthDiff === 0 && today.getDate() < birthDate.getDate())
    ) {
      age--;
    }
    return age;
  });

  protected readonly sortedMedicalHistory = computed(() => {
    const data = this.patientData();
    if (!data?.medicalHistory) return [];
    return [...data.medicalHistory].sort(
      (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime(),
    );
  });

  protected readonly sortedVisits = computed(() => {
    const visits = this.patientVisits();
    if (!Array.isArray(visits)) return [];
    return [...visits].sort((a, b) => {
      const dateA = new Date(
        a.startTime[0],
        a.startTime[1] - 1,
        a.startTime[2],
        a.startTime[3],
        a.startTime[4],
      );
      const dateB = new Date(
        b.startTime[0],
        b.startTime[1] - 1,
        b.startTime[2],
        b.startTime[3],
        b.startTime[4],
      );
      return dateB.getTime() - dateA.getTime();
    });
  });

  protected readonly upcomingVisits = computed(() => {
    return this.sortedVisits().filter((visit) => visit.status === 'Upcoming');
  });

  protected readonly completedVisits = computed(() => {
    return this.sortedVisits().filter((visit) => visit.status === 'Completed');
  });

  protected readonly cancelledVisits = computed(() => {
    return this.sortedVisits().filter((visit) => visit.status === 'Cancelled');
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.isLoading.set(true);
    this.doctorService.getPatientProfile(id!).subscribe({
      next: (data) => {
        this.patientData.set(data as PatientForDoctor);
        this.getPatientVisits(id);
      },
      error: () => {
        this.toastService.showError(
          this.translationService.translate('doctor.patientProfile.loadError'),
        );
        this.isLoading.set(false);
      },
    });
  }

  protected getDoctorName(record: MedicalHistoryRecord): string {
    if (!record.doctor) {
      return this.translationService.translate('patient.profile.noDoctor');
    }
    return `${record.doctor.doctorName} ${record.doctor.doctorSurname}`;
  }
  private getPatientVisits(id: string | null): void {
    if (!id) {
      this.isLoading.set(false);
      return;
    }

    this.doctorService.getPatientVisits(id).subscribe({
      next: (data) => {
        const visitsArray = Array.isArray(data) ? data : [];
        this.patientVisits.set(visitsArray as PatientVisit[]);
        this.isLoading.set(false);
      },
      error: () => {
        this.toastService.showError(
          this.translationService.translate(
            'doctor.patientProfile.visitsLoadError',
          ),
        );
        this.isLoading.set(false);
      },
    });
  }

  protected formatVisitDate(startTime: number[]): string {
    if (!startTime || startTime.length < 5) return '';
    const [year, month, day, hour, minute] = startTime;
    const date = new Date(year, month - 1, day, hour, minute);
    return date.toLocaleString('pl-PL', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  protected getVisitStatusSeverity(
    status: string,
  ): 'success' | 'info' | 'danger' | 'warning' {
    switch (status) {
      case 'Completed':
        return 'success';
      case 'Upcoming':
        return 'info';
      case 'Cancelled':
        return 'danger';
      default:
        return 'warning';
    }
  }

  protected getVisitStatusLabel(status: string): string {
    switch (status) {
      case 'Completed':
        return this.translationService.translate(
          'patient.visits.statusCompleted',
        );
      case 'Upcoming':
        return this.translationService.translate(
          'patient.visits.statusScheduled',
        );
      case 'Cancelled':
        return this.translationService.translate(
          'patient.visits.statusCanceled',
        );
      default:
        return status;
    }
  }
}
