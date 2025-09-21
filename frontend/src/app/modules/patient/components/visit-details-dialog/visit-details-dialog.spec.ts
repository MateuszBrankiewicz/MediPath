import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisitDetailsDialog } from './visit-details-dialog';

describe('VisitDetailsDialog', () => {
  let component: VisitDetailsDialog;
  let fixture: ComponentFixture<VisitDetailsDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisitDetailsDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisitDetailsDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
