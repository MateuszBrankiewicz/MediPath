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
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { PopoverModule } from 'primeng/popover';
import { TableModule } from 'primeng/table';
import { AddComentRequest } from '../../../../core/models/review.model';
import {
  VisitPageModel,
  VisitResponseArray,
  VisitStatus,
} from '../../../../core/models/visit.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { ReviewVisitDialog } from '../review-visit-dialog/review-visit-dialog';
import { ScheduleVisitDialog } from '../schedule-visit-dialog/schedule-visit-dialog';
import { VisitDetailsDialog } from '../visit-details-dialog/visit-details-dialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

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
export class VisitPage implements OnInit {
  protected readonly showVisitDetailsDialog = signal(false);
  protected readonly selectedVisitId = signal<string | null>(null);
  private commentService = inject(CommentService);
  private visitService = inject(VisitsService);
  private dialogService = inject(DialogService);
  protected translationService = inject(TranslationService);
  private destroyRef = inject(DestroyRef);
  private ref: DynamicDialogRef | null = null;
  private toastService = inject(ToastService);

  protected readonly isVisitLoading = signal(false);
  protected readonly isLoading = signal(false);

  protected readonly first = signal(0);
  protected readonly rows = signal(10);
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

  protected onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
  }

  protected readonly totalRecords = computed(
    () => this.filteredVisits().length,
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
    const search = filters.searchTerm?.toLowerCase().trim() ?? '';
    const statusFilterRaw = (filters.status ?? 'all').toLowerCase();
    const statusFilter =
      statusFilterRaw === 'cancelled' ? 'canceled' : statusFilterRaw;
    const from = filters.dateFrom ? new Date(filters.dateFrom) : null;
    const to = filters.dateTo ? new Date(filters.dateTo) : null;

    return this.visits().filter((value) => {
      if (
        statusFilter !== 'all' &&
        value.status !== (statusFilter as VisitStatus)
      ) {
        return false;
      }
      if (from && value.date < from) return false;
      if (to && value.date > to) return false;
      if (search) {
        const hay = `${value.doctorName} ${value.institution} ${value.status}`
          .toLowerCase()
          .trim();
        if (!hay.includes(search)) return false;
      }
      return true;
    });
  });

  protected readonly sortedVisits = computed(() => {
    const list = this.filteredVisits().slice();
    const { sortField, sortOrder } = this.filters();
    const dir = sortOrder === 'asc' ? 1 : -1;
    list.sort((firstValue, secondValue) => {
      let aValue: number | string = 0;
      let bValue: number | string = 0;
      switch (sortField) {
        case 'doctorName':
          aValue = firstValue.doctorName?.toLowerCase() ?? '';
          bValue = secondValue.doctorName?.toLowerCase() ?? '';
          break;
        case 'institution':
          aValue = firstValue.institution?.toLowerCase() ?? '';
          bValue = secondValue.institution?.toLowerCase() ?? '';
          break;
        case 'status':
          aValue = String(firstValue.status).toLowerCase();
          bValue = String(secondValue.status).toLowerCase();
          break;
        case 'date':
        default:
          aValue = firstValue.date?.getTime?.() ?? 0;
          bValue = secondValue.date?.getTime?.() ?? 0;
      }
      if (aValue < bValue) return -1 * dir;
      if (aValue > bValue) return 1 * dir;
      return 0;
    });
    return list;
  });

  protected readonly paginatedVisits = computed(() => {
    const start = this.first();
    const end = start + this.rows();
    return this.sortedVisits().slice(start, end);
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
