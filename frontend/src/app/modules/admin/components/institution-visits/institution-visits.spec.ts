import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InstitutionVisits } from './institution-visits';

describe('InstitutionVisits', () => {
  let component: InstitutionVisits;
  let fixture: ComponentFixture<InstitutionVisits>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionVisits],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionVisits);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
