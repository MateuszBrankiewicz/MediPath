import { DatePipe, UpperCasePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { Comment } from '../..//models/doctor.model';

@Component({
  selector: 'app-patient-comment-component',
  imports: [DatePipe, UpperCasePipe],
  templateUrl: './patient-comment-component.html',
  styleUrl: './patient-comment-component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientCommentComponent {
  public readonly comment = input.required<Comment>();
  protected translationService = inject(TranslationService);
}
