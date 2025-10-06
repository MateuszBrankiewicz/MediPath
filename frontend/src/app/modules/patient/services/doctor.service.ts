import { HttpClient } from '@angular/common/http';
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
}
