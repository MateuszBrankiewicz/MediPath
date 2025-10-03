import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../utils/constants';
import {
  MedicalHistoryApiRequest,
  MedicalHistoryApiResponse,
} from '../models/medical-history.model';

export interface MedicalHistoryEntry {
  date: string;
  description: string;
}

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

  public addMedicalHistoryEntry(entry: MedicalHistoryApiRequest) {
    console.log('Adding medical history entry:', entry);
    return this.http.post(`${API_URL}/medicalhistory/add`, entry, {
      withCredentials: true,
    });
  }
}
