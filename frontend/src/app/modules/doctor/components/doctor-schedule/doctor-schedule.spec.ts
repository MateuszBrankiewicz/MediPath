import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoctorSchedule } from './doctor-schedule';

describe('DoctorSchedule', () => {
  let component: DoctorSchedule;
  let fixture: ComponentFixture<DoctorSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
