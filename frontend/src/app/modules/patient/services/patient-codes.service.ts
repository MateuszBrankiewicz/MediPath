import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  ApiCodesResponse,
  Refferal,
  UsedState,
} from '../models/refferal-page.model';

@Injectable({
  providedIn: 'root',
})
export class PatientCodesService {
  private http = inject(HttpClient);

  public getPrescriptions() {
    return this.http
      .get<ApiCodesResponse>(`${API_URL}/users/me/codes`, {
        withCredentials: true,
      })
      .pipe(
        map((response): Refferal[] => {
          const codes = response?.codes;
          if (!Array.isArray(codes)) return [];
          return codes.map((item, idx) => ({
            id: idx + 1,
            doctorName: item.doctor ?? '',
            prescriptionPin: Number(item.codes.code),
            status: UsedState.UNUSED,
            date: new Date(item.date),
            codeType: item.codes.codeType,
          }));
        }),
      );
  }

  public useCode(code: {
    codeNumber: number;
    codeType: string;
  }): Observable<boolean> {
    return this.http
      .put(`${API_URL}/visits/code`, code, { withCredentials: true })
      .pipe(
        map(() => true),
        catchError(() => of(false)),
      );
  }
}
