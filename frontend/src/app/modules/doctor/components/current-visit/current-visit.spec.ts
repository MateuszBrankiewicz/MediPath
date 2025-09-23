import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CurrentVisit } from './current-visit';

describe('CurrentVisit', () => {
  let component: CurrentVisit;
  let fixture: ComponentFixture<CurrentVisit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CurrentVisit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CurrentVisit);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
