import {
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { DataViewModule } from 'primeng/dataview';
import {
  SearchQuery,
  SearchResponse,
  SearchService,
} from './services/search.service';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import {
  Hospital,
  HospitalCardComponent,
} from './components/hospital-card.component/hospital-card.component';
import { DoctorCardComponent } from './components/doctor-card.component/doctor-card.component';
import { Doctor, BookAppointment, AddressChange } from './search-result.model';
import { BreadcumbComponent } from '../../breadcumb/breadcumb.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
export class SearchResultComponent implements OnInit {
  private searchService = inject(SearchService);
  private route = inject(ActivatedRoute);

  protected readonly category = signal('');

  protected readonly values = signal<SearchResponse | null>(null);

  protected readonly hospitals = computed(() => {
    const results = this.values();
    if (this.category() === 'institution' && results?.result) {
      return results.result as Hospital[];
    }
    return [];
  });

  protected readonly doctors = computed(() => {
    const results = this.values();
    if (this.category() === 'doctor' && results?.result) {
      return results.result as Doctor[];
    }
    return [];
  });

  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const query = params['query'] || '';
      const category = params['category'] || '';
      const location = params['location'] || '';
      const specialization = params['specialization'] || '';
      this.category.set(category);
      this.performSearch({ query, category, location, specialization });
    });
  }

  protected performSearch(params: SearchQuery): void {
    this.searchService
      .search(params)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((results) => {
        console.log('Search results:', results);
        this.values.set(results);
      });
  }

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
}
