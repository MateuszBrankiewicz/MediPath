import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProgressSpinner } from 'primeng/progressspinner';
import { VisitCode } from '../../../../core/models/visit.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { VisitsService } from '../../../../core/services/visits/visits.service';

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

  protected readonly medicalHistory = signal<{ date: string; title: string }[]>(
    [
      { date: '10-10-2024', title: 'Diagnostic visit' },
      { date: '10-01-2023', title: 'Hearth Surgeon' },
      { date: '10-01-2023', title: 'Teeth removal' },
      { date: '10-10-2024', title: 'Diagnostic visit' },
      { date: '10-01-2023', title: 'Hearth Surgeon' },
      { date: '10-01-2023', title: 'Teeth removal' },
    ],
  );
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
          console.log('Visit finished successfully');
        },
        error: (error) => {
          console.error('Error finishing visit:', error);
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
        console.log(this.visitStatus());
      },
      error: (error) => {
        this.isLoading.set(false);
        console.error('Error loading visit data:', error);
      },
    });
  }
}
