import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { UpcomingVisitItem } from '../../../modules/admin/components/admin-dashboard/widgets/upcoming-visits-card';
import { API_URL } from '../../../utils/constants';
import { DoctorApiResponse, DoctorProfile } from '../../models/doctor.model';
import {
  AdminInstitutionResponse,
  Institution,
  InstitutionResponse,
} from '../../models/institution.model';
import { UpcomingVisitsResponse } from '../../models/visit.model';
import { AuthenticationService } from '../authentication/authentication';
import { AddDoctorRequest } from '../../models/add-docotr.model';

@Injectable({
  providedIn: 'root',
})
export class InstitutionService {
  private http = inject(HttpClient);
  private authService = inject(AuthenticationService);
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

  public getInstitutionsForAdmin(): Observable<Institution[]> {
    const httpParam = new HttpParams().set('role', 'staff');

    return this.http
      .get<AdminInstitutionResponse>(`${API_URL}/users/me/institutions`, {
        params: httpParam,
        withCredentials: true,
      })
      .pipe(map((res) => res.institutions));
  }

  public getDoctorsForInstitution(
    institutionId: string,
  ): Observable<DoctorProfile[]> {
    return this.http
      .get<DoctorApiResponse>(`${API_URL}/institution/${institutionId}/doctors`)
      .pipe(map((res: DoctorApiResponse): DoctorProfile[] => res.doctors));
  }

  public getUpcomingVisitsForInstitution(
    institutionId: string,
  ): Observable<UpcomingVisitItem[]> {
    return this.http
      .get<UpcomingVisitsResponse>(
        `${API_URL}/institution/${institutionId}/upcomingvisits`,
        {
          withCredentials: true,
        },
      )
      .pipe(
        map((res) =>
          res.visits.map((visit) => ({
            id: visit.id,
            patientName: `${visit.patient.name} ${visit.patient.surname}`,
            time: visit.time.startTime,
            doctorName: `${visit.doctor.doctorName} ${visit.doctor.doctorSurname}`,
            doctorId: visit.doctor.userId,
            date: visit.time.startTime,
          })),
        ),
      );
  }

  public addInstitution(institution: Partial<Institution>): Observable<void> {
    return this.http
      .post<void>(
        `${API_URL}/institution/add`,
        {
          name: institution.name,
          types: institution.specialisation,
          address: institution.address,
          image: institution.image,
          isPublic: institution.isPublic,
        },
        {
          withCredentials: true,
        },
      )
      .pipe(
        map(() => {
          console.log('Institution added successfully');
        }),
      );
  }

  public addEmployee(
    employee: Partial<AddDoctorRequest>,
    id: string,
  ): Observable<unknown> {
    return this.http.post(
      `${API_URL}/institution/${id}/employee/register`,
      employee,
      { withCredentials: true },
    );
  }
}
