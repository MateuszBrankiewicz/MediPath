import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
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
  ): Observable<{
    schedules: {
      id: string;
      startHour: string;
      isBooked: boolean;
    }[];
  }> {
    const params = new HttpParams().set('institution', institutionId);
    return this.http.get<{
      schedules: {
        id: string;
        startHour: string;
        isBooked: boolean;
      }[];
    }>(`${API_URL}/doctors/${doctorId}/schedules`, {
      params,
      withCredentials: true,
    });
  }

  public getDoctorSchedulesForDoctorPage(
    institutionId: string,
    doctorId: string,
  ) {
    const params = new HttpParams().set('institution', institutionId);
    return this.http
      .get(`${API_URL}/doctors/${doctorId}/schedules`, {
        params,
        withCredentials: true,
      })
      .pipe(
        tap((response) => console.log('Doctor schedule response:', response)),
      );
  }
}
