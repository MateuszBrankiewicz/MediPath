import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PrescriptionPage } from './prescription-page';

describe('PrescriptionPage', () => {
  let component: PrescriptionPage;
  let fixture: ComponentFixture<PrescriptionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrescriptionPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PrescriptionPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
