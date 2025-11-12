import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import { SpecialisationResponse } from '../../models/specialisation.model';

@Injectable({
  providedIn: 'root',
})
export class SpecialisationService {
  private http = inject(HttpClient);

  public getSpecialisations(
    institution: boolean,
  ): Observable<SpecialisationResponse[]> {
    const httpParam = institution
      ? '?isInsitutionType=true'
      : '?isInsitutionType=false';
    return this.http.get<SpecialisationResponse[]>(
      `${API_URL}/specialisations${httpParam}`,
    );
  }
}
