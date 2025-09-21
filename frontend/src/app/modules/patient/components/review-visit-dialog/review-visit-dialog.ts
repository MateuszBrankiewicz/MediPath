import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { SelectModule } from 'primeng/select';
import { ButtonModule } from 'primeng/button';
import { StarRatingOption } from '../../models/review-page.model';

@Component({
  selector: 'app-review-visit-dialog',
  imports: [SelectModule, ButtonModule, FormsModule],
  templateUrl: './review-visit-dialog.html',
  styleUrl: './review-visit-dialog.scss',
})
export class ReviewVisitDialog {
  private ref = inject(DynamicDialogRef);
  private config = inject(DynamicDialogConfig);

  comments = '';
  doctorRating: number | null = null;
  institutionRating: number | null = null;

  starRatingOptions: StarRatingOption[] = [
    { label: '1 Star', value: 1, stars: '★☆☆☆☆' },
    { label: '2 Stars', value: 2, stars: '★★☆☆☆' },
    { label: '3 Stars', value: 3, stars: '★★★☆☆' },
    { label: '4 Stars', value: 4, stars: '★★★★☆' },
    { label: '5 Stars', value: 5, stars: '★★★★★' },
  ];

  onSubmit() {
    const reviewData = {
      comments: this.comments,
      doctorRating: this.doctorRating,
      institutionRating: this.institutionRating,
    };
    this.ref.close(reviewData);
  }

  onCancel() {
    this.ref.close();
  }
}
