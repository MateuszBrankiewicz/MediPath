import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { API_URL } from '../../../utils/constants';
import {
  MedicalHistoryApiRequest,
  MedicalHistoryApiResponse,
} from '../../models/medical-history.model';
import { Observable } from 'rxjs';

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
}
