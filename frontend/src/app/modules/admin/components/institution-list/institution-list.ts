import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { Institution } from '../../../../core/models/institution.model';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import {
  Hospital,
  HospitalCardComponent,
} from '../../../shared/components/ui/search-result.component/components/hospital-card.component/hospital-card.component';

@Component({
  selector: 'app-institution-list',
  templateUrl: './institution-list.html',
  styleUrl: './institution-list.scss',
  imports: [ProgressSpinnerModule, HospitalCardComponent],
})
export class InstitutionList implements OnInit {
  private readonly translationService = inject(TranslationService);
  protected readonly institutions = signal<Institution[]>([]);
  private readonly institutionService = inject(InstitutionService);
  private readonly destroyRef = inject(DestroyRef);
  protected isLoading = signal<boolean>(true);
  private authService = inject(AuthenticationService);
  private router = inject(Router);
  ngOnInit(): void {
    this.loadInstitutions();
  }

  protected roleCode = computed(() => {
    return this.authService.getLastPanel();
  });

  private loadInstitutions(): void {
    this.isLoading.set(true);
    this.institutionService
      .getInstitutionsForAdmin(this.authService.getLastPanel())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (institutions) => {
          console.log(institutions);
          this.institutions.set(institutions);

          this.isLoading.set(false);
        },
        error: (error) => {
          console.error(
            this.translationService.translate('institutionList.loadError'),
            error,
          );
          this.isLoading.set(false);
        },
        complete: () => this.isLoading.set(false),
      });
  }

  protected getDataForInstitutionCard = computed<Hospital[]>(() => {
    const institutions = this.institutions();
    return institutions.map((inst) => ({
      id: inst?.id ?? '',
      name: inst?.name ?? '',
      address: inst?.address
        ? `${inst.address.street ?? ''} ${inst.address.number} ${inst.address.postalCode} ${inst.address.city}, ${inst.address.province}`.trim()
        : '',
      specialisation: inst?.specialisation ?? [],
      isPublic: inst?.isPublic ?? false,
      imageUrl: inst.image ?? '',
    }));
  });

  protected redirectToInstitutionView(institutionId: string): void {
    this.router.navigate(['/admin/institutions', institutionId]);
  }

  protected isAdminForThisInstitution(): boolean {
    if (this.authService.getLastPanel() !== 'admin') {
      return false;
    }
    return true;
  }

  protected redirectToInstitutionEdit(institutionId: string): void {
    this.router.navigate([`/admin/institutions/${institutionId}/edit`]);
  }
}
