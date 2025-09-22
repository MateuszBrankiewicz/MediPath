import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoctorPatientsPage } from './doctor-patients-page';

describe('DoctorPatientsPage', () => {
  let component: DoctorPatientsPage;
  let fixture: ComponentFixture<DoctorPatientsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorPatientsPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoctorPatientsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
