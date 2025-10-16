import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SelectOption } from '../../../modules/shared/components/forms/input-for-auth/select-with-search/select-with-search';
import { API_URL } from '../../../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class AddressService {
  private http = inject(HttpClient);
  public getCities(searchTerm: string) {
    return this.http.get<SelectOption[]>(API_URL + `/cities/${searchTerm}`);
  }

  public getProvinces() {
    return this.http.get<string[]>(API_URL + '/provinces');
  }
}
