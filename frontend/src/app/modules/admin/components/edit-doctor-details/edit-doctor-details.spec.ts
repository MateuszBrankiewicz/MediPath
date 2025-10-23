import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditDoctorDetails } from './edit-doctor-details';

describe('EditDoctorDetails', () => {
  let component: EditDoctorDetails;
  let fixture: ComponentFixture<EditDoctorDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditDoctorDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditDoctorDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
