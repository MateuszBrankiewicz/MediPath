import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DoctorView } from './doctor-view';

describe('DoctorView', () => {
  let component: DoctorView;
  let fixture: ComponentFixture<DoctorView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DoctorView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DoctorView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
