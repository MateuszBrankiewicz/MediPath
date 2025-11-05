import { inject, Injectable } from '@angular/core';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';
import { TranslationService } from '../../../core/services/translation/translation.service';
import { ReviewVisitDialog } from '../components/review-visit-dialog/review-visit-dialog';
import { ScheduleVisitDialog } from '../components/schedule-visit-dialog/schedule-visit-dialog';
import {
  VisitDetailsDialog,
  VisitDetailsDialogResult,
} from '../components/visit-details-dialog/visit-details-dialog';

export interface OpenReviewDialogOptions {
  visitId: string;
  commentId?: string;
  doctorName?: string;
  institutionName?: string;
}

export interface OpenVisitDetailsOptions {
  visitId: string;
}

export interface OpenRescheduleDialogOptions {
  visitId: string;
}

@Injectable({
  providedIn: 'root',
})
export class VisitDialogService {
  private dialogService = inject(DialogService);
  private translationService = inject(TranslationService);

  /**
   * Otwiera dialog szczegółów wizyty
   */
  public openVisitDetailsDialog(
    options: OpenVisitDetailsOptions,
  ): DynamicDialogRef {
    return this.dialogService.open(VisitDetailsDialog, {
      data: { visitId: options.visitId },
      header: this.translationService.translate(
        'patient.visits.visitDetailsTitle',
      ),
      width: '80%',
      height: '67%',
      closable: true,
      modal: true,
      styleClass: 'visit-dialog',
    });
  }

  /**
   * Otwiera dialog recenzji/dodawania komentarza do wizyty
   */
  public openReviewDialog(options: OpenReviewDialogOptions): DynamicDialogRef {
    const dialogData = options.commentId
      ? {
          id: options.commentId,
          doctorName: options.doctorName,
          institutionName: options.institutionName,
        }
      : {
          visitId: options.visitId,
          doctorName: options.doctorName,
          institutionName: options.institutionName,
        };

    return this.dialogService.open(ReviewVisitDialog, {
      data: dialogData,
      header: this.translationService.translate('patient.visits.reviewTitle'),
      width: '70%',
      height: 'auto',
      modal: true,
    });
  }

  /**
   * Otwiera dialog przełożenia wizyty
   */
  public openRescheduleDialog(
    options: OpenRescheduleDialogOptions,
  ): DynamicDialogRef {
    return this.dialogService.open(ScheduleVisitDialog, {
      data: { visitId: options.visitId },
      header: this.translationService.translate(
        'patient.visits.rescheduleTitle',
      ),
      width: '70%',
      height: 'auto',
      closable: true,
      modal: true,
      styleClass: 'reschedule-dialog',
    });
  }

  /**
   * Pomocnicza metoda do obsługi workflow: szczegóły wizyty → recenzja
   * Zwraca Observable emitujący gdy użytkownik kliknie "Review" w details dialog
   */
  public openVisitDetailsWithReviewFlow(
    visitId: string,
  ): Observable<VisitDetailsDialogResult | undefined> {
    const ref = this.openVisitDetailsDialog({ visitId });
    return ref.onClose;
  }
}
