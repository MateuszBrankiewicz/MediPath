import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputGroupModule } from 'primeng/inputgroup';
import { InputGroupAddonModule } from 'primeng/inputgroupaddon';
import { InputTextModule } from 'primeng/inputtext';
import { MenuModule } from 'primeng/menu';
import { Menubar } from 'primeng/menubar';
import { TranslationService } from '../../../../core/services/translation/translation.service';

enum LandingPageSelectedTab {
  FOR_INSTITUTIONS = 'for_institutions',
  FOR_DOCTORS = 'for_doctors',
  FOR_PATIENTS = 'for_patients',
}

@Component({
  selector: 'app-landing-page-component',
  imports: [
    Menubar,
    Button,
    Card,
    RouterLink,
    InputGroupModule,
    InputGroupAddonModule,
    InputTextModule,
    FormsModule,
    MenuModule,
  ],
  templateUrl: './landing-page-component.html',
  styleUrl: './landing-page-component.scss',
})
export class LandingPageComponent {
  private readonly router = inject(Router);
  private readonly translationService = inject(TranslationService);

  protected readonly selectedTab = signal(
    LandingPageSelectedTab.FOR_INSTITUTIONS,
  );
  protected readonly LandingPageSelectedTab = LandingPageSelectedTab;

  // Search state
  protected readonly selectedSearchType = signal<'institution' | 'doctor'>(
    'institution',
  );
  protected readonly query = signal('');
  protected readonly locationQuery = signal('');
  protected readonly specializationQuery = signal('');

  protected readonly searchMenuItems = computed<MenuItem[]>(() => [
    {
      label: this.translationService.translate(
        'shared.search.searchByInstitution',
      ),
      icon: 'pi pi-building',
      command: () => {
        this.selectedSearchType.set('institution');
      },
    },
    {
      label: this.translationService.translate('shared.search.searchByDoctor'),
      icon: 'pi pi-user-md',
      command: () => {
        this.selectedSearchType.set('doctor');
      },
    },
  ]);

  protected getSearchPlaceholder(): string {
    if (this.selectedSearchType() === 'institution') {
      return this.translationService.translate(
        'shared.search.searchInstitutionPlaceholder',
      );
    }
    return this.translationService.translate(
      'shared.search.searchDoctorPlaceholder',
    );
  }

  protected setTab(tab: LandingPageSelectedTab): void {
    this.selectedTab.set(tab);
  }

  protected search(): void {
    const queryParams: Record<string, string> = {
      query: this.query(),
      category: this.selectedSearchType(),
    };

    if (this.locationQuery()) {
      queryParams['location'] = this.locationQuery();
    }

    if (this.specializationQuery()) {
      queryParams['specialization'] = this.specializationQuery();
    }

    this.router.navigate(['/search'], { queryParams });
  }
}
