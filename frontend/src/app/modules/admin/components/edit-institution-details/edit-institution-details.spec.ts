import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditInstitutionDetails } from './edit-institution-details';

describe('EditInstitutionDetails', () => {
  let component: EditInstitutionDetails;
  let fixture: ComponentFixture<EditInstitutionDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditInstitutionDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditInstitutionDetails);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
