import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { DataViewModule } from 'primeng/dataview';
import { SearchService, SearchType } from './services/search.service';
import { ActivatedRoute } from '@angular/router';
import { map, switchMap, of } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import {
  Hospital,
  HospitalCardComponent,
} from './components/hospital-card.component/hospital-card.component';
import { DoctorCardComponent } from './components/doctor-card.component/doctor-card.component';
import { Doctor, BookAppointment, AddressChange } from './search-result.model';

@Component({
  selector: 'app-search-result.component',
  imports: [
    DataViewModule,
    ButtonModule,
    HospitalCardComponent,
    DoctorCardComponent,
  ],
  templateUrl: './search-result.component.html',
  styleUrl: './search-result.component.scss',
})
export class SearchResultComponent {
  private searchService = inject(SearchService);
  private route = inject(ActivatedRoute);

  sampleHospital: Hospital = {
    id: 1,
    name: 'Szpital kliniczny',
    address: 'Jana Pawła II 25, 23-200 Lublin',
    specialisation: ['Oncologist', 'Cardiologist'],
    isPublic: true,
    imageUrl: 'assets/footer-landing.png',
  };
  sampleDoctor: Doctor = {
    id: 1,
    name: 'Dr. Jadwiga Chymyl',
    specialisation: 'Cardiologist',
    rating: 5,
    reviewsCount: 120,
    photoUrl: 'assets/footer-landing.png',
    addresses: [
      'Jana Pawła II 4/32, Lublin',
      'Szpital Kliniczny nr 1',
      'Centrum Medyczne',
    ],
    currentAddressIndex: 0,
    schedule: [
      {
        date: '2024-04-02',
        dayName: 'Today',
        dayNumber: '2 Apr',
        slots: [
          { time: '12:00AM', available: false },
          { time: '12:20AM', available: true },
          { time: '12:40AM', available: true },
          { time: '1:00PM', available: true },
          { time: '1:20PM', available: false },
          { time: '1:40PM', available: true },
        ],
      },
      {
        date: '2024-04-03',
        dayName: 'Tomorrow',
        dayNumber: '3 Apr',
        slots: [
          { time: '12:00AM', available: true },
          { time: '12:20AM', available: true },
          { time: '12:40AM', available: false },
          { time: '1:00PM', available: true },
          { time: '1:20PM', available: true },
          { time: '1:40PM', available: true },
        ],
      },
      {
        date: '2024-04-05',
        dayName: 'Friday',
        dayNumber: '4 Apr',
        slots: [
          { time: '12:00AM', available: true },
          { time: '12:20AM', available: true },
          { time: '12:40AM', available: true },
          { time: '1:00PM', available: false },
          { time: '1:20PM', available: true },
          { time: '1:40PM', available: true },
        ],
      },
      {
        date: '2024-04-05',
        dayName: 'Friday',
        dayNumber: '4 Apr',
        slots: [
          { time: '12:00AM', available: true },
          { time: '12:20AM', available: true },
          { time: '12:40AM', available: true },
          { time: '1:00PM', available: false },
          { time: '1:20PM', available: true },
          { time: '1:40PM', available: true },
        ],
      },
    ],
  };
  protected readonly values = toSignal(
    this.route.paramMap.pipe(
      map((params) => ({
        byType: params.get('type') as SearchType,
        query: params.get('query') ?? undefined,
      })),
      switchMap((searchQuery) => {
        if (
          searchQuery.byType &&
          searchQuery.query &&
          Object.values(SearchType).includes(searchQuery.byType)
        ) {
          console.log(this.searchService.getSearchResult(searchQuery));
          return this.searchService.getSearchResult(searchQuery);
        }
        return of([]);
      }),
    ),
    { initialValue: [] },
  );

  onEditHospital(hospital: Hospital): void {
    console.log('Edit hospital:', hospital);
    // Implementuj logikę edycji
  }

  onDisableHospital(hospital: Hospital): void {
    console.log('Disable hospital:', hospital);
    // Implementuj logikę wyłączania
  }

  onBookAppointment(event: BookAppointment): void {
    console.log('Book appointment:', event);
    // Implementuj logikę rezerwacji
  }

  onShowMoreInfo(doctor: Doctor): void {
    console.log('Show more info for:', doctor);
    // Implementuj logikę pokazania więcej informacji
  }

  onAddressChange(event: AddressChange): void {
    console.log('Address changed:', event);
    // Implementuj logikę zmiany adresu
  }
}
