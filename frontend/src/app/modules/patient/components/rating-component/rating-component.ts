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
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { Paginator } from 'primeng/paginator';
import { PanelModule } from 'primeng/panel';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { RatingModule } from 'primeng/rating';
import { Tooltip } from 'primeng/tooltip';
import { map } from 'rxjs';
import { FilterParams } from '../../../../core/models/filter.model';
import { CommentWithRating } from '../../../../core/models/review.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { AcceptActionDialogComponent } from '../../../shared/components/ui/accept-action-dialog/accept-action-dialog-component';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { ReviewVisitDialog } from '../review-visit-dialog/review-visit-dialog';

@Component({
  selector: 'app-rating-component',
  imports: [
    FormsModule,
    ButtonModule,
    PanelModule,
    RatingModule,
    FilterComponent,
    Paginator,
    Tooltip,
    ProgressSpinnerModule,
  ],
  templateUrl: './rating-component.html',
  styleUrl: './rating-component.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RatingComponent
  extends PaginatedComponentBase<CommentWithRating>
  implements OnInit
{
  protected translationService = inject(TranslationService);
  protected readonly comments = signal<CommentWithRating[]>([]);
  private commentService = inject(CommentService);
  private dialogService = inject(DialogService);
  private toastService = inject(ToastService);
  private destroyRef = inject(DestroyRef);
  private readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: '',
    dateFrom: null,
    dateTo: null,
    sortField: '',
    sortOrder: 'asc',
  });
  protected readonly isCommentLoading = signal(false);
  protected readonly isLoading = signal(false);

  ngOnInit(): void {
    this.isCommentLoading.set(true);

    this.initCommentsData();
  }

  private readonly filteredComments = computed(() => {
    const allComments = this.comments();
    const filter = this.filters();
    let filtered = allComments;

    if (!allComments) {
      return [];
    }

    if (filter.searchTerm) {
      const searchTermLower = filter.searchTerm.toLowerCase();
      filtered = filtered?.filter(
        (comment) =>
          comment.comment.toLowerCase().includes(searchTermLower) ||
          comment.doctorName.toLowerCase().includes(searchTermLower) ||
          comment.institutionName.toLowerCase().includes(searchTermLower),
      );
    }

    // if (filter.dateFrom) {
    //   // Assuming comments have a date property of type Date
    //   filtered = filtered?.filter(
    //     (comment: CommentWithRating) => new Date(comment.) >= new Date(filter.dateFrom!),
    //   );
    // }
    //TODO add date to comments and uncomment filtering by date

    filtered = filtered?.sort((a, b) => {
      if (filter.sortOrder === 'asc') {
        return a.doctorName.localeCompare(b.doctorName);
      } else {
        return b.doctorName.localeCompare(a.doctorName);
      }
    });
    return filtered;
  });

  protected override get sourceData() {
    return this.filteredComments();
  }

  protected readonly paginatedComments = computed(() => {
    return this.paginatedData();
  });

  protected viewRatings(comment: CommentWithRating): void {
    const ref = this.dialogService.open(ReviewVisitDialog, {
      width: window.innerWidth < 768 ? '100%' : '70%',
      height: 'auto',
      data: comment,
    });
    if (!ref) {
      return;
    }
    ref.onClose.subscribe((editedComment) => {
      if (!editedComment) {
        return;
      }
      const comment: Partial<CommentWithRating> = {
        id: editedComment.commentId,
        comment: editedComment.comments,
        doctorRating: editedComment.doctorRating,
        institutionRating: editedComment.institutionRating,
      };
      this.isLoading.set(true);
      this.commentService
        .editComment(comment)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.toastService.showSuccess(
              this.translationService.translate('comment.edit.success'),
            );
            this.isLoading.set(false);

            this.comments.update((val) => {
              return val.map((v) =>
                v.id === comment.id
                  ? {
                      ...v,
                      comment: comment.comment ?? v.comment,
                      institutionRating:
                        comment.institutionRating ?? v.institutionRating,
                      doctorRating: comment.doctorRating ?? v.doctorRating,
                    }
                  : v,
              );
            });
          },
          error: () => {
            this.toastService.showError(
              this.translationService.translate('comments.edit.failed'),
            );
            this.isLoading.set(false);
          },
        });
    });
  }

  protected initCommentsData() {
    this.isCommentLoading.set(true);
    this.commentService
      .getUsersComments()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((response) =>
          response.comments.map((comment) => ({
            id: comment.id,
            comment: comment.content,
            doctorName: comment.doctor,
            institutionName: comment.institution,
            doctorRating: comment.doctorRating,
            institutionRating: comment.institutionRating,
          })),
        ),
      )
      .subscribe((comments) => {
        this.comments.set(comments);
        this.isCommentLoading.set(false);
      });
  }

  protected onFiltersChange(filter: FilterParams): void {
    this.filters.set(filter);
  }
  protected deleteComment(comment: CommentWithRating) {
    const ref = this.dialogService.open(AcceptActionDialogComponent, {
      width: '400px',
      height: 'auto',
      closable: true,
      header: this.translationService.translate('comment.delete.title'),
      data: {
        title: this.translationService.translate('comment.delete.title'),
        message: this.translationService.translate('comment.delete.message'),
      },
    });
    if (!ref) {
      return;
    }
    ref.onClose.subscribe((accept) => {
      if (accept) {
        this.confirmDelete(comment);
      }
    });
  }

  protected confirmDelete(comment: CommentWithRating) {
    this.isLoading.set(true);
    this.commentService
      .deleteComment(comment.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate('comment.deleted.success'),
          );
          this.comments.update((comments) =>
            comments.filter((com) => com.id !== comment.id),
          );
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
          this.toastService.showError(
            this.translationService.translate('comment.deleted.error'),
          );
        },
      });
  }
}
