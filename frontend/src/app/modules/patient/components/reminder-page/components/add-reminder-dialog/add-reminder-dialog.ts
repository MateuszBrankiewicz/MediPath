import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  inject,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

export interface MedicationReminder {
  id?: number;
  title: string;
  reminderTime: Date | null;
  startDate: Date | null;
  endDate: Date | null;
  content: string;
}

@Component({
  selector: 'app-add-reminder-dialog',
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule],
  templateUrl: './add-reminder-dialog.html',
  styleUrl: './add-reminder-dialog.scss',
})
export class AddReminderDialog implements OnInit {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);
  @Input() visible = false;
  @Input() reminder: MedicationReminder | null = null;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() save = new EventEmitter<MedicationReminder>();
  @Output() delete = new EventEmitter<number>();

  reminderForm: FormGroup;
  private fb = inject(FormBuilder);

  constructor() {
    this.reminderForm = this.fb.group({
      title: ['', [Validators.required]],
      reminderTime: [null],
      startDate: [null],
      endDate: [null],
      content: [''],
    });
  }

  ngOnInit() {
    if (this.reminder) {
      this.reminderForm.patchValue({
        title: this.reminder.title,
        reminderTime: this.reminder.reminderTime,
        startDate: this.reminder.startDate,
        endDate: this.reminder.endDate,
        content: this.reminder.content,
      });
    }
  }

  onClose() {
    this.ref.close();
  }

  onSave() {
    if (this.reminderForm.valid) {
      const formValue = this.reminderForm.value;
      const reminderData: MedicationReminder = {
        ...formValue,
        id: this.reminder?.id,
      };

      this.save.emit(reminderData);
      this.onClose();
    }
  }

  onDelete() {
    if (this.reminder?.id) {
      this.delete.emit(this.reminder.id);
      this.onClose();
    }
  }
}
