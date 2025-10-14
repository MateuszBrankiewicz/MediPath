import { Component, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { DoctorSchedule } from '../../../../../../core/models/doctor.model';
import { TranslationService } from '../../../../../../core/services/translation/translation.service';
import {
  BulkEditFormData,
  ScheduleManagementService,
  SingleEditFormData,
} from '../../services/schedule-management.service';

@Component({
  selector: 'app-schedule-details-dialog',
  imports: [
    DialogModule,
    TagModule,
    InputTextModule,
    FormsModule,
    ButtonModule,
    ProgressSpinnerModule,
  ],
  templateUrl: './schedule-details-dialog.html',
  styleUrl: './schedule-details-dialog.scss',
})
export class ScheduleDetailsDialog {
  protected translationService = inject(TranslationService);
  private scheduleManagementService = inject(ScheduleManagementService);

  // Inputs
  visible = input<boolean>(false);
  doctorName = input<string>('');
  selectedDate = input<Date | null>(null);
  schedules = input<DoctorSchedule[]>([]);

  // Outputs
  visibleChange = output<boolean>();
  dialogClosed = output<void>();

  // Internal state
  protected editingSlotId = signal<string | null>(null);
  protected editMode = signal<'single' | 'bulk'>('single');
  protected showBulkEdit = signal<boolean>(false);

  // Form data
  protected editFormData = signal<SingleEditFormData>({
    startTime: '',
    endTime: '',
  });

  protected bulkEditFormData = signal<BulkEditFormData>({
    startTime: '08:00',
    endTime: '16:00',
    interval: 30,
  });

  // Computed properties
  protected readonly selectedDateFormatted = () => {
    const date = this.selectedDate();
    if (!date) return '';
    return this.scheduleManagementService.getFormattedDate(date);
  };

  protected readonly isLoading = this.scheduleManagementService.getIsLoading();

  /**
   * Close dialog handler
   */
  protected closeDialog(): void {
    this.visibleChange.emit(false);
    this.resetDialogState();
    this.dialogClosed.emit();
  }

  /**
   * Toggle bulk edit mode
   */
  protected toggleBulkEdit(): void {
    const isCurrentlyBulk = this.showBulkEdit();
    this.showBulkEdit.set(!isCurrentlyBulk);
    this.editMode.set(!isCurrentlyBulk ? 'bulk' : 'single');
    this.editingSlotId.set(null);
  }

  /**
   * Format time for display
   */
  protected formatTime(dateTimeString: string): string {
    return this.scheduleManagementService.formatTime(dateTimeString);
  }

  /**
   * Edit single slot
   */
  protected editSlot(slotId: string): void {
    if (this.editingSlotId() === slotId) {
      this.editingSlotId.set(null);
      this.editFormData.set({ startTime: '', endTime: '' });
    } else {
      const slot = this.schedules().find((s) => s.id === slotId);
      if (slot) {
        this.editFormData.set({
          startTime: this.scheduleManagementService.formatTimeForInput(
            slot.startHour,
          ),
          endTime: this.scheduleManagementService.formatTimeForInput(
            slot.endHour,
          ),
        });
      }
      this.editingSlotId.set(slotId);
    }
  }

  /**
   * Save single slot changes
   */
  protected saveSlotChanges(slot: DoctorSchedule): void {
    this.scheduleManagementService.updateSingleSlot(slot, this.editFormData());
    this.editingSlotId.set(null);
  }

  /**
   * Cancel single slot edit
   */
  protected cancelSlotEdit(): void {
    this.editingSlotId.set(null);
    this.editFormData.set({ startTime: '', endTime: '' });
  }

  /**
   * Save bulk changes
   */
  protected saveBulkChanges(): void {
    this.scheduleManagementService.saveBulkChanges(this.bulkEditFormData());
    this.showBulkEdit.set(false);
    this.editMode.set('single');
  }

  /**
   * Cancel bulk edit
   */
  protected cancelBulkEdit(): void {
    this.showBulkEdit.set(false);
    this.editMode.set('single');
    // Reset form to defaults
    this.bulkEditFormData.set({
      startTime: '08:00',
      endTime: '16:00',
      interval: 30,
    });
  }

  /**
   * Reset dialog state when closing
   */
  private resetDialogState(): void {
    this.editingSlotId.set(null);
    this.editMode.set('single');
    this.showBulkEdit.set(false);
    this.editFormData.set({ startTime: '', endTime: '' });
    this.bulkEditFormData.set({
      startTime: '08:00',
      endTime: '16:00',
      interval: 30,
    });
  }
}
