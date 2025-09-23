import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReviewVisitDialog } from './review-visit-dialog';

describe('ReviewVisitDialog', () => {
  let component: ReviewVisitDialog;
  let fixture: ComponentFixture<ReviewVisitDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewVisitDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReviewVisitDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
