import { Component, input } from '@angular/core';
import { Comment } from '../doctor-page/doctor.model';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-patient-comment-component',
  imports: [DatePipe],
  templateUrl: './patient-comment-component.html',
  styleUrl: './patient-comment-component.scss',
})
export class PatientCommentComponent {
  public readonly comment = input.required<Comment>();
}
