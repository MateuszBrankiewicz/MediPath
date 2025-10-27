import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Hospital, HospitalCardComponent } from './hospital-card.component';

describe('HospitalCardComponent', () => {
  let component: HospitalCardComponent;
  let fixture: ComponentFixture<HospitalCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HospitalCardComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(HospitalCardComponent);
    component = fixture.componentInstance;

    const mockHospital: Hospital = {
      id: '1',
      name: 'Test Hospital',
      specialisation: ['Cardiology'],
      address: 'Test Street 1, Test City',
      isPublic: true,
      imageUrl: '',
    };
    fixture.componentRef.setInput('hospital', mockHospital);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
