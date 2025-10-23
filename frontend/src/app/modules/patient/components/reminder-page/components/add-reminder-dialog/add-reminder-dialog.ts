import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { InputTextModule } from 'primeng/inputtext';
import { MedicationReminder } from '../../../../../../core/models/reminder.model';
import { TranslationService } from '../../../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-add-reminder-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    DatePicker,
  ],
  templateUrl: './add-reminder-dialog.html',
  styleUrl: './add-reminder-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddReminderDialog implements OnInit {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);
  visible = signal(false);
  reminder = signal<MedicationReminder | null>(null);
  protected translationService = inject(TranslationService);

  visibleChange = new EventEmitter<boolean>();
  save = new EventEmitter<MedicationReminder>();
  delete = new EventEmitter<number>();

  reminderForm: FormGroup;
  private fb = inject(FormBuilder);

  constructor() {
    this.reminderForm = this.fb.group({
      title: ['', [Validators.required]],
      reminderTime: [null, [Validators.required]],
      startDate: [null, [Validators.required]],
      endDate: [null, [Validators.required]],
      content: [''],
    });
  }

  ngOnInit() {
    this.reminder.set(this.config.data?.notification ?? null);

    if (this.reminder()) {
      this.reminderForm.patchValue({
        title: this.reminder()?.title ?? '',
        reminderTime: this.reminder()?.reminderTime ?? null,
        startDate: this.reminder()?.startDate ?? null,
        endDate: this.reminder()?.endDate ?? null,
        content: this.reminder()?.content ?? '',
      });
    }
  }

  onClose() {
    this.ref.close();
  }

  onSave() {
    if (this.reminderForm.invalid) {
      this.reminderForm.markAllAsTouched();
      return;
    }

    if (this.reminderForm.valid) {
      const formValue = this.reminderForm.value;
      const reminderData: MedicationReminder = {
        ...formValue,
        id: this.reminder()?.id,
      };

      this.save.emit(reminderData);
      this.onClose();
    }
  }

  onDelete() {
    this.onClose();
  }
}
