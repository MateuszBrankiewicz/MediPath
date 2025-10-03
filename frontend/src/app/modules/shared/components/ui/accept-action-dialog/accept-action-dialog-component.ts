import { Component, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';

export interface AcceptActionDialogData {
  message: string;
  acceptText?: string;
  cancelText?: string;
}

@Component({
  selector: 'app-accept-action-dialog-component',
  imports: [ButtonModule],
  templateUrl: './accept-action-dialog-component.html',
  styleUrl: './accept-action-dialog-component.scss',
})
export class AcceptActionDialogComponent {
  private ref = inject(
    DynamicDialogRef<AcceptActionDialogComponent | undefined>,
  );
  private config = inject(DynamicDialogConfig<AcceptActionDialogData>);
  protected data = this.config.data ?? { message: '' };

  protected onAccept(): void {
    this.ref.close(true);
  }

  protected onCancel(): void {
    this.ref.close(false);
  }
}
