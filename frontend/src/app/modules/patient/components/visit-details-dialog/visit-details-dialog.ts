import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { map } from 'rxjs';
import { Visit, VisitCode } from '../../../../core/models/visit.model';
import { AddressFormatPipe } from '../../../../core/pipes/address-format-pipe';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';

@Component({
  selector: 'app-visit-details-dialog',
  imports: [
    ButtonModule,
    CommonModule,
    ProgressSpinnerModule,
    DatePipe,
    AddressFormatPipe,
  ],
  templateUrl: './visit-details-dialog.html',
  styleUrl: './visit-details-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisitDetailsDialog implements OnInit {
  private ref = inject(DynamicDialogRef<VisitDetailsDialogResult>);
  private config = inject(DynamicDialogConfig);
  protected translationService = inject(TranslationService);
  private visitService = inject(VisitsService);
  public readonly visitId = this.config.data?.visitId;
  protected isLoading = signal(false);

  ngOnInit(): void {
    this.isLoading.set(true);
    this.loadVisitDetails(this.visitId);
  }

  public readonly visit = signal<Visit | null>(null);

  private loadVisitDetails(visitId: string): void {
    this.visitService
      .getVisitDetails(visitId)
      .pipe(
        map((visit) => {
          return {
            id: visit.visit.id,
            doctorName: `${visit.visit.doctor.doctorName} ${visit.visit.doctor.doctorSurname}`,
            doctorPhoto: visit.visit.doctorPfp,
            specialisation: visit.visit.doctor.specialisations[0],
            institution: visit.visit.institution.institutionName,
            institutionPhoto: visit.visit.institutionPfp,
            address: visit.visit.institutionAddress || '',
            date: new Date(visit.visit.time.startTime),
            status: visit.visit.status,
            notes: visit.visit.note,
            prescriptionPin: this.getCodePins(
              'prescription',
              visit.visit.codes,
            ),
            referralPin: this.getCodePins('referral', visit.visit.codes),
            commentId: visit.visit.commentId,
          };
        }),
      )
      .subscribe((visit) => {
        this.visit.set(visit);
        console.log(visit);
        this.isLoading.set(false);
      });
  }

  public reviewVisit(): void {
    this.ref.close({
      action: 'REVIEW',
      commentId: this.visit()?.commentId,
      doctorName: this.visit()?.doctorName,
      institutionName: this.visit()?.institution,
    });
  }

  public closeDialog(): void {
    this.ref.close();
  }

  public getCodePins(
    codeType: 'prescription' | 'referral',
    codes: VisitCode[],
  ): string {
    return codes
      .filter((code) => code.codeType.toLowerCase() === codeType)
      .map((code) => code.code)
      .join(',');
  }
}

export interface VisitDetailsDialogResult {
  action: 'REVIEW';
  commentId?: string;
  doctorName?: string;
  institutionName?: string;
}
