import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import { VisitResponse } from '../models/visit-page.model';

interface UpcomingVisitsResponse {
  visits: VisitResponse[];
}

@Injectable({
  providedIn: 'root',
})
export class PatientVisitsService {
  private http = inject(HttpClient);

  public getUpcomingVisits() {
    return this.http
      .get<UpcomingVisitsResponse>(`${API_URL}/users/me/visits?upcoming=true`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.visits ?? []));
  }

  public getAllVisits() {
    return this.http
      .get<UpcomingVisitsResponse>(`${API_URL}/users/me/visits`, {
        withCredentials: true,
      })
      .pipe(
        map((response) => {
          console.log('All visits response:', response);
          return response.visits ?? [];
        }),
      );
  }

  public cancelVisit(visitId: string) {
    return this.http.delete(`${API_URL}/visits/${visitId}`, {
      withCredentials: true,
    });
  }
}
