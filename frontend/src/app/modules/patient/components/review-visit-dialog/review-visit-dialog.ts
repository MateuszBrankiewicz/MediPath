import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SelectModule } from 'primeng/select';
import { catchError, of } from 'rxjs';
import { StarRatingOption } from '../../../../core/models/review.model';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-review-visit-dialog',
  imports: [SelectModule, ButtonModule, FormsModule],
  templateUrl: './review-visit-dialog.html',
  styleUrl: './review-visit-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReviewVisitDialog implements OnInit {
  private ref = inject(DynamicDialogRef<ReviewVisitDialogResult | undefined>);
  private config = inject(DynamicDialogConfig<ReviewVisitDialogData>);
  private commentService = inject(CommentService);
  private messageService = inject(MessageService);
  protected translationService = inject(TranslationService);

  private readonly dialogData = this.config.data ?? {};
  private readonly existingComment = signal<ExistingComment | null>(
    this.extractExistingComment(),
  );
  protected readonly isLoading = signal<boolean>(false);

  private readonly commentsSignal = signal<string>(
    this.existingComment()?.comment ?? '',
  );
  private readonly doctorRatingSignal = signal<number | null>(
    this.existingComment()?.doctorRating ?? null,
  );
  private readonly institutionRatingSignal = signal<number | null>(
    this.existingComment()?.institutionRating ?? null,
  );

  protected readonly isEditing = computed(() => !!this.existingComment()?.id);

  get commentsModel() {
    return this.commentsSignal();
  }

  set commentsModel(value: string) {
    this.commentsSignal.set(value);
  }

  get doctorRatingModel() {
    return this.doctorRatingSignal();
  }

  set doctorRatingModel(value: number | null) {
    this.doctorRatingSignal.set(value);
  }

  get institutionRatingModel() {
    return this.institutionRatingSignal();
  }

  set institutionRatingModel(value: number | null) {
    this.institutionRatingSignal.set(value);
  }

  readonly starRatingOptions: StarRatingOption[] = [
    { label: '1 Star', value: 1, stars: '★☆☆☆☆' },
    { label: '2 Stars', value: 2, stars: '★★☆☆☆' },
    { label: '3 Stars', value: 3, stars: '★★★☆☆' },
    { label: '4 Stars', value: 4, stars: '★★★★☆' },
    { label: '5 Stars', value: 5, stars: '★★★★★' },
  ];

  protected readonly doctorName = computed(
    () =>
      this.dialogData.doctorName || this.existingComment()?.doctorName || '',
  );

  protected readonly institutionName = computed(
    () =>
      this.dialogData.institutionName ||
      this.existingComment()?.institutionName ||
      '',
  );

  ngOnInit(): void {
    if (this.dialogData.id) {
      this.loadCommentById(this.dialogData.id);
    }
  }

  private loadCommentById(commentId: string): void {
    this.isLoading.set(true);
    this.commentService
      .getUsersComments()
      .pipe(
        catchError(() => {
          this.messageService.add({
            severity: 'error',
            summary: this.translationService.translate('shared.error'),
            detail: this.translationService.translate(
              'patient.reviewDialog.loadError',
            ),
          });
          this.isLoading.set(false);
          return of({ comments: [] });
        }),
      )
      .subscribe({
        next: (response) => {
          const comment = response.comments.find((c) => c.id === commentId);

          if (comment) {
            const existingComment: ExistingComment = {
              id: comment.id,
              comment: comment.content,
              doctorRating: comment.doctorRating,
              institutionRating: comment.institutionRating,
              doctorName: comment.doctor,
              institutionName: comment.institution,
            };

            this.existingComment.set(existingComment);
            this.commentsSignal.set(comment.content);
            this.doctorRatingSignal.set(comment.doctorRating);
            this.institutionRatingSignal.set(comment.institutionRating);
          } else {
            this.messageService.add({
              severity: 'warn',
              summary: this.translationService.translate('shared.notFound'),
              detail: this.translationService.translate(
                'patient.reviewDialog.commentNotFound',
              ),
            });
          }

          this.isLoading.set(false);
        },
      });
  }

  public onSubmit() {
    const reviewData: ReviewVisitDialogResult = {
      visitId: this.dialogData.visitId,
      commentId: this.existingComment()?.id,
      comments: this.commentsSignal().trim(),
      doctorRating: this.doctorRatingSignal(),
      institutionRating: this.institutionRatingSignal(),
      isEditing: this.isEditing(),
    };
    this.ref.close(reviewData);
  }

  public onCancel() {
    this.ref.close();
  }

  private extractExistingComment(): ExistingComment | null {
    const data = this.dialogData;
    if (!data) {
      return null;
    }

    // Jeśli mamy tylko id bez danych, zwróć tylko id - dane zostaną załadowane później
    if (data.id && !data.comment && !data.content) {
      return {
        id: data.id,
        comment: '',
        doctorRating: null,
        institutionRating: null,
      };
    }

    const commentText = this.resolveCommentText(data);
    if (!commentText) {
      return null;
    }

    return {
      id: typeof data.id === 'string' ? data.id : '',
      comment: commentText,
      doctorRating: this.normalizeRating(data.doctorRating),
      institutionRating: this.normalizeRating(data.institutionRating),
      doctorName:
        typeof (data as ReviewVisitDialogData).doctorName === 'string'
          ? (data as ReviewVisitDialogData).doctorName
          : undefined,
      institutionName:
        typeof (data as ReviewVisitDialogData).institutionName === 'string'
          ? (data as ReviewVisitDialogData).institutionName
          : undefined,
    };
  }

  private resolveCommentText(data: ReviewVisitDialogData): string | undefined {
    if (typeof data.comment === 'string') {
      return data.comment;
    }
    if (typeof (data as CommentContentShape).content === 'string') {
      return (data as CommentContentShape).content;
    }
    return undefined;
  }

  private normalizeRating(value?: number): number | null {
    if (typeof value !== 'number' || Number.isNaN(value)) {
      return null;
    }
    const floored = Math.floor(value);
    if (floored < 1) {
      return null;
    }
    return Math.min(floored, 5);
  }
}

interface CommentContentShape {
  content: string;
}

interface ExistingComment {
  id: string;
  comment: string;
  doctorRating: number | null;
  institutionRating: number | null;
  doctorName?: string;
  institutionName?: string;
}

export interface ReviewVisitDialogData {
  visitId?: string;
  id?: string;
  comment?: string;
  content?: string;
  doctorRating?: number;
  institutionRating?: number;
  doctorName?: string;
  institutionName?: string;
}

export interface ReviewVisitDialogResult {
  visitId?: string;
  commentId?: string;
  comments: string;
  doctorRating: number | null;
  institutionRating: number | null;
  isEditing: boolean;
}
