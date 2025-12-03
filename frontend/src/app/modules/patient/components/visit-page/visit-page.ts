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
import { DynamicDialogRef } from 'primeng/dynamicdialog';
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
import { VisitDialogService } from '../../services/visit-dialog.service';
import { ReviewVisitDialogResult } from '../review-visit-dialog/review-visit-dialog';

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
  private visitDialogService = inject(VisitDialogService);
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

  protected cancelVisit(visitId: string) {
    this.visitDialogService.openAcceptActionDialog().subscribe((res) => {
      if (!res) {
        return;
      }
      this.visitService
        .cancelVisit(visitId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.toastService.showSuccess('Visit caneled');
          },
          error: () => {
            this.toastService.showError(
              'Error occurs, please try again later.',
            );
          },
        });
    });
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

    this.ref = this.visitDialogService.openVisitDetailsDialog({
      visitId: id,
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((result) => {
      if (result?.action === 'REVIEW') {
        this.openReviewDialog(
          id,
          result.commentId,
          result.doctorName,
          result.institutionName,
        );
      }
    });
  }

  protected openRescheduleDialog(visitId: string): void {
    this.ref = this.visitDialogService.openRescheduleDialog({
      visitId: visitId,
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((result) => {
      if (result) {
        console.log('Reschedule result:', result);
        this.visitService
          .rescheduleVisit(
            {
              scheduleID: result.slotId,
              patientRemarks: result.remarks,
            },
            visitId,
          )
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
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

  private openReviewDialog(
    id: string,
    commentId?: string,
    doctorName?: string,
    institutionName?: string,
  ): void {
    const visit = this.visits().find((v) => v.id === id);
    this.ref = this.visitDialogService.openReviewDialog({
      visitId: id,
      commentId: commentId,
      doctorName: doctorName ?? visit?.doctorName,
      institutionName: institutionName ?? visit?.institution,
    });

    if (!this.ref) {
      return;
    }

    this.ref.onClose.subscribe((comment) => {
      this.handleReviewDialogResult(comment);
    });
  }

  private handleReviewDialogResult(
    comment: ReviewVisitDialogResult | undefined,
  ): void {
    if (!comment) {
      return;
    }

    if (comment.isEditing && comment.commentId) {
      this.handleEditComment(comment);
    } else {
      this.handleAddComment(comment);
    }
  }

  private handleEditComment(comment: ReviewVisitDialogResult): void {
    const editedComment = {
      id: comment.commentId!,
      comment: comment.comments,
      doctorRating: comment.doctorRating ?? 0,
      institutionRating: comment.institutionRating ?? 0,
    };
    this.commentService
      .editComment(editedComment)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccessMessage('comment.edit.success');
          this.initVisitList();
        },
        error: () => {
          this.showErrorMessage('comments.edit.failed');
        },
      });
  }

  private handleAddComment(comment: ReviewVisitDialogResult): void {
    const newComment: AddComentRequest = {
      comment: comment.comments,
      visitID: comment.visitId!,
      doctorRating: String(comment.doctorRating ?? 0),
      institutionRating: String(comment.institutionRating ?? 0),
    };

    this.commentService
      .addComment(newComment)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.showSuccessMessage('comment.add.success');
          this.initVisitList();
        },
        error: () => {
          this.showErrorMessage('comment.add.error');
        },
      });
  }

  private showSuccessMessage(translationKey: string): void {
    this.toastService.showSuccess(
      this.translationService.translate(translationKey),
    );
  }

  private showErrorMessage(translationKey: string): void {
    this.toastService.showError(
      this.translationService.translate(translationKey),
    );
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
            doctorName: `${visit.doctor.doctorName} ${visit.doctor.doctorSurname}`,
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
