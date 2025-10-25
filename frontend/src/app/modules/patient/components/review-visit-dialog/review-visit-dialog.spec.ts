import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { vi } from 'vitest';
import { ReviewVisitDialog } from './review-visit-dialog';

describe('ReviewVisitDialog', () => {
  let component: ReviewVisitDialog;
  let fixture: ComponentFixture<ReviewVisitDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReviewVisitDialog],
      providers: [
        { provide: DynamicDialogRef, useValue: { close: vi.fn() } },
        { provide: DynamicDialogConfig, useValue: { data: {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReviewVisitDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
