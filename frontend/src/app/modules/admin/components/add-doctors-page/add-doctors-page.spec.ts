import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDoctorsPage } from './add-doctors-page';

describe('AddDoctorsPage', () => {
  let component: AddDoctorsPage;
  let fixture: ComponentFixture<AddDoctorsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddDoctorsPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddDoctorsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
