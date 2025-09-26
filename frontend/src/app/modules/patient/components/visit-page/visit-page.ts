import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { MenuModule } from 'primeng/menu';
import { PopoverModule } from 'primeng/popover';
import { TableModule } from 'primeng/table';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  VisitPageModel,
  VisitResponseArray,
  VisitStatus,
} from '../../models/visit-page.model';
import { PatientVisitsService } from '../../services/patient-visits.service';
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
  ],
  providers: [DialogService],
  templateUrl: './visit-page.html',
  styleUrl: './visit-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisitPage {
  protected readonly showVisitDetailsDialog = signal(false);
  protected readonly selectedVisitId = signal<string | null>(null);
  private visitService = inject(PatientVisitsService);
  private dialogService = inject(DialogService);
  protected translationService = inject(TranslationService);

  private ref: DynamicDialogRef | undefined;

  protected readonly visits = toSignal<VisitPageModel[]>(
    this.visitService.getUpcomingVisits().pipe(
      map((visits: VisitResponseArray): VisitPageModel[] =>
        visits.map((visit) => ({
          id: visit.id,
          date: this.formatDate(visit.time.startTime),
          doctorName: visit.doctor.doctorName,
          institution: visit.institution.institutionName,
          status: this.parseVisitStatus(visit.status),
        })),
      ),
    ),
  );

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

    this.ref.onClose.subscribe((result) => {
      if (result === 'REVIEW') {
        this.openReviewDialog(id);
      }
    });
  }

  protected openRescheduleDialog(id: string): void {
    this.ref = this.dialogService.open(ScheduleVisitDialog, {
      data: { visitId: id },
      header: this.translationService.translate(
        'patient.visits.rescheduleTitle',
      ),
      width: '70%',
      height: 'auto',
      closable: true,
      modal: true,
      styleClass: 'reschedule-dialog',
    });

    this.ref.onClose.subscribe((result) => {
      if (result) {
        console.log('Visit rescheduled:', result);
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
  }

  private parseVisitStatus(status: string): VisitStatus {
    switch (status.toLowerCase()) {
      case 'upcoming':
        return VisitStatus.Scheduled;
      case 'completed':
        return VisitStatus.Completed;
      case 'canceled':
        return VisitStatus.Canceled;
      default:
        return VisitStatus.Scheduled;
    }
  }

  private formatDate(array: number[]): Date {
    const [year, month, day, hour, minute] = array;
    const dateString = `${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.${year} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    return new Date(dateString);
  }
}
