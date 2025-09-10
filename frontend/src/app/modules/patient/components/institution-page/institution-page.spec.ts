import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionPage } from './institution-page';

describe('InstitutionPage', () => {
  let component: InstitutionPage;
  let fixture: ComponentFixture<InstitutionPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionPage],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
