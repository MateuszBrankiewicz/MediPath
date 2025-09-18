import { CardModule } from 'primeng/card';
import { Component, inject, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { VisitPageModel, VisitStatus } from './visit-page.model';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { PopoverModule } from 'primeng/popover';
import { VisitDetailsDialog } from '../visit-details-dialog/visit-details-dialog';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ScheduleVisitDialog } from '../schedule-visit-dialog/schedule-visit-dialog';
import { ReviewVisitDialog } from '../review-visit-dialog/review-visit-dialog';

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
})
export class VisitPage {
  protected readonly showVisitDetailsDialog = signal(false);
  protected readonly selectedVisitId = signal<number | null>(null);

  private dialogService = inject(DialogService);

  private ref: DynamicDialogRef | undefined;

  visits: VisitPageModel[] = [
    {
      id: 1,
      doctorName: 'Dr. Smith',
      institution: 'City Hospital',
      date: new Date('2024-06-01'),
      status: VisitStatus.Scheduled,
    },
    {
      id: 2,
      doctorName: 'Dr. Johnson',
      institution: 'Health Clinic',
      date: new Date('2024-05-20'),
      status: VisitStatus.Completed,
    },
    {
      id: 3,
      doctorName: 'Dr. Lee',
      institution: 'Downtown Medical Center',
      date: new Date('2024-06-10'),
      status: VisitStatus.Canceled,
    },
  ];

  protected cancelVisit() {
    console.log('cancel');
  }

  protected editVisit() {
    console.log('edit');
  }
  protected openVisitDialog(id: number): void {
    this.selectedVisitId.set(id);

    this.ref = this.dialogService.open(VisitDetailsDialog, {
      data: { visitId: id },
      header: 'Visit details',
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
  protected openRescheduleDialog(id: number): void {
    this.ref = this.dialogService.open(ScheduleVisitDialog, {
      data: { visitId: id },
      header: 'Reschedule a visit',
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

  private openReviewDialog(id: number) {
    this.ref = this.dialogService.open(ReviewVisitDialog, {
      data: { visitId: id },
      header: 'ReviewVisit',
      width: '70%',
      height: 'auto',
    });
  }
}
