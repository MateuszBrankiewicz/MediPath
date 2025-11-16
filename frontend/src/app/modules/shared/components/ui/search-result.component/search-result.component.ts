import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DataViewModule } from 'primeng/dataview';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { DoctorService } from '../../../../../core/services/doctor/doctor.service';
import { ToastService } from '../../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../../core/services/translation/translation.service';
import { groupSchedulesByDate } from '../../../../../utils/scheduleMapper';
import { ScheduleVisitDialog } from '../../../../patient/components/schedule-visit-dialog/schedule-visit-dialog';

import { HttpErrorResponse } from '@angular/common/http';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import {
  ScheduleByInstitutionResponse,
  ScheduleItem,
} from '../../../../../core/models/schedule.model';
import { InstitutionObject } from '../../../../../core/models/visit.model';
import { VisitsService } from '../../../../../core/services/visits/visits.service';
import { DoctorCardComponent } from './components/doctor-card.component/doctor-card.component';
import {
  Hospital,
  HospitalCardComponent,
} from './components/hospital-card.component/hospital-card.component';
import { AddressChange, BookAppointment, Doctor } from './search-result.model';
import {
  SearchQuery,
  SearchResponse,
  SearchService,
} from './services/search.service';

@Component({
  selector: 'app-search-result.component',
  imports: [
    DataViewModule,
    ButtonModule,
    HospitalCardComponent,
    DoctorCardComponent,
    ProgressSpinnerModule,
  ],
  providers: [DialogService],
  templateUrl: './search-result.component.html',
  styleUrl: './search-result.component.scss',
})
export class SearchResultComponent implements OnInit {
  private searchService = inject(SearchService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private readonly dialogService = inject(DialogService);
  private destroyRef = inject(DestroyRef);
  private dialogRef: DynamicDialogRef | null = null;
  private patientVisitsService = inject(VisitsService);
  private toastService = inject(ToastService);
  private readonly translationService = inject(TranslationService);
  protected readonly category = signal('');
  private doctorService = inject(DoctorService);
  protected isNewScheduleLoaded = signal({ isLoading: false, cardId: '' });
  protected isLoading = signal(false);

  protected readonly values = signal<SearchResponse | null>(null);

  protected readonly hospitals = computed(() => {
    const results = this.values();
    if (this.category() === 'institution' && results?.result) {
      return results.result as Hospital[];
    }
    return [];
  });

  protected readonly doctors = computed(() => {
    const results = this.values();
    if (this.category() === 'doctor' && results?.result) {
      return results.result as Doctor[];
    }
    return [];
  });

  ngOnInit(): void {
    this.requestSearchResult();
  }

  protected performSearch(params: SearchQuery): void {
    this.isLoading.set(true);
    this.searchService
      .search(params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((results) => {
        this.values.set(results);
        this.isLoading.set(false);
      });
  }

  protected onBookAppointment(event: BookAppointment): void {
    this.dialogRef = this.dialogService.open(ScheduleVisitDialog, {
      data: {
        availableTerms: event.doctor.schedule,
        event,
      },
      header: 'Book Appointment',
      width: '70%',
      closable: true,
      modal: true,
      styleClass: 'schedule-visit-dialog',
    });
    if (!this.dialogRef) {
      return;
    }
    this.dialogRef.onClose.subscribe((result) => {
      if (result) {
        this.patientVisitsService
          .scheduleVisit({
            scheduleID: result.slotId,
            patientRemarks: result.remarks,
          })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.toastService.showSuccess(
                this.translationService.translate(
                  'patient.appointment.bookSuccess',
                ),
              );
              this.requestSearchResult();
            },
            error: (error: HttpErrorResponse) => {
              if (error.status === 409) {
                this.toastService.showError('patient.doctor.conflict');
                return;
              }
              this.toastService.showError('patient.appointment.bookError');
            },
          });
      }
    });
  }

  protected onShowMoreInfo(doctor: Doctor): void {
    this.router.navigate(['/patient/doctor', doctor.id]);
  }

  protected onAddressChange(event: AddressChange): void {
    const institutionId = this.extractInstitutionId(event);
    this.isNewScheduleLoaded.set({ isLoading: true, cardId: event.doctor.id });
    this.doctorService
      .getDoctorScheduleByInstitution(institutionId, event.doctor.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((schedules: ScheduleByInstitutionResponse) => {
        this.updateDoctorSchedule(event.doctor.id, schedules);
        this.isNewScheduleLoaded.set({ isLoading: false, cardId: '' });
      });
  }

  private extractInstitutionId(event: AddressChange): string {
    const institution = event.doctor.addresses[event.addressIndex]
      .institution as unknown as InstitutionObject;
    return institution.institutionId;
  }

  private updateDoctorSchedule(
    doctorId: string,
    schedules: ScheduleByInstitutionResponse,
  ): void {
    const currentValues = this.values();
    if (!currentValues) return;

    const doctor = this.findDoctorInResults(currentValues.result, doctorId);
    if (!doctor) return;

    const groupedSchedule = this.mapAndGroupSchedules(schedules.schedules);
    doctor.schedule = groupedSchedule;

    this.updateSearchResults(doctorId, doctor);
  }

  private findDoctorInResults(
    results: (Doctor | Hospital)[],
    doctorId: string,
  ): Doctor | null {
    const found = results.find(
      (res) => res.id === doctorId && this.isDoctor(res),
    );
    return found && this.isDoctor(found) ? found : null;
  }

  private mapAndGroupSchedules(
    schedules: ScheduleByInstitutionResponse['schedules'],
  ): ReturnType<typeof groupSchedulesByDate> {
    const mappedSchedules: ScheduleItem[] = schedules.map((schedule) => ({
      id: schedule.id,
      startTime: schedule.startHour,
      isBooked: schedule.isBooked,
    }));
    return groupSchedulesByDate(mappedSchedules);
  }

  private updateSearchResults(doctorId: string, updatedDoctor: Doctor): void {
    this.values.update((val) => {
      if (!val || this.category() !== 'doctor') return val;

      return {
        ...val,
        result: (val.result as Doctor[]).map((doctor) =>
          doctor.id === doctorId ? updatedDoctor : doctor,
        ),
      };
    });
  }

  private isDoctor(obj: Doctor | Hospital): obj is Doctor {
    return 'schedule' in obj;
  }

  private requestSearchResult(): void {
    this.route.queryParams.subscribe((params) => {
      const query = params['query'] || '';
      const category = params['category'] || '';
      const location = params['location'] || '';
      const specialization = params['specialization'] || '';
      this.category.set(category);
      this.performSearch({ query, category, location, specialization });
    });
  }

  protected onInstitutionClicked(hospital: Hospital): void {
    this.router.navigate(['/patient/institution', hospital.id]);
  }

  protected getDataForInstitutionCard = computed<Hospital[]>(() => {
    const institution = this.hospitals();
    return institution.map((inst) => ({
      id: inst?.id ?? '',
      name: inst?.name ?? '',
      address: inst?.address ?? '',
      specialisation: inst?.specialisation ?? [],
      isPublic: inst?.isPublic ?? false,
      imageUrl: inst.image ?? '',
    }));
  });
}
