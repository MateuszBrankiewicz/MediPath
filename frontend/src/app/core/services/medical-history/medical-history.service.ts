import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { map, Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  MedicalHistoryApiRequest,
  MedicalHistoryApiResponse,
  MedicalHistoryResponse,
} from '../../models/medical-history.model';

@Injectable({
  providedIn: 'root',
})
export class MedicalHistoryService {
  private http = inject(HttpClient);

  public getMyMedicalHistory(): Observable<MedicalHistoryApiResponse> {
    return this.http.get<MedicalHistoryApiResponse>(
      `${API_URL}/users/me/medicalhistory`,
      {
        withCredentials: true,
      },
    );
  }

  public addMedicalHistoryEntry(entry: MedicalHistoryApiRequest) {
    return this.http.post(`${API_URL}/medicalhistory/add`, entry, {
      withCredentials: true,
    });
  }

  public getPatientMedicalHistory(
    patientId: string,
  ): Observable<MedicalHistoryResponse[]> {
    return this.http
      .get<MedicalHistoryApiResponse>(
        `${API_URL}/users/${patientId}/medicalhistory`,
        {
          withCredentials: true,
        },
      )
      .pipe(map((response) => response.medicalhistories));
  }
}
