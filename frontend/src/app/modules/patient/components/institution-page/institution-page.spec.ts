import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { InstitutionPage } from './institution-page';

describe('InstitutionPage', () => {
  let component: InstitutionPage;
  let fixture: ComponentFixture<InstitutionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
