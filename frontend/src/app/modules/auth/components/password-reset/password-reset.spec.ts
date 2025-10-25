import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { PasswordReset } from './password-reset';

describe('PasswordReset', () => {
  let component: PasswordReset;
  let fixture: ComponentFixture<PasswordReset>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordReset],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PasswordReset);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
