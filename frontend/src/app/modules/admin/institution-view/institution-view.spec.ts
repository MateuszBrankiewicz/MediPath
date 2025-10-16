import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionView } from './institution-view';

describe('InstitutionView', () => {
  let component: InstitutionView;
  let fixture: ComponentFixture<InstitutionView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstitutionView);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
