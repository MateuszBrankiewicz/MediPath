import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../utils/constants';
import { MedicalHistoryApiResponse } from '../models/medical-history.model';

@Injectable({
  providedIn: 'root',
})
export class PatientMedicalHistoryService {
  private http = inject(HttpClient);

  public getMyMedicalHistory() {
    return this.http.get<MedicalHistoryApiResponse>(
      `${API_URL}/users/me/medicalhistory`,
      {
        withCredentials: true,
      },
    );
  }
}
