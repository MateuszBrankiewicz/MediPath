import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SelectModule } from 'primeng/select';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { StarRatingOption } from '../../models/review-page.model';

@Component({
  selector: 'app-review-visit-dialog',
  imports: [SelectModule, ButtonModule, FormsModule],
  templateUrl: './review-visit-dialog.html',
  styleUrl: './review-visit-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReviewVisitDialog {
  private ref = inject(DynamicDialogRef<ReviewVisitDialogResult | undefined>);
  private config = inject(DynamicDialogConfig<ReviewVisitDialogData>);
  protected translationService = inject(TranslationService);

  private readonly dialogData = this.config.data ?? {};
  private readonly existingComment = this.extractExistingComment();

  private readonly commentsSignal = signal<string>(
    this.existingComment?.comment ?? '',
  );
  private readonly doctorRatingSignal = signal<number | null>(
    this.existingComment?.doctorRating ?? null,
  );
  private readonly institutionRatingSignal = signal<number | null>(
    this.existingComment?.institutionRating ?? null,
  );

  protected readonly isEditing = computed(() => !!this.existingComment?.id);

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
    () => this.dialogData.doctorName || this.existingComment?.doctorName || '',
  );

  protected readonly institutionName = computed(
    () =>
      this.dialogData.institutionName ||
      this.existingComment?.institutionName ||
      '',
  );

  public onSubmit() {
    const reviewData: ReviewVisitDialogResult = {
      visitId: this.dialogData.visitId,
      commentId: this.existingComment?.id,
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
