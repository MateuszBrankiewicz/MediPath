import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectInstitution } from './select-institution';

describe('SelectInstitution', () => {
  let component: SelectInstitution;
  let fixture: ComponentFixture<SelectInstitution>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectInstitution],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectInstitution);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
