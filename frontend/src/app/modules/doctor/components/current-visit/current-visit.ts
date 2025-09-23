import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-current-visit',
  imports: [],
  templateUrl: './current-visit.html',
  styleUrl: './current-visit.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CurrentVisit {
  translationService = inject(TranslationService);

  // Basic state
  patientName = signal<string>('Monika Nowak');
  prescriptions = signal<string[]>(['1324', '1324', '1324']);
  referrals = signal<string[]>(['1324', '1324', '1324']);
  notes = signal<string>('');

  // Right-side editor panel state
  editSidebar = signal<'none' | 'prescriptions' | 'referrals'>('none');
  pinDraft = signal<string>('');

  // Mock medical history
  medicalHistory = signal<{ date: string; title: string }[]>([
    { date: '10-10-2024', title: 'Diagnostic visit' },
    { date: '10-01-2023', title: 'Hearth Surgeon' },
    { date: '10-01-2023', title: 'Teeth removal' },
    { date: '10-10-2024', title: 'Diagnostic visit' },
    { date: '10-01-2023', title: 'Hearth Surgeon' },
    { date: '10-01-2023', title: 'Teeth removal' },
  ]);

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
