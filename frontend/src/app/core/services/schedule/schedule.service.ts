import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../utils/constants';
import { CreateScheduleRequest } from '../../models/schedule.model';

@Injectable({
  providedIn: 'root',
})
export class ScheduleService {
  private http = inject(HttpClient);

  public createSchedule(schedule: CreateScheduleRequest) {
    return this.http.post(`${API_URL}/schedules/addmany`, schedule, {
      withCredentials: true,
    });
  }

  public getSchedulesForDoctor(doctorId: string) {
    return this.http.get(`${API_URL}/schedules/doctor/${doctorId}`, {
      withCredentials: true,
    });
  }

  public getSchedulesForInstitution(institutionId: string) {
    const month = new Date().getMonth() + 1;
    const year = new Date().getFullYear();
    const combined = `${month < 10 ? '0' + month : month}-${year}`;
    return this.http.get(
      `${API_URL}/institution/${institutionId}/schedules/${combined}`,
      {
        withCredentials: true,
      },
    );
  }
}
