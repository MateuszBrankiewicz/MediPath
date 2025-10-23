import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { MenuModule } from 'primeng/menu';
import { PaginatorModule } from 'primeng/paginator';
import { PopoverModule } from 'primeng/popover';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TableModule } from 'primeng/table';
import { AddComentRequest } from '../../../../core/models/review.model';
import {
  VisitPageModel,
  VisitResponseArray,
  VisitStatus,
} from '../../../../core/models/visit.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import {
  FilterFieldConfig,
  FilteringService,
} from '../../../../core/services/filtering/filtering.service';
import {
  SortFieldConfig,
  SortingService,
} from '../../../../core/services/sorting/sorting.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { ReviewVisitDialog } from '../review-visit-dialog/review-visit-dialog';
import { ScheduleVisitDialog } from '../schedule-visit-dialog/schedule-visit-dialog';
import { VisitDetailsDialog } from '../visit-details-dialog/visit-details-dialog';

@Component({
  selector: 'app-visit-page',
  imports: [
    TableModule,
    CommonModule,
    MenuModule,
    ButtonModule,
    PopoverModule,
    CardModule,
    PaginatorModule,
    FilterComponent,
    ProgressSpinnerModule,
  ],
  providers: [DialogService],
  templateUrl: './visit-page.html',
  styleUrl: './visit-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisitPage
  extends PaginatedComponentBase<VisitPageModel>
  implements OnInit
{
  protected readonly showVisitDetailsDialog = signal(false);
  protected readonly selectedVisitId = signal<string | null>(null);
  private commentService = inject(CommentService);
  private visitService = inject(VisitsService);
  private dialogService = inject(DialogService);
  protected translationService = inject(TranslationService);
  private destroyRef = inject(DestroyRef);
  private ref: DynamicDialogRef | null = null;
  private toastService = inject(ToastService);
  private sortingService = inject(SortingService);
  private filteringService = inject(FilteringService);

  protected readonly isVisitLoading = signal(false);
  protected readonly isLoading = signal(false);

  private readonly visitFilterConfig: FilterFieldConfig<VisitPageModel> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<VisitPageModel>(
        (v) => v.doctorName,
        (v) => v.institution,
        (v) => String(v.status),
      ),
      this.filteringService.statusConfig<VisitPageModel>((v) => v.status),
      this.filteringService.dateRangeConfig<VisitPageModel>((v) => v.date),
    );

  private readonly visitSortConfig: SortFieldConfig<VisitPageModel>[] = [
    this.sortingService.dateField('date', (v) => v.date),
    this.sortingService.stringField('doctorName', (v) => v.doctorName),
    this.sortingService.stringField('institution', (v) => v.institution),
    this.sortingService.stringField('status', (v) => String(v.status)),
  ];

  protected readonly filters = signal<{
    searchTerm: string;
    status: string;
    dateFrom: Date | null;
    dateTo: Date | null;
    sortField: string;
    sortOrder: 'asc' | 'desc';
  }>({
    searchTerm: '',
    status: 'all',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  });

  protected override readonly totalRecords = computed(
    () => this.sortedVisits().length,
  );

  protected readonly visits = signal<VisitPageModel[]>([]);

  ngOnInit(): void {
    this.initVisitList();
  }

  protected cancelVisit() {
    console.log('cancel');
  }

  protected getStatusTranslation(status: VisitStatus): string {
    switch (status) {
      case VisitStatus.Scheduled:
        return this.translationService.translate(
          'patient.visits.statusScheduled',
        );
      case VisitStatus.Completed:
        return this.translationService.translate(
          'patient.visits.statusCompleted',
        );
      case VisitStatus.Canceled:
        return this.translationService.translate(
          'patient.visits.statusCanceled',
        );
      default:
        return status;
    }
  }

  protected editVisit() {
    console.log('edit');
  }

  protected readonly filteredVisits = computed(() => {
    const filters = this.filters();
    return this.filteringService.filter(
      this.visits(),
      {
        searchTerm: filters.searchTerm,
        status: filters.status,
        dateFrom: filters.dateFrom,
        dateTo: filters.dateTo,
      },
      this.visitFilterConfig,
    );
  });

  protected readonly sortedVisits = computed(() => {
    const { sortField, sortOrder } = this.filters();
    return this.sortingService.sort(
      this.filteredVisits(),
      sortField,
      sortOrder,
      this.visitSortConfig,
    );
  });

  protected override get sourceData() {
    return this.sortedVisits();
  }

  protected readonly paginatedVisits = computed(() => {
    return this.paginatedData();
  });

  protected onFiltersChange(ev: {
    searchTerm: string;
    status: string;
    dateFrom: Date | null;
    dateTo: Date | null;
    sortField: string;
    sortOrder: 'asc' | 'desc';
  }): void {
    this.filters.set(ev);
    this.first.set(0);
  }

  protected openVisitDialog(id: string): void {
    this.selectedVisitId.set(id);

    this.ref = this.dialogService.open(VisitDetailsDialog, {
      data: { visitId: id },
      header: this.translationService.translate(
        'patient.visits.visitDetailsTitle',
      ),
      width: '80%',
      closable: true,
      modal: true,
      styleClass: 'visit-dialog',
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((result) => {
      if (result === 'REVIEW') {
        this.openReviewDialog(id);
      }
    });
  }

  protected openRescheduleDialog(visitId: string): void {
    this.ref = this.dialogService.open(ScheduleVisitDialog, {
      data: { visitId: visitId },
      header: this.translationService.translate(
        'patient.visits.rescheduleTitle',
      ),
      width: '70%',
      height: 'auto',
      closable: true,
      modal: true,
      styleClass: 'reschedule-dialog',
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((result) => {
      if (result) {
        this.visitService
          .rescheduleVisit(
            {
              scheduleID: result.slotId,
              patientRemarks: result.patientRemarks,
            },
            visitId,
          )
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (result) => {
              console.log(result);
              this.toastService.showSuccess(
                this.translationService.translate(
                  'patient.appointment.bookSuccess',
                ),
              );
            },
            error: (error: unknown) => {
              this.toastService.showError(
                this.translationService.translate(
                  'patient.appointment.bookError',
                ),
              );
              console.error('Failed to reschedule visit:', error);
            },
          });
      }
    });
  }

  private openReviewDialog(id: string): void {
    this.ref = this.dialogService.open(ReviewVisitDialog, {
      data: { visitId: id },
      header: this.translationService.translate('patient.visits.reviewTitle'),
      width: '70%',
      height: 'auto',
      modal: true,
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((comment) => {
      const newComment: AddComentRequest = {
        comment: comment.comments,
        visitID: comment.visitId,
        doctorRating: comment.doctorRating,
        institutionRating: comment.institutionRating,
      };
      this.commentService
        .addComment(newComment)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.toastService.showSuccess(
              this.translationService.translate('comment.add.succes'),
            );
          },
          error: (err) => {
            console.log(err);
            this.toastService.showError(
              this.translationService.translate('comment.add.error'),
            );
          },
        });
    });
  }

  private parseVisitStatus(status: string): VisitStatus {
    switch (status.toLowerCase()) {
      case 'upcoming':
        return VisitStatus.Scheduled;
      case 'completed':
        return VisitStatus.Completed;
      case 'cancelled':
        return VisitStatus.Canceled;
      default:
        return VisitStatus.Scheduled;
    }
  }

  private initVisitList(): void {
    this.isVisitLoading.set(true);
    this.visitService
      .getAllVisits()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (visits: VisitResponseArray) => {
          const visitList: VisitPageModel[] = visits.map((visit) => ({
            id: visit.id,
            date: new Date(visit.time.startTime),
            doctorName: visit.doctor.doctorName,
            institution: visit.institution.institutionName,
            status: this.parseVisitStatus(visit.status),
          }));
          this.visits.set(visitList);
          this.isVisitLoading.set(false);
        },
        error: (error: unknown) => {
          console.error('Failed to fetch visits:', error);
          this.isVisitLoading.set(false);
        },
      });
  }

  protected checkIfDisablePopover(status: string): boolean {
    return (
      status.toLowerCase() === 'completed' ||
      status.toLowerCase() === 'canceled'
    );
  }

  protected checkIfDisableDetailsButton(status: string): boolean {
    return (
      status.toLowerCase() === 'canceled' ||
      status.toLowerCase() === 'scheduled'
    );
  }
}
