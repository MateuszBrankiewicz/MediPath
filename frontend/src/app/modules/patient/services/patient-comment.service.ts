import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import { CommentWithRating } from '../components/rating-component/rating-component';
import {
  AddComentRequest,
  CommentApiResponse,
} from '../models/review-page.model';

@Injectable({
  providedIn: 'root',
})
export class PatientCommentService {
  private http = inject(HttpClient);

  public getUsersComments(): Observable<CommentApiResponse> {
    return this.http.get<CommentApiResponse>(API_URL + '/users/me/comments', {
      withCredentials: true,
    });
  }

  public editComment(
    commentToEdit: Partial<CommentWithRating>,
  ): Observable<CommentWithRating> {
    const { comment, institutionRating, doctorRating } = commentToEdit;
    return this.http.put<CommentWithRating>(
      API_URL + '/comments/' + commentToEdit.id,
      { comment, institutionRating, doctorRating },
      { withCredentials: true },
    );
  }

  public deleteComment(commentId: string): Observable<unknown> {
    return this.http.delete(`${API_URL}/comments/${commentId}`, {
      withCredentials: true,
    });
  }

  public addComment(commentToSend: AddComentRequest) {
    const { comment, visitID, doctorRating, institutionRating } = commentToSend;
    return this.http.post(
      `${API_URL}/comments/add`,
      { comment, visitID, doctorRating, institutionRating },
      { withCredentials: true },
    );
  }
}
