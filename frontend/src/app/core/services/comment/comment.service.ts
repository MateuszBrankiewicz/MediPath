import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';

import { Comment } from '../../models/doctor.model';
import {
  AddComentRequest,
  CommentApiResponse,
  CommentWithRating,
  InstitutionCommentResponse,
} from '../../models/review.model';
@Injectable({
  providedIn: 'root',
})
export class CommentService {
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

  public getCommentByInstitution(institutionId: string): Observable<Comment[]> {
    return this.http
      .get<InstitutionCommentResponse>(
        `${API_URL}/comments/institution/${institutionId}`,
      )
      .pipe(
        map((response) =>
          response.comments.map((resp) => {
            return {
              id: resp.id,
              userName: resp.author,
              visitedInstitution: resp.institution,
              content: resp.content,
              numberOfStars: resp.institutionRating,
              dateOfVisit: new Date(resp.createdAt),
            };
          }),
        ),
      );
  }

  public getCommentByDoctor(doctorId: string): Observable<Comment[]> {
    return this.http
      .get<InstitutionCommentResponse>(`${API_URL}/comments/doctor/${doctorId}`)
      .pipe(
        map((response) =>
          response.comments.map((resp) => {
            return {
              id: resp.id,
              userName: resp.author,
              visitedInstitution: resp.institution,
              content: resp.content,
              numberOfStars: resp.doctorRating,
              dateOfVisit: new Date(resp.createdAt),
            };
          }),
        ),
      );
  }
}
