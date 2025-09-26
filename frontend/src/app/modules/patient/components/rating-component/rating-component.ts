import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { PanelModule } from 'primeng/panel';
import { RatingModule } from 'primeng/rating';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { PatientCommentService } from '../../services/patient-comment.service';
import { ReviewVisitDialog } from '../review-visit-dialog/review-visit-dialog';

export interface CommentWithRating {
  id: string;
  comment: string;
  doctorName: string;
  institutionName: string;
  doctorRating: number;
  institutionRating: number;
}

@Component({
  selector: 'app-rating-component',
  imports: [FormsModule, ButtonModule, PanelModule, RatingModule],
  templateUrl: './rating-component.html',
  styleUrl: './rating-component.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RatingComponent {
  protected translationService = inject(TranslationService);
  private dialogService = inject(DialogService);

  protected viewRatings(comment: CommentWithRating): void {
    this.dialogService.open(ReviewVisitDialog, {
      width: '70%',
      height: 'auto',
      data: comment,
    });
  }
  private commentService = inject(PatientCommentService);

  protected readonly comments = toSignal<CommentWithRating[]>(
    this.commentService.getUsersComments().pipe(
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
    ),
  );
}
