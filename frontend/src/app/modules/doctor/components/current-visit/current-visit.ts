import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DialogService } from 'primeng/dynamicdialog';
import { ProgressSpinner } from 'primeng/progressspinner';
import { MedicalHistoryResponse } from '../../../../core/models/medical-history.model';
import { VisitCode } from '../../../../core/models/visit.model';
import { MedicalHistoryService } from '../../../../core/services/medical-history/medical-history.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';
import { MedicalHistoryDialog } from '../../../patient/components/medical-history-page/components/medical-history-dialog/medical-history-dialog';

@Component({
  selector: 'app-current-visit',
  imports: [ProgressSpinner],
  templateUrl: './current-visit.html',
  styleUrl: './current-visit.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CurrentVisit implements OnInit {
  getPrescriptionCodes(): string {
    return this.prescriptions()
      .map((p) => p.code)
      .join(', ');
  }

  getReferralCodes(): string {
    return this.referrals()
      .map((r) => r.code)
      .join(', ');
  }
  private dialogService = inject(DialogService);
  private medicalHistoryService = inject(MedicalHistoryService);
  protected readonly translationService = inject(TranslationService);
  protected readonly isLoading = signal<boolean>(false);
  protected readonly patientName = signal<string>('Monika Nowak');
  protected readonly prescriptions = signal<VisitCode[]>([]);
  protected readonly referrals = signal<VisitCode[]>([]);
  protected readonly notes = signal<string>('');
  protected readonly governmentId = signal<string>('ABC123456');
  protected readonly editSidebar = signal<
    'none' | 'prescriptions' | 'referrals'
  >('none');
  protected readonly visitStatus = signal<string>('');
  protected readonly visitDate = signal<Date | null>(null);
  protected readonly pinDraft = signal<string>('');
  private toastService = inject(ToastService);
  protected readonly isEditable = computed(() => {
    const status = this.visitStatus();
    const visitDate = this.visitDate();

    if (status !== 'Upcoming' || !visitDate) {
      return false;
    }

    const now = new Date();
    now.setHours(0, 0, 0, 0);

    const visit = new Date(visitDate);
    visit.setHours(0, 0, 0, 0);

    const dayBefore = new Date(visit);
    dayBefore.setDate(dayBefore.getDate() - 1);

    const dayAfter = new Date(visit);
    dayAfter.setDate(dayAfter.getDate() + 1);

    return now >= dayBefore && now <= dayAfter;
  });

  protected readonly medicalHistory = signal<MedicalHistoryResponse[]>([]);
  private activatedRoute = inject(ActivatedRoute);

  private visitService = inject(VisitsService);

  ngOnInit() {
    const visitIdFromRoute = this.activatedRoute.snapshot.paramMap.get('id');
    this.loadVisitData(visitIdFromRoute);
  }

  openEditor(type: 'prescriptions' | 'referrals') {
    this.editSidebar.set(type);
    this.pinDraft.set('');
  }

  closeEditor() {
    this.editSidebar.set('none');
    this.pinDraft.set('');
  }

  onNotesChange(event: Event) {
    const target = event.target as HTMLTextAreaElement;
    this.notes.set(target.value);
  }

  onPinDraftChange(event: Event) {
    const target = event.target as HTMLInputElement;
    if (this.pinDraft().length > 4) return;
    this.pinDraft.set(target.value);
  }

  addPin() {
    const value = this.pinDraft().trim();
    if (!value) return;
    if (this.editSidebar() === 'prescriptions') {
      this.prescriptions.update((arr) => [
        ...arr,
        { codeType: 'PRESCRIPTION', code: value, active: true },
      ]);
    } else if (this.editSidebar() === 'referrals') {
      this.referrals.update((arr) => [
        ...arr,
        { codeType: 'REFERRAL', code: value, active: true },
      ]);
    }
    this.pinDraft.set('');
  }

  removePin(index: number) {
    if (this.editSidebar() === 'prescriptions') {
      this.prescriptions.update((arr) => arr.filter((_, i) => i !== index));
    } else if (this.editSidebar() === 'referrals') {
      this.referrals.update((arr) => arr.filter((_, i) => i !== index));
    }
  }

  saveEditor() {
    this.closeEditor();
  }

  finishVisit() {
    this.visitService
      .finishVisit(
        {
          prescriptions: this.getPrescriptionCodes().split(', '),
          referrals: this.getReferralCodes().split(', '),
          note: this.notes(),
        },
        this.activatedRoute.snapshot.paramMap.get('id') || '',
      )
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate('doctor.visit.finishSuccess'),
          );
          this.visitStatus.set('Completed');
        },
        error: () => {
          this.toastService.showError(
            this.translationService.translate('doctor.visit.finishError'),
          );
        },
      });
  }

  private loadVisitData(visitId: string | null) {
    this.isLoading.set(true);
    this.visitService.getVisitDetails(visitId || '').subscribe({
      next: (visit) => {
        this.patientName.set(
          `${visit.visit.patient.name} ${visit.visit.patient.surname}`,
        );
        this.governmentId.set(visit.visit.patient.govID || '');
        this.notes.set(visit.visit.note || '');
        this.visitStatus.set(visit.visit.status);
        this.visitDate.set(new Date(visit.visit.time.startTime));
        this.prescriptions.set(
          Array.isArray(visit.visit.codes)
            ? visit.visit.codes.filter(
                (code) => code.codeType === 'PRESCRIPTION',
              )
            : [],
        );
        this.referrals.set(
          Array.isArray(visit.visit.codes)
            ? visit.visit.codes.filter((code) => code.codeType === 'REFERRAL')
            : [],
        );
        this.isLoading.set(false);
        this.loadMedicalHistory(visit.visit.patient.userId);
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Error loading visit data:', error);
      },
    });
  }
  private loadMedicalHistory(patientId: string) {
    this.medicalHistoryService.getPatientMedicalHistory(patientId).subscribe({
      next: (history) => {
        this.medicalHistory.set(history);
      },
      error: (error) => {
        console.error('Error loading medical history:', error);
      },
    });
  }

  protected viewHistoryDetails(item: MedicalHistoryResponse): void {
    this.dialogService.open(MedicalHistoryDialog, {
      data: { mode: 'view', record: item },
      header: this.translationService.translate('doctor.visit.historyDetails'),
      width: '50%',
      closable: true,
      modal: true,
      styleClass: 'medical-history-dialog',
    });
  }
}
