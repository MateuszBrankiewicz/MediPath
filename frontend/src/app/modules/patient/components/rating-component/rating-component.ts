import { FormsModule } from '@angular/forms';
import { Component } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { PanelModule } from 'primeng/panel';
import { RatingModule } from 'primeng/rating';

export interface CommentWithRating {
  id: string;
  comment: string;
  doctorName: string;
  institutionName: string;
  doctorRating: number;
  institutionRating: number;
}

@Component({
  selector: 'app-rating-component',
  imports: [FormsModule, ButtonModule, PanelModule, RatingModule],
  templateUrl: './rating-component.html',
  styleUrl: './rating-component.scss',
})
export class RatingComponent {
  protected viewRatings(id: string): void {
    console.log('Viewing ratings for ID:', id);
  }

  protected comments = [
    {
      id: '1',
      comment: 'Excellent care and attention.',
      doctorName: 'Dr. Smith',
      institutionName: 'City Hospital',
      doctorRating: 5,
      institutionRating: 4,
    },
    {
      id: '2',
      comment: 'Very professional and kind.',
      doctorName: 'Dr. Johnson',
      institutionName: 'Green Clinic',
      doctorRating: 4,
      institutionRating: 5,
    },
    {
      id: '3',
      comment: 'Average experience.',
      doctorName: 'Dr. Lee',
      institutionName: 'Downtown Medical Center',
      doctorRating: 3,
      institutionRating: 3,
    },
  ] as CommentWithRating[];
}
