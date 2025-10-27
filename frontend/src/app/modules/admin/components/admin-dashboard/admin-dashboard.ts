import { CommonModule } from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ProgressSpinner } from 'primeng/progressspinner';
import { CommentService } from '../../../../core/services/comment/comment.service';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InstitutionStoreService } from './../../services/institution/institution-store.service';
import { CommentItem, CommentsCard } from './widgets/comments-card';
import {
  InstitutionOption,
  InstitutionSelectCard,
} from './widgets/institution-select-card';
import {
  UpcomingVisitItem,
  UpcomingVisitsCard,
} from './widgets/upcoming-visits-card';

@Component({
  selector: 'app-admin-dashboard',
  imports: [
    CommonModule,
    InstitutionSelectCard,
    CommentsCard,
    UpcomingVisitsCard,
    ProgressSpinner,
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard {
  protected translationService = inject(TranslationService);
  private institutionStoreService = inject(InstitutionStoreService);
  private commentsService = inject(CommentService);
  private institutionService = inject(InstitutionService);
  private destroyRef = inject(DestroyRef);
  protected readonly institutions = computed(() =>
    this.institutionStoreService.institutionOptions(),
  );
  protected readonly isInstitutionLoading = signal<boolean>(false);
  protected readonly isCommentsLoading = signal<boolean>(false);
  protected readonly isUpcomingVisitsLoading = signal<boolean>(false);
  protected readonly comments = signal<CommentItem[]>([]);

  protected readonly upcomingVisits = signal<UpcomingVisitItem[]>([]);
  protected readonly isLoading = computed(() => {
    return (
      this.institutionStoreService.isInstitutionLoading() ||
      this.isCommentsLoading() ||
      this.isUpcomingVisitsLoading()
    );
  });

  constructor() {
    effect(() => {
      const institutions = this.institutionStoreService.selectedInstitution();

      if (!institutions) {
        this.onInstitutionChanged(this.institutions()[0] || null);
        return;
      }
      this.onInstitutionChanged(institutions);
    });
  }

  protected onInstitutionChanged(
    optionSelected: InstitutionOption | null,
  ): void {
    if (!optionSelected) {
      return;
    }
    this.institutionStoreService.setInstitution(optionSelected);
    this.loadCommentsForInstitution(optionSelected.id);
    this.loadUpcomingVisits();
  }

  protected onChangeDoctor(_visit: UpcomingVisitItem): void {
    console.log('Change doctor clicked', _visit);
  }

  protected onCancelVisit(_visit: UpcomingVisitItem): void {
    console.log('Cancel visit clicked', _visit);
  }

  selectedInstitution = computed(() =>
    this.institutionStoreService.selectedInstitution(),
  );

  private loadCommentsForInstitution(institutionId: string): void {
    this.isCommentsLoading.set(true);
    this.commentsService
      .getCommentByInstitution(institutionId)
      .subscribe((comments) => {
        const formattedComments = comments.map((comment) => ({
          id: comment.id,
          content: comment.content,
        }));
        this.comments.set(formattedComments);
        this.isCommentsLoading.set(false);
      });
  }

  private loadInstitutions(): void {
    this.isInstitutionLoading.set(true);
    this.institutionService
      .getInstitutionsForAdmin()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((institutions) => {
        const formattedInstitutions = institutions.map((inst) => ({
          id: inst.id,
          name: inst.name,
        }));
        this.institutionStoreService.setInstitution(formattedInstitutions[0]);

        this.loadUpcomingVisits();
        this.loadCommentsForInstitution(formattedInstitutions[0].id);
        this.isInstitutionLoading.set(false);
      });
  }

  private loadUpcomingVisits(): void {
    this.isUpcomingVisitsLoading.set(true);
    this.institutionService
      .getUpcomingVisitsForInstitution(this.selectedInstitution()?.id || '')
      .subscribe((visits) => {
        this.upcomingVisits.set(
          visits.sort((a, b) => a.date.localeCompare(b.date)).slice(0, 5),
        );
        this.isUpcomingVisitsLoading.set(false);
      });
  }
}
