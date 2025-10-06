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
import { ToastService } from '../../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../../core/services/translation/translation.service';
import { ScheduleVisitDialog } from '../../../../patient/components/schedule-visit-dialog/schedule-visit-dialog';
import { PatientVisitsService } from '../../../../patient/services/patient-visits.service';
import { BreadcumbComponent } from '../../breadcumb/breadcumb.component';
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
    BreadcumbComponent,
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
  private patientVisitsService = inject(PatientVisitsService);
  private toastService = inject(ToastService);
  private readonly translationService = inject(TranslationService);
  protected readonly category = signal('');

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
    this.searchService
      .search(params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((results) => {
        this.values.set(results);
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
            patientRemarks: result.patientRemarks,
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
            error: () => {
              this.toastService.showError(
                this.translationService.translate(
                  'patient.appointment.bookError',
                ),
              );
            },
          });
      }
    });
  }

  protected onShowMoreInfo(doctor: Doctor): void {
    this.router.navigate(['/patient/doctor', doctor.id]);
  }

  protected onAddressChange(event: AddressChange): void {
    console.log('Address changed:', event);
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
}
