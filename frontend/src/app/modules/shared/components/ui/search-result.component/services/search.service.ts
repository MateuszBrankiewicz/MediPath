import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_URL } from '../../../../../../utils/constants';
import { groupSchedulesByDate } from '../../../../../../utils/scheduleMapper';
import { Hospital } from '../components/hospital-card.component/hospital-card.component';
import { Doctor } from '../search-result.model';

export enum SearchType {
  INSTITUTION = 'institution',
  DOCTOR = 'doctor',
}

export interface SearchResponse {
  result: Doctor[] | Hospital[];
}

export interface SearchQuery {
  query: string;
  category: string;
  location?: string;
  specialization?: string;
}

export interface ApiDoctor {
  name: string;
  numOfRatings: number;
  rating: number;
  surname: string;
  schedules: {
    startTime: string;
    isBooked: boolean;
    id: string;
  }[];
  specialisations: string[];
  addresses: {
    first: {
      institutionId: string;
      institutionName: string;
    };
    second: string;
  }[];
  image: string;
  id: string;
}

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private readonly http = inject(HttpClient);

  public search(searchQuery: SearchQuery) {
    let params = new HttpParams();

    params = params.set('type', searchQuery.category);

    if (searchQuery.location) {
      params = params.set('city', searchQuery.location);
    }

    if (searchQuery.specialization) {
      params = params.set('specialisations', searchQuery.specialization);
    }

    const query = searchQuery.query || '';
    return this.http
      .get<SearchResponse>(`${API_URL}/search/${query}`, {
        params,
      })
      .pipe(
        map((response) => {
          if (searchQuery.category === SearchType.DOCTOR) {
            const doctors = (response.result as unknown[]).map((doc) =>
              this.mapApiDoctorToComponentFormat(doc as ApiDoctor),
            );
            return { result: doctors } as SearchResponse;
          }
          return response;
        }),
      );
  }

  public mapApiDoctorToComponentFormat(apiDoctor: ApiDoctor): Doctor {
    return {
      id: apiDoctor.id,
      name: `${apiDoctor.name} ${apiDoctor.surname}`,
      specialisation: apiDoctor.specialisations.join(', '),
      rating: apiDoctor.rating,
      reviewsCount: apiDoctor.numOfRatings,
      photoUrl: apiDoctor.image || 'assets/imageDoctor.png',
      addresses: apiDoctor.addresses.map((addr) => ({
        address: addr.second,
        institution: {
          institutionId: addr.first.institutionId,
          institutionName: addr.first.institutionName,
        },
      })),
      currentAddressIndex: 0,
      schedule: groupSchedulesByDate(apiDoctor.schedules),
    };
  }
}
