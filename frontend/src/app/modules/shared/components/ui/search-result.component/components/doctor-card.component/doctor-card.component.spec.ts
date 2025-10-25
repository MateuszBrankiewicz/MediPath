import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Doctor } from '../../search-result.model';
import { DoctorCardComponent } from './doctor-card.component';

describe('DoctorCardComponent', () => {
  let component: DoctorCardComponent;
  let fixture: ComponentFixture<DoctorCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorCardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorCardComponent);
    component = fixture.componentInstance;

    const mockDoctor: Doctor = {
      id: '1',
      name: 'John Doe',
      specialisation: 'Cardiology',
      rating: 4.5,
      reviewsCount: 10,
      photoUrl: '',
      addresses: [
        {
          address: 'Test Street 1, Test City',
          institution: {
            institutionId: 'inst-1',
            institutionName: 'Test Hospital',
          },
        },
      ],
      schedule: [],
      currentAddressIndex: 0,
    };
    fixture.componentRef.setInput('doctor', mockDoctor);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
