import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { TranslationService } from '../../../../core/services/translation/translation.service';
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

  protected readonly institutions = signal<InstitutionOption[]>([
    { id: '1', name: 'Uniwersytecki Szpital Kliniczny nr 1' },
    { id: '2', name: 'Instytut Kardiologii' },
  ]);
  protected readonly selectedInstitutionId = signal<string | null>('1');

  protected readonly comments = signal<CommentItem[]>([
    { id: 1, content: 'Ale super klinika moje serce jest w niebie' },
    { id: 2, content: 'Lekarz bardzo miły obsługa również pozdrawiam!' },
    { id: 3, content: 'Jedzenie takie średnia ale ujdzie' },
  ]);

  protected readonly upcomingVisits = signal<UpcomingVisitItem[]>([
    { id: 1, time: '8:00 am', patientName: 'Kazimierz Nowak' },
    { id: 2, time: '1:00 pm', patientName: 'Piotr Nowak' },
  ]);

  protected onInstitutionChanged(id: string): void {
    this.selectedInstitutionId.set(id);
    // TODO: fetch data for new institution
  }

  protected onChangeDoctor(_visit: UpcomingVisitItem): void {
    // TODO: navigate to assign/change doctor flow
    console.log('Change doctor clicked', _visit);
  }

  protected onCancelVisit(_visit: UpcomingVisitItem): void {
    // TODO: call cancel visit flow
    console.log('Cancel visit clicked', _visit);
  }
}
