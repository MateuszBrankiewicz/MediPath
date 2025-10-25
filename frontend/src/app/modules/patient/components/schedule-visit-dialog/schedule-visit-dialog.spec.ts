import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { vi } from 'vitest';
import { ScheduleVisitDialog } from './schedule-visit-dialog';

describe('ScheduleVisitDialog', () => {
  let component: ScheduleVisitDialog;
  let fixture: ComponentFixture<ScheduleVisitDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduleVisitDialog],
      providers: [
        { provide: DynamicDialogRef, useValue: { close: vi.fn() } },
        { provide: DynamicDialogConfig, useValue: { data: {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ScheduleVisitDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
