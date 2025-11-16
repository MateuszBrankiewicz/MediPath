import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { UpcomingVisitItem } from '../../../modules/admin/components/admin-dashboard/widgets/upcoming-visits-card';
import { API_URL } from '../../../utils/constants';
import { AddDoctorRequest } from '../../models/add-docotr.model';
import {
  DoctorApiResponse,
  DoctorProfile,
  FindedEmployee,
} from '../../models/doctor.model';
import {
  AdminInstitutionResponse,
  Institution,
  InstitutionResponse,
} from '../../models/institution.model';
import {
  UpcomingVisitsResponse,
  VisitApiResponseArray,
  VisitResponse,
} from '../../models/visit.model';
import { UserRoles } from '../authentication/authentication.model';

export interface UpdateEmployeeRequest {
  userID: string;
  roleCode: number;
  specialisations?: string[];
}

export interface AddEmployeesRequest {
  userID: string;
  rolecode: number;
  specialisations?: string[];
}

@Injectable({
  providedIn: 'root',
})
export class InstitutionService {
  private http = inject(HttpClient);
  public getInstitution(institutionId: string): Observable<Institution> {
    return this.http
      .get<InstitutionResponse>(`${API_URL}/institution/${institutionId}`, {
        withCredentials: true,
      })
      .pipe(
        map((value: InstitutionResponse): Institution => {
          const inst = value.institution;
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

  public getInstitutionsForAdmin(
    role: UserRoles | null,
  ): Observable<Institution[]> {
    let httpParam;
    if (role === 'admin') {
      httpParam = new HttpParams().set('role', 'admin');
    } else {
      httpParam = new HttpParams().set('role', 'staff');
    }
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

  public getEmployeesForInstitution(
    institutionId: string,
  ): Observable<DoctorProfile[]> {
    return this.http
      .get<DoctorApiResponse>(
        `${API_URL}/institution/${institutionId}/employees`,
        { withCredentials: true },
      )
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
    return this.http.post<void>(
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
    );
  }
  public editInstitution(
    institutionId: string,
    institution: Partial<Institution>,
  ): Observable<void> {
    return this.http.put<void>(
      `${API_URL}/institution/${institutionId}/`,
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

  public getVisits(institutionId: string): Observable<unknown> {
    return this.http.get(`${API_URL}/institution/${institutionId}/visits`, {
      withCredentials: true,
    });
  }

  public updateEmployee(
    institutionId: string,
    employeeData: UpdateEmployeeRequest,
  ): Observable<void> {
    return this.http.put<void>(
      `${API_URL}/institution/${institutionId}/employee/`,
      employeeData,
      { withCredentials: true },
    );
  }

  public deleteEmployee(
    institutionId: string,
    userId: string,
  ): Observable<void> {
    return this.http.delete<void>(
      `${API_URL}/institution/${institutionId}/employee/${userId}`,
      { withCredentials: true },
    );
  }

  public addEmployees(
    institutionId: string,
    employees: AddEmployeesRequest[],
  ): Observable<void> {
    return this.http.post<void>(
      `${API_URL}/institution/${institutionId}/employees/`,
      employees,
      { withCredentials: true },
    );
  }

  public getVisitsForInstitution(
    institutionId: string,
  ): Observable<VisitResponse[]> {
    return this.http
      .get<VisitApiResponseArray>(
        `${API_URL}/institution/${institutionId}/visits`,
        {
          withCredentials: true,
        },
      )
      .pipe(map((response) => response.visits));
  }

  public findUserByGovId(govId: string): Observable<FindedEmployee> {
    return this.http.get<FindedEmployee>(`${API_URL}/users/find/${govId}`, {
      withCredentials: true,
    });
  }

  public updatePwzNumber(userId: string, pwzNumber: string) {
    return this.http.put<void>(
      `${API_URL}/doctors/${userId}`,
      { licenceNumber: pwzNumber },
      { withCredentials: true },
    );
  }

  public deactivateInstitution(institutionId: string): Observable<void> {
    return this.http.post<void>(
      `${API_URL}/institution/${institutionId}/deactivate`,
      {},
      { withCredentials: true },
    );
  }
}
