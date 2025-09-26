import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import { CommentApiResponse } from '../models/review-page.model';

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
}
