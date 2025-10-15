import { inject, Injectable, signal } from '@angular/core';
import { map, Observable } from 'rxjs';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { InstitutionOption } from './../../components/admin-dashboard/widgets/institution-select-card';

@Injectable({
  providedIn: 'root',
})
export class InstitutionStoreService {
  private readonly institution = signal<InstitutionOption>({
    id: '68c5dc05d2569d07e73a8456',
    name: 'Twoja stara',
  });
  private institutionService = inject(InstitutionService);
  private availableInstitutions = signal<InstitutionOption[]>([]);

  public setAvailableInstitutions(institutions: InstitutionOption[]) {
    this.availableInstitutions.set(institutions);
  }

  public getAvailableInstitutions(): InstitutionOption[] {
    return this.availableInstitutions();
  }

  public loadAvailableInstitutions(): Observable<InstitutionOption[]> {
    return this.institutionService.getInstitutionsForAdmin().pipe(
      map((institutions) => {
        const formattedInstitutions = institutions.map((inst) => ({
          id: inst.id,
          name: inst.name,
        }));
        this.availableInstitutions.set(formattedInstitutions);
        return formattedInstitutions;
      }),
    );
  }

  public setInstitution(institution: InstitutionOption) {
    this.institution.set(institution);
  }

  public getInstitution(): InstitutionOption {
    return this.institution();
  }
}
