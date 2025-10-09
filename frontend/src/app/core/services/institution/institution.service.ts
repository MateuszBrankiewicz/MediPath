import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { API_URL } from '../../../utils/constants';
import {
  Institution,
  InstitutionResponse,
} from '../../models/institution.model';

@Injectable({
  providedIn: 'root',
})
export class InstitutionService {
  private http = inject(HttpClient);

  public getInstitution(institutionId: string): Observable<Institution> {
    return this.http
      .get<InstitutionResponse>(`${API_URL}/institution/${institutionId}`)
      .pipe(
        map((value: InstitutionResponse): Institution => {
          const inst = value.institution;
          console.log(inst);
          return {
            address: {
              province: inst.address.province,
              number: inst.address.number,
              postalCode: inst.address.postalCode,
              city: inst.address.city,
              street: inst.address?.street ?? undefined,
            },
            employees: inst.employees.map((emp) => ({
              name: emp.name,
              pfpImage: emp.pfpImage,
              roleCode: emp.roleCode,
              specialisation: emp.specialisations,
              surname: emp.surname,
              userId: emp.userId,
            })),
            id: inst.id,
            image: inst.image,
            isPublic: inst.isPublic,
            name: inst.name,
            rating: inst.rating,
            specialisation: inst.types,
          };
        }),
      );
  }
}
