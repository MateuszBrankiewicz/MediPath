import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { DataViewModule } from 'primeng/dataview';
import { DialogService } from 'primeng/dynamicdialog';
import { PanelModule } from 'primeng/panel';
import { TooltipModule } from 'primeng/tooltip';
import { map } from 'rxjs';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { MedicalRecord } from '../../models/medical-history.model';
import { PatientMedicalHistoryService } from '../../services/patient-medical-history.service';
import { MedicalHistoryDialog } from './components/medical-history-dialog/medical-history-dialog';

@Component({
  selector: 'app-medical-history-page',
  imports: [DataViewModule, DatePipe, ButtonModule, PanelModule, TooltipModule],
  templateUrl: './medical-history-page.html',
  providers: [DialogService],
  styleUrl: './medical-history-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MedicalHistoryPage implements OnInit {
  protected readonly medicalRecords = signal<MedicalRecord[]>([]);
  protected translationService = inject(TranslationService);
  private destroyRef = inject(DestroyRef);
  private dialogService = inject(DialogService);

  private readonly medicalHistoryService = inject(PatientMedicalHistoryService);
  ngOnInit(): void {
    this.loadMedicalHistory();
  }

  viewRecord(record: MedicalRecord): void {
    const ref = this.dialogService.open(MedicalHistoryDialog, {
      header: 'Medical Record Details',
      width: '50%',
      data: {
        mode: 'edit',
        record,
      },
      closable: true,
    });

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated) => {
        if (updated) {
          this.loadMedicalHistory();
        }
      });
  }

  deleteRecord(recordId: string): void {
    const currentRecords = this.medicalRecords();
    this.medicalRecords.set(
      currentRecords.filter((record) => record.id !== recordId),
    );
  }

  addNewEntry(): void {
    const ref = this.dialogService.open(MedicalHistoryDialog, {
      header: 'Medical Record Details',
      width: '50%',
      data: {
        mode: 'edit',
        record: undefined,
      },
      closable: true,
    });

    ref.onClose
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((updated) => {
        console.log(updated);
      });
  }

  private loadMedicalHistory(): void {
    this.medicalHistoryService
      .getMyMedicalHistory()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        map((response): MedicalRecord[] => {
          const medicalhistories = Array.isArray(response.medicalhistories)
            ? response.medicalhistories
            : [];
          return medicalhistories.map((record) => ({
            id: record.id,
            title: record.title,
            date: new Date(record.date),
            doctor: {
              doctorName: record.doctor.doctorName,
              doctorSurname: record.doctor.doctorSurname,
            },
            notes: record.note,
          }));
        }),
      )
      .subscribe((records) => {
        this.medicalRecords.set(records);
      });
  }
}
