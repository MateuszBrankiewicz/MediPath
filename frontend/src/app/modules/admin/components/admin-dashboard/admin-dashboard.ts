import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { InstitutionService } from '../../../../core/services/institution/institution.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { InstitutionStoreService } from './../../services/institution/institution-store.service';
import { CommentItem, CommentsCard } from './widgets/comments-card';
import {
  InstitutionOption,
  InstitutionSelectCard,
} from './widgets/institution-select-card';
import {
  UpcomingVisitItem,
  UpcomingVisitsCard,
} from './widgets/upcoming-visits-card';

@Component({
  selector: 'app-admin-dashboard',
  imports: [
    CommonModule,
    InstitutionSelectCard,
    CommentsCard,
    UpcomingVisitsCard,
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboard {
  protected translationService = inject(TranslationService);
  private institutionStoreService = inject(InstitutionStoreService);
  private institutionService = inject(InstitutionService);
  protected readonly institutions = signal<InstitutionOption[]>([
    {
      id: '68c5dc05d2569d07e73a8456',
      name: 'Uniwersytecki Szpital Kliniczny nr 1',
    },
    { id: '68c5dc05d2569d07e73a8456', name: 'Instytut Kardiologii' },
  ]);

  protected readonly comments = signal<CommentItem[]>([
    { id: 1, content: 'Ale super klinika moje serce jest w niebie' },
    { id: 2, content: 'Lekarz bardzo miły obsługa również pozdrawiam!' },
    { id: 3, content: 'Jedzenie takie średnia ale ujdzie' },
  ]);

  protected readonly upcomingVisits = signal<UpcomingVisitItem[]>([
    { id: 1, time: '8:00 am', patientName: 'Kazimierz Nowak' },
    { id: 2, time: '1:00 pm', patientName: 'Piotr Nowak' },
  ]);

  protected onInstitutionChanged(
    optionSelected: InstitutionOption | null,
  ): void {
    if (!optionSelected) {
      return;
    }
    this.institutionStoreService.setInstitution({
      institutionId: optionSelected.id,
      institutionName: optionSelected.name,
    });
  }

  protected onChangeDoctor(_visit: UpcomingVisitItem): void {
    console.log('Change doctor clicked', _visit);
  }

  protected onCancelVisit(_visit: UpcomingVisitItem): void {
    // TODO: call cancel visit flow
    console.log('Cancel visit clicked', _visit);
  }

  selectedInstitution = computed(() => {
    const institutionsList = this.institutions();
    const currentInstitution = this.institutionStoreService.getInstitution();
    if (!institutionsList || !currentInstitution) return undefined;
    // Adjust property names as needed
    return institutionsList.find(
      (i) => i.id === currentInstitution.institutionId,
    );
  });
}
