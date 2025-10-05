import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  SingleVisitResponse,
  UpcomingVisitsResponse,
} from '../models/visit-page.model';

interface ScheduleVisitRequest {
  scheduleID: string;
  patientRemarks: string;
}

export interface ScheduleResponse {
  schedules: {
    id: string;
    startHour: string;
    endHour: string;
    booked: boolean;
    doctor: {
      userId: string;
      doctorName: string;
      doctorSurname: string;
      specialisations: string[];
      valid: boolean;
    };
    institution: {
      institutionId: string;
      institutionName: string;
      valid: boolean;
    };
  }[];
}

@Injectable({
  providedIn: 'root',
})
export class PatientVisitsService {
  private http = inject(HttpClient);

  public getUpcomingVisits() {
    return this.http
      .get<UpcomingVisitsResponse>(`${API_URL}/users/me/visits?upcoming=true`, {
        withCredentials: true,
      })
      .pipe(map((response) => response.visits ?? []));
  }

  public getAllVisits() {
    return this.http
      .get<UpcomingVisitsResponse>(`${API_URL}/users/me/visits`, {
        withCredentials: true,
      })
      .pipe(
        map((response) => {
          console.log(response);
          return response.visits ?? [];
        }),
      );
  }

  public getVisitDetails(visitId: string) {
    return this.http.get<SingleVisitResponse>(`${API_URL}/visits/${visitId}`, {
      withCredentials: true,
    });
  }

  public cancelVisit(visitId: string) {
    return this.http.delete(`${API_URL}/visits/${visitId}`, {
      withCredentials: true,
    });
  }

  public scheduleVisit(scheduleVisit: ScheduleVisitRequest) {
    return this.http.post(`${API_URL}/visits/add`, scheduleVisit, {
      withCredentials: true,
    });
  }

  public rescheduleVisit(scheduleVisit: ScheduleVisitRequest) {
    return this.http.post(
      `${API_URL}/visits/${scheduleVisit.scheduleID}/reschedule`,
      { patientRemarks: scheduleVisit.patientRemarks },
      { withCredentials: true },
    );
  }

  public getDoctorSchedule(doctorId: string) {
    return this.http.get<ScheduleResponse>(
      `${API_URL}/schedules/bydoctor/${doctorId}`,
      {
        withCredentials: true,
      },
    );
  }
}
