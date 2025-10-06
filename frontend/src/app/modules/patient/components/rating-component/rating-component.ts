import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { Paginator, PaginatorState } from 'primeng/paginator';
import { PanelModule } from 'primeng/panel';
import { RatingModule } from 'primeng/rating';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { FilterParams } from '../../models/filter.model';
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
  imports: [
    FormsModule,
    ButtonModule,
    PanelModule,
    RatingModule,
    FilterComponent,
    Paginator,
  ],
  templateUrl: './rating-component.html',
  styleUrl: './rating-component.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RatingComponent {
  protected translationService = inject(TranslationService);
  private dialogService = inject(DialogService);
  protected readonly first = signal(0);
  protected readonly rows = signal(10);
  private readonly filters = signal<FilterParams>({
    searchTerm: '',
    status: '',
    dateFrom: null,
    dateTo: null,
    sortField: '',
    sortOrder: 'asc',
  });

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

  protected readonly paginatedComments = computed(() => {
    const start = this.first();
    const end = start + this.rows();
    return this.filteredComments()?.slice(start, end);
  });

  protected onPageChange(event: PaginatorState): void {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
  }

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

  protected onFiltersChange(filter: FilterParams): void {
    this.filters.set(filter);
  }
}
