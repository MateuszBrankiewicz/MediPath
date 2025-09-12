import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../../../../utils/constants';

export enum SearchType {
  INSTITUTION = 'institution',
  DOCTOR = 'doctor',
}

export interface SearchQuery {
  byType?: SearchType;
  query?: string;
}

@Injectable({
  providedIn: 'root',
})
export class SearchService {
  private readonly http = inject(HttpClient);

  public getSearchResult(searchQuery: SearchQuery) {
    console.log(searchQuery);
    return this.http.get(API_URL + `/search/${searchQuery.query}`);
  }
}
