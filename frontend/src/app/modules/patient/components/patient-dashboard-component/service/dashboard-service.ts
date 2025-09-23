import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../../../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private http = inject(HttpClient);

  public getPrescriptions() {
    return this.http.get(`${API_URL}/users/me/codes`, {
      withCredentials: true,
    });
  }
}
