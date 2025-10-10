import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
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
import { DatePickerModule } from 'primeng/datepicker';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import {
  MedicalHistoryDialogData,
  MedicalHistoryDialogMode,
  MedicalHistoryDialogResult,
  MedicalHistoryFormModel,
  MedicalHistoryResponse,
  MedicalRecord,
} from '../../../../../../core/models/medical-history.model';

type MedicalHistoryForm = FormGroup<MedicalHistoryFormModel>;

@Component({
  selector: 'app-medical-history-dialog',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    DatePickerModule,
    InputTextModule,
    TextareaModule,
  ],
  templateUrl: './medical-history-dialog.html',
  styleUrl: './medical-history-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MedicalHistoryDialog {
  private ref = inject(
    DynamicDialogRef<MedicalHistoryDialogResult | undefined>,
  );
  private config = inject(DynamicDialogConfig<MedicalHistoryDialogData>);
  private fb = inject(FormBuilder);

  private readonly initialMode: MedicalHistoryDialogMode =
    this.config.data?.mode ?? 'view';

  private readonly initialRecord: MedicalRecord =
    this.config.data?.record ?? this.createEmptyRecord();

  protected readonly mode = signal<MedicalHistoryDialogMode>(this.initialMode);

  readonly form: MedicalHistoryForm = this.createForm(this.initialRecord);

  protected readonly isReadOnly = computed(() => this.mode() === 'view');

  protected readonly dialogTitle = computed(() =>
    this.mode() === 'view' ? 'Medical History' : 'Edit Medical History',
  );

  protected readonly confirmLabel = computed(() =>
    this.initialRecord?.id ? 'Confirm' : 'Create',
  );

  protected readonly formattedDate = computed(() => {
    const date = this.form.controls.date.value;
    if (!date) {
      return '';
    }
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(date);
  });

  constructor() {
    if (this.initialMode === 'view') {
      this.form.disable({ emitEvent: false });
    }
    this.form.controls.doctorName.disable();
    this.form.controls.doctorSurname.disable();
  }

  private createForm(record: MedicalRecord): MedicalHistoryForm {
    return this.fb.nonNullable.group({
      id: this.fb.nonNullable.control(record.id ?? ''),
      title: this.fb.nonNullable.control(record.title, [Validators.required]),
      date: this.fb.control(record.date ? new Date(record.date) : null, [
        Validators.required,
      ]),
      doctorName: this.fb.nonNullable.control(
        record.doctor?.doctorName ?? '',
        [],
      ),
      doctorSurname: this.fb.nonNullable.control(
        record.doctor?.doctorSurname ?? '',
      ),
      notes: this.fb.control(record.notes ?? '', [Validators.maxLength(2000)]),
    }) as MedicalHistoryForm;
  }

  private createEmptyRecord(): MedicalHistoryResponse {
    return {
      id: '',
      userId: '',
      title: '',
      date: new Date().toISOString(),
      note: '',
      doctor: {
        doctorName: '',
        doctorSurname: '',
        userId: '',
        specializations: [],
        valid: true,
      },
    } satisfies MedicalHistoryResponse;
  }

  public closeDialog(): void {
    this.ref.close(null);
  }

  public submit(): void {
    if (this.isReadOnly() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    const record: MedicalHistoryResponse = {
      id: '',
      title: value.title.trim(),
      date: value.date ? value.date.toISOString().split('T')[0] : '',
      note: value.notes ?? '',
      doctor: {
        ...this.initialRecord.doctor,
        doctorName: value.doctorName.trim(),
        doctorSurname: value.doctorSurname.trim(),
        specializations: [],
        userId: '',
        valid: false,
      },
      userId: '',
    };

    this.ref.close({
      mode: this.mode(),
      record,
    });
  }
}
