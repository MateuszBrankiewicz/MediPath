import { Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Comment } from '../..//models/doctor.model';

@Component({
  selector: 'app-patient-comment-component',
  imports: [DatePipe],
  templateUrl: './patient-comment-component.html',
  styleUrl: './patient-comment-component.scss',
})
export class PatientCommentComponent {
  public readonly comment = input.required<Comment>();
}
