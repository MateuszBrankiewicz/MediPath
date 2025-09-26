import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class PatientVisitsService {
  private http = inject(HttpClient);

  public getUpcomingVisits() {
    return this.http.get(`${API_URL}/users/me/visits`, {
      withCredentials: true,
    });
  }
}
