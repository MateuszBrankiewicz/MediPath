import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DataViewModule } from 'primeng/dataview';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ToastService } from '../../../../../core/services/toast/toast.service';
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
  private readonly dialogService = inject(DialogService);
  private destroyRef = inject(DestroyRef);
  private dialogRef: DynamicDialogRef | null = null;
  private patientVisitsService = inject(PatientVisitsService);
  private toastService = inject(ToastService);
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
    this.route.queryParams.subscribe((params) => {
      const query = params['query'] || '';
      const category = params['category'] || '';
      const location = params['location'] || '';
      const specialization = params['specialization'] || '';
      this.category.set(category);
      this.performSearch({ query, category, location, specialization });
    });
  }

  protected performSearch(params: SearchQuery): void {
    this.searchService
      .search(params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((results) => {
        console.log('Search results:', results);
        this.values.set(results);
      });
  }

  onEditHospital(hospital: Hospital): void {
    console.log('Edit hospital:', hospital);
  }

  onDisableHospital(hospital: Hospital): void {
    console.log('Disable hospital:', hospital);
  }

  onBookAppointment(event: BookAppointment): void {
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
          .scheduleVisit(result.slotId, result.patientRemarks)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.toastService.showSuccess('Appointment booked successfully');
            },
            error: (err) => {
              console.error('Error booking appointment:', err);
            },
          });
      }
    });
  }

  onShowMoreInfo(doctor: Doctor): void {
    console.log('Show more info for:', doctor);
  }

  onAddressChange(event: AddressChange): void {
    console.log('Address changed:', event);
  }
}
