import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  DoctorDetailsApiResponse,
  DoctorPageModel,
  DoctorPatientsApiResponse,
  DoctorPatientsVisitApiResponse,
  PatientForDoctor,
  VisitsForPatientProfile,
} from '../../models/doctor.model';
import { DoctorScheduleResponse, InputSlot } from '../../models/schedule.model';
import { VisitApiResponseArray, VisitResponse } from '../../models/visit.model';

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
          return mapped;
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
      schedule: [],
      comments: [],
    };
  }

  public getDoctorsSchedule(): Observable<InputSlot[]> {
    return this.http
      .get<DoctorScheduleResponse>(`${API_URL}/doctors/me/schedules`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.schedules));
  }

  public getDoctorVisits(): Observable<VisitResponse[]> {
    return this.http
      .get<VisitApiResponseArray>(`${API_URL}/doctors/me/visits`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.visits));
  }

  public getDoctorPatients(): Observable<PatientForDoctor[]> {
    return this.http
      .get<DoctorPatientsApiResponse>(`${API_URL}/doctors/me/patients`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.patients));
  }

  public getPatientProfile(patientId: string): Observable<unknown> {
    return this.http.get(`${API_URL}/users/patients/${patientId}`, {
      withCredentials: true,
    });
  }

  public getPatientVisits(
    patientId: string,
  ): Observable<VisitsForPatientProfile[]> {
    return this.http
      .get<DoctorPatientsVisitApiResponse>(
        `${API_URL}/doctors/me/patients/${patientId}/visits`,
        {
          withCredentials: true,
        },
      )
      .pipe(map((response) => response.visits));
  }
}
