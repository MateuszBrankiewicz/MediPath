import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { vi } from 'vitest';

import { AcceptActionDialogComponent } from './accept-action-dialog-component';

describe('AcceptActionDialogComponent', () => {
  let component: AcceptActionDialogComponent;
  let fixture: ComponentFixture<AcceptActionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceptActionDialogComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: DynamicDialogRef,
          useValue: { close: vi.fn() },
        },
        {
          provide: DynamicDialogConfig,
          useValue: { data: {} },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AcceptActionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
