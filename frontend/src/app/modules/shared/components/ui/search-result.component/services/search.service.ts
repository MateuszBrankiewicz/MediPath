import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../../../../utils/constants';
import { Doctor } from '../search-result.model';
import { Hospital } from '../components/hospital-card.component/hospital-card.component';
import { map } from 'rxjs';

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

// Format który przychodzi z API (ten który mi wysłałeś)
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
    first: string;
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
    console.log('Search query:', query, params);
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
        institution: addr.first,
      })),
      currentAddressIndex: 0,
      schedule: this.groupSchedulesByDate(apiDoctor.schedules),
    };
  }

  private groupSchedulesByDate(
    schedules: { startTime: string; isBooked: boolean; id: string }[],
  ) {
    const grouped = new Map<
      string,
      { startTime: string; isBooked: boolean; id: string }[]
    >();

    schedules.forEach((slot) => {
      const date = new Date(slot.startTime);
      const dateKey = date.toISOString().split('T')[0];

      if (!grouped.has(dateKey)) {
        grouped.set(dateKey, []);
      }
      grouped.get(dateKey)!.push(slot);
    });

    return Array.from(grouped.entries()).map(([dateKey, slots]) => {
      const date = new Date(dateKey);
      const dayNames = [
        'Niedziela',
        'Poniedziałek',
        'Wtorek',
        'Środa',
        'Czwartek',
        'Piątek',
        'Sobota',
      ];

      return {
        date: dateKey, // "2026-02-01"
        dayName: dayNames[date.getDay()], // "Sobota"
        dayNumber: date.getDate().toString(), // "1" (jako string)
        slots: slots.map((slot) => {
          const slotDate = new Date(slot.startTime);
          const timeString = slotDate.toLocaleTimeString('pl-PL', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
          });

          return {
            time: timeString,
            available: !slot.isBooked,
            booked: slot.isBooked,
          };
        }),
      };
    });
  }
}
