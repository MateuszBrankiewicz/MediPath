import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduleVisitDialog } from './schedule-visit-dialog';

describe('ScheduleVisitDialog', () => {
  let component: ScheduleVisitDialog;
  let fixture: ComponentFixture<ScheduleVisitDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduleVisitDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScheduleVisitDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
