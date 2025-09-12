import { Component, DestroyRef, effect, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { DataViewModule } from 'primeng/dataview';
import { SearchService } from './services/search.service';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import {
  Hospital,
  HospitalCardComponent,
} from './components/hospital-card.component/hospital-card.component';
import { DoctorCardComponent } from './components/doctor-card.component/doctor-card.component';
import { Doctor, BookAppointment, AddressChange } from './search-result.model';
import { BreadcumbComponent } from '../../breadcumb/breadcumb.component';

@Component({
  selector: 'app-search-result.component',
  imports: [
    DataViewModule,
    ButtonModule,
    HospitalCardComponent,
    DoctorCardComponent,
    BreadcumbComponent,
  ],
  templateUrl: './search-result.component.html',
  styleUrl: './search-result.component.scss',
})
export class SearchResultComponent {
  private searchService = inject(SearchService);
  private route = inject(ActivatedRoute);

  private destroyRef = inject(DestroyRef);

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
    // this.route.paramMap.pipe(
    //   map((params) => ({
    //     query: params.get('query') ?? undefined,
    //   })),
    //   tap((searchQuery) => {
    //     console.log('Route params:', searchQuery);
    //     console.log('Available SearchType values:', Object.values(SearchType));
    //     console.log('Type matches SearchType?');
    //   }),
    //   switchMap((searchQuery) => {
    //     if (searchQuery.query) {
    //       const mappedQuery = {
    //         //byType: mappedType,
    //         query: searchQuery.query,
    //       };

    //       console.log('Mapped search query:', mappedQuery);
    //       console.log('Calling search service with:', mappedQuery);

    //       return this.searchService.getSearchResult(mappedQuery).pipe(
    //         tap((result) => console.log('Search result:', result)),
    //         take(1),
    //         takeUntilDestroyed(this.destroyRef),
    //       );
    //     }

    //     console.log('No valid search parameters, returning empty array');
    //     return of([]);
    //   }),
    // ),
    this.searchService.getSearchResult({ query: 'Szpi' }),
    { initialValue: [] },
  );

  onEditHospital(hospital: Hospital): void {
    console.log('Edit hospital:', hospital);
  }

  onDisableHospital(hospital: Hospital): void {
    console.log('Disable hospital:', hospital);
  }

  onBookAppointment(event: BookAppointment): void {
    console.log('Book appointment:', event);
  }

  onShowMoreInfo(doctor: Doctor): void {
    console.log('Show more info for:', doctor);
  }

  onAddressChange(event: AddressChange): void {
    console.log('Address changed:', event);
  }

  constructor() {
    effect(() => {
      console.log('Search results updated:', this.values());
    });
  }
}
