import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Visit } from '../../models/visit-page.model';

@Component({
  selector: 'app-visit-details-dialog',
  standalone: true,
  imports: [ButtonModule, CommonModule],
  templateUrl: './visit-details-dialog.html',
  styleUrl: './visit-details-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VisitDetailsDialog {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);
  protected translationService = inject(TranslationService);

  public readonly visitId = this.config.data?.visitId;

  public readonly visit = signal<Visit>({
    id: this.visitId || 1,
    doctorName: 'Jadwiga Chymyl',
    doctorPhoto: 'assets/imageDoctor.png',
    specialisation: 'Cardiologist',
    institution: 'Szpital kliniczny',
    institutionPhoto: 'assets/footer-landing.png',
    address: 'Jana Pawła II 2/34, 23-500 Lublin',
    date: new Date('2025-03-20T10:00:00'),
    status: 'Scheduled',
    notes:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla facilisi. Sed tristique, turpis ut tincidunt pretium, purus ex faucibus odio, ut varius elit lorem a justo. Phasellus in urna vitae mauris pharetra sollicitudin. Integer auctor, turpis vel dignissim pharetra, arcu ligula tincidunt eros, eget scelerisque velit nulla nec nunc.',
    prescriptionPin: '0000',
    referralPin: '0000',
  });

  public reviewVisit(): void {
    this.ref.close('REVIEW');
  }

  public closeDialog(): void {
    this.ref.close();
  }
}
