import { Injectable, signal } from '@angular/core';
import { InstitutionOption } from './../../components/admin-dashboard/widgets/institution-select-card';

@Injectable({
  providedIn: 'root',
})
export class InstitutionStoreService {
  private readonly institution = signal<InstitutionOption>({
    id: '68c5dc05d2569d07e73a8456',
    name: 'Twoja stara',
  });

  private availableInstitutions = signal<InstitutionOption[]>([]);

  public setAvailableInstitutions(institutions: InstitutionOption[]) {
    this.availableInstitutions.set(institutions);
  }

  public getAvailableInstitutions(): InstitutionOption[] {
    return this.availableInstitutions();
  }

  public setInstitution(institution: InstitutionOption) {
    this.institution.set(institution);
  }

  public getInstitution(): InstitutionOption {
    return this.institution();
  }
}
