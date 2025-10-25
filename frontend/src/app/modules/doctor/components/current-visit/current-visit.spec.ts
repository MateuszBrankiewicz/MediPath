import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CurrentVisit } from './current-visit';

describe('CurrentVisit', () => {
  let component: CurrentVisit;
  let fixture: ComponentFixture<CurrentVisit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrentVisit],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CurrentVisit);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
