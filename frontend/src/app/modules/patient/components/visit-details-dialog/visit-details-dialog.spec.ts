import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { vi } from 'vitest';
import { VisitDetailsDialog } from './visit-details-dialog';

describe('VisitDetailsDialog', () => {
  let component: VisitDetailsDialog;
  let fixture: ComponentFixture<VisitDetailsDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisitDetailsDialog],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: DynamicDialogRef, useValue: { close: vi.fn() } },
        { provide: DynamicDialogConfig, useValue: { data: {} } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VisitDetailsDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
