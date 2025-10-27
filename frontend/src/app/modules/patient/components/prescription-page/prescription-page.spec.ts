import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { PrescriptionPage } from './prescription-page';

describe('PrescriptionPage', () => {
  let component: PrescriptionPage;
  let fixture: ComponentFixture<PrescriptionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrescriptionPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PrescriptionPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
