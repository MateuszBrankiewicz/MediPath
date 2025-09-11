import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../../../../../../utils/constants';

export enum SearchType {
  ByType = 'by_type',
  ByName = 'by_name',
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
    return this.http.get(
      API_URL + `/search/${searchQuery.byType}/${searchQuery.query}`,
    );
  }
}
