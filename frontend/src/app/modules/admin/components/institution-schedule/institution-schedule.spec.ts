import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionSchedule } from './institution-schedule';

describe('InstitutionSchedule', () => {
  let component: InstitutionSchedule;
  let fixture: ComponentFixture<InstitutionSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionSchedule]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstitutionSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
