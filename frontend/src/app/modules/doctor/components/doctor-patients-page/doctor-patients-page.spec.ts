import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoctorPatientsPage } from './doctor-patients-page';

describe('DoctorPatientsPage', () => {
  let component: DoctorPatientsPage;
  let fixture: ComponentFixture<DoctorPatientsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorPatientsPage],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorPatientsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
