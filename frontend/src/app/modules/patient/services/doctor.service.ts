import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private http = inject(HttpClient);
  public getDoctorDetails(doctorId: string) {
    return this.http.get(`${API_URL}/doctors/${doctorId}`);
  }

  public getDoctorScheduleByInstitution(
    institutionId: string,
    doctorId: string,
  ) {
    const params = new HttpParams().set('institution', institutionId);
    return this.http.get(`${API_URL}/doctors/${doctorId}/schedule`, { params });
  }
}
