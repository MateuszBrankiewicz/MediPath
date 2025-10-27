import { Component, computed, effect, inject } from '@angular/core';
import { Select } from 'primeng/select';
import { TranslationService } from '../../../../../core/services/translation/translation.service';
import { InstitutionStoreService } from '../../../services/institution/institution-store.service';
import { FormsModule } from '@angular/forms';
import { InstitutionOption } from '../../admin-dashboard/widgets/institution-select-card';

@Component({
  selector: 'app-select-institution',
  imports: [Select, FormsModule],
  templateUrl: './select-institution.html',
  styleUrl: './select-institution.scss',
})
export class SelectInstitution {
  protected translationService = inject(TranslationService);
  private readonly institutionStoreService = inject(InstitutionStoreService);
  protected readonly institutionOptions = computed(() =>
    this.institutionStoreService.institutionOptions(),
  );
  protected selectedInstitution = computed(() =>
    this.institutionStoreService.selectedInstitution(),
  );

  constructor() {
    effect(() => {
      const institutions = this.institutionOptions();

      if (institutions.length > 0 && !this.selectedInstitution()) {
        this.institutionStoreService.setInstitution(institutions[0]);
      }
    });
  }

  protected onInstitutionChange($event: InstitutionOption) {
    this.institutionStoreService.setInstitution($event);
  }
}
