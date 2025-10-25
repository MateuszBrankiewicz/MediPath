import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MessageService } from 'primeng/api';

import { InstitutionView } from './institution-view';

describe('InstitutionView', () => {
  let component: InstitutionView;
  let fixture: ComponentFixture<InstitutionView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionView],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
