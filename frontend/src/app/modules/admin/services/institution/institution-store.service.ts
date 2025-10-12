import { Injectable, signal } from '@angular/core';
import { InstitutionShortInfo } from '../../../../core/models/institution.model';

@Injectable({
  providedIn: 'root',
})
export class InstitutionStoreService {
  private readonly institutionId = signal<InstitutionShortInfo>({
    institutionId: '68c5dc05d2569d07e73a8456',
    institutionName: 'Twoja stara',
  });

  public setInstitution(institution: InstitutionShortInfo) {
    this.institutionId.set(institution);
  }

  public getInstitution(): InstitutionShortInfo {
    return this.institutionId();
  }
}
