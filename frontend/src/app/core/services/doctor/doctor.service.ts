import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  DoctorDetailsApiResponse,
  DoctorPageModel,
} from '../../models/doctor.model';
import { DoctorScheduleResponse, InputSlot } from '../../models/schedule.model';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private http = inject(HttpClient);

  public getDoctorDetails(doctorId: string): Observable<DoctorPageModel> {
    return this.http
      .get<DoctorDetailsApiResponse>(`${API_URL}/doctors/${doctorId}`)
      .pipe(
        map((response) => {
          const mapped = this.mapDoctorDetailsResponse(response);
          console.log('Mapped doctor details:', mapped);
          return this.mapDoctorDetailsResponse(response);
        }),
      );
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
    return this.http
      .get<{
        schedules: {
          id: string;
          startHour: string;
          booked: boolean;
        }[];
      }>(`${API_URL}/doctors/${doctorId}/schedules`, {
        params,
        withCredentials: true,
      })
      .pipe(
        map((response) => ({
          schedules: response.schedules.map((schedule) => ({
            id: schedule.id,
            startHour: schedule.startHour,
            isBooked: schedule.booked,
          })),
        })),
      );
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

  private mapDoctorDetailsResponse(
    response: DoctorDetailsApiResponse,
  ): DoctorPageModel {
    console.log('Mapping doctor details response:', response);
    return {
      name: response.doctor.name,
      surname: response.doctor.surname,
      photoUrl: 'assets/footer-landing.png',
      pwz: response.doctor.licence_number,
      rating: {
        stars: 4.5,
        opinions: 20,
      },
      institutions: response.doctor.employers,
      specialisation: response.doctor.specialisations,
      schedule: [], // Schedule will be fetched separately
      comments: [], // Comments will be fetched separately
    };
  }

  public getDoctorsSchedule(): Observable<InputSlot[]> {
    return this.http
      .get<DoctorScheduleResponse>(`${API_URL}/doctors/me/schedules`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.schedules));
  }
}
