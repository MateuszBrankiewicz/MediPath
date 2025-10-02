import { inject, Injectable } from '@angular/core';
import { DialogService } from 'primeng/dynamicdialog';
import { Observable } from 'rxjs';
import { AcceptActionDialogComponent } from '../../shared/components/ui/accept-action-dialog/accept-action-dialog-component';

@Injectable({
  providedIn: 'root',
})
export class PatientCodeDialogService {
  private readonly dialogService = inject(DialogService);

  public useCode(code: {
    codeNumber: number;
    codeType: string;
  }): Observable<boolean> {
    return new Observable<boolean>((observer) => {
      const ref = this.dialogService.open(AcceptActionDialogComponent, {
        header: 'Confirm Action',
        width: '400px',
        data: {
          message: `Are you sure you want to use the code ${code.codeNumber}?`,
        },
      });

      ref.onClose.subscribe((confirmed: boolean) => {
        observer.next(!!confirmed);
        observer.complete();
      });
    });
  }
}
