import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-current-visit',
  imports: [],
  templateUrl: './current-visit.html',
  styleUrl: './current-visit.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CurrentVisit {
  protected readonly translationService = inject(TranslationService);

  protected readonly patientName = signal<string>('Monika Nowak');
  protected readonly prescriptions = signal<string[]>(['1324', '1324', '1324']);
  protected readonly referrals = signal<string[]>(['1324', '1324', '1324']);
  protected readonly notes = signal<string>('');

  protected readonly editSidebar = signal<
    'none' | 'prescriptions' | 'referrals'
  >('none');
  protected readonly pinDraft = signal<string>('');

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
  // UI actions
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
    this.pinDraft.set(target.value);
  }

  addPin() {
    const value = this.pinDraft().trim();
    if (!value) return;
    if (this.editSidebar() === 'prescriptions') {
      this.prescriptions.update((arr) => [...arr, value]);
    } else if (this.editSidebar() === 'referrals') {
      this.referrals.update((arr) => [...arr, value]);
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
    // Placeholder: implement finish visit logic
    console.log('Finishing visit for', this.patientName());
  }
}
