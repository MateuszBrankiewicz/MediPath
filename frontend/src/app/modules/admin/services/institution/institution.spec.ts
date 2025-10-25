import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { InstitutionStoreService } from './institution-store.service';

describe('Institution', () => {
  let service: InstitutionStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(InstitutionStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
