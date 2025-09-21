import { Component, input, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Comment } from '../..//models/doctor.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-patient-comment-component',
  imports: [DatePipe],
  templateUrl: './patient-comment-component.html',
  styleUrl: './patient-comment-component.scss',
})
export class PatientCommentComponent {
  public readonly comment = input.required<Comment>();
  protected translationService = inject(TranslationService);
}
