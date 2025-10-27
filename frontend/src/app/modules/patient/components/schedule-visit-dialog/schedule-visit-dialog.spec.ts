import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { vi } from 'vitest';
import { ScheduleVisitDialog } from './schedule-visit-dialog';

describe('ScheduleVisitDialog', () => {
  let component: ScheduleVisitDialog;
  let fixture: ComponentFixture<ScheduleVisitDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduleVisitDialog],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: DynamicDialogRef, useValue: { close: vi.fn() } },
        {
          provide: DynamicDialogConfig,
          useValue: {
            data: {
              event: {
                doctor: { name: 'Dr. Smith', id: 'doc-1' },
                institution: {
                  institution: { institutionName: 'Test Hospital' },
                },
                patientRemarks: '',
                day: null,
                time: null,
                slotId: null,
              },
            },
          },
        },
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
