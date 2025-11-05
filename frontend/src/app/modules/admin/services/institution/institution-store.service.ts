import { effect, inject, Injectable, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, Observable, shareReplay } from 'rxjs';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { InstitutionOption } from './../../components/admin-dashboard/widgets/institution-select-card';

@Injectable({
  providedIn: 'root',
})
export class InstitutionStoreService {
  private readonly institution = signal<InstitutionOption | null>(null);
  private authentication = inject(AuthenticationService);
  public isInstitutionLoading = signal<boolean>(true);

  private institutionService = inject(InstitutionService);
  private availableInstitutions = signal<InstitutionOption[]>([]);

  private availableInstitutions$ = this.institutionService
    .getInstitutionsForAdmin(this.authentication.getLastPanel())
    .pipe(
      map((institutions) =>
        institutions.map((inst) => ({
          id: inst.id,
          name: inst.name,
        })),
      ),
      shareReplay(1),
    );

  public institutionOptions = toSignal(this.availableInstitutions$, {
    initialValue: [] as InstitutionOption[],
  });

  public selectedInstitution = this.institution.asReadonly();

  constructor() {
    effect(() => {
      if (this.institutionOptions().length > 0) {
        this.isInstitutionLoading.set(false);
      }
    });
  }

  public getAvailableInstitutions(): InstitutionOption[] {
    return this.availableInstitutions();
  }

  public loadAvailableInstitutions(): Observable<InstitutionOption[]> {
    this.isInstitutionLoading.set(true);
    return this.institutionService
      .getInstitutionsForAdmin(this.authentication.getLastPanel())
      .pipe(
        map((institutions) => {
          const formattedInstitutions = institutions.map((inst) => ({
            id: inst.id,
            name: inst.name,
          }));
          this.availableInstitutions.set(formattedInstitutions);
          this.isInstitutionLoading.set(false);
          return formattedInstitutions;
        }),
      );
  }

  public setInstitution(institution: InstitutionOption) {
    this.institution.set(institution);
  }

  public getInstitution(): InstitutionOption {
    return this.institution() ? this.institution()! : { id: '', name: '' };
  }
}
