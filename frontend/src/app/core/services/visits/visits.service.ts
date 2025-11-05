import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  ScheduleResponse,
  ScheduleVisitRequest,
} from '../../models/schedule.model';
import {
  SingleVisitResponse,
  UpcomingVisitsResponse,
  VisitResponse,
} from '../../models/visit.model';
import { FinishVisitResponse } from './../../models/visit.model';

@Injectable({
  providedIn: 'root',
})
export class VisitsService {
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

  public rescheduleVisit(scheduleVisit: ScheduleVisitRequest, visitId: string) {
    return this.http.put(
      `${API_URL}/visits/${visitId}/reschedule`,
      {
        patientRemarks: scheduleVisit.patientRemarks,
        newschedule: scheduleVisit.scheduleID,
      },
      { withCredentials: true },
    );
  }

  public getDoctorSchedule(doctorId: string) {
    return this.http.get<ScheduleResponse>(
      `${API_URL}/doctors/${doctorId}/schedules`,
      {
        withCredentials: true,
      },
    );
  }

  public getDoctorVisitByDate(date: string): Observable<VisitResponse[]> {
    return this.http
      .get<UpcomingVisitsResponse>(
        `${API_URL}/doctors/me/visitsbydate/${date}`,
        {
          withCredentials: true,
        },
      )
      .pipe(map((response) => response.visits ?? []));
  }

  public finishVisit(
    finishVisit: FinishVisitResponse,
    visitId: string,
  ): Observable<void> {
    return this.http.put<void>(
      `${API_URL}/visits/${visitId}/complete`,
      {
        prescriptions: finishVisit.prescriptions,
        referrals: finishVisit.referrals,
        note: finishVisit.note,
      },
      { withCredentials: true },
    );
  }
}
