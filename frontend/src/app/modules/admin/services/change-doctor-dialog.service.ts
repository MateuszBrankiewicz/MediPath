import { inject, Injectable } from '@angular/core';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ChangeDoctorDialogComponent } from '../components/admin-dashboard/dialogs/change-doctor-dialog/change-doctor-dialog.component';

export interface ChangeDoctorDialogData {
  visitId: string;
  institutionId: string;
  currentDoctorId: string;
  currentDoctorName: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChangeDoctorDialogService {
  private dialogService = inject(DialogService);

  openChangeDoctorDialog(data: ChangeDoctorDialogData): DynamicDialogRef {
    return this.dialogService.open(ChangeDoctorDialogComponent, {
      header: 'Change Doctor',
      width: '500px',
      modal: true,
      closable: true,
      dismissableMask: true,
      focusOnShow: false,
      data,
    });
  }
}
