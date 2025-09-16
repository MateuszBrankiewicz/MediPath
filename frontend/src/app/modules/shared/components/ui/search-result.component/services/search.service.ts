import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../../../../utils/constants';
import { Doctor } from '../search-result.model';
import { Hospital } from '../components/hospital-card.component/hospital-card.component';

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
    return this.http.get<SearchResponse>(`${API_URL}/search/${query}`, {
      params,
    });
  }
}
