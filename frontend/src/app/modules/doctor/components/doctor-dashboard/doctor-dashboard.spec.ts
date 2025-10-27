import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoctorDashboard } from './doctor-dashboard';

describe('DoctorDashboard', () => {
  let component: DoctorDashboard;
  let fixture: ComponentFixture<DoctorDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorDashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorDashboard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
