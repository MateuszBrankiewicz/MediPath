import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectInstitution } from './select-institution';

describe('SelectInstitution', () => {
  let component: SelectInstitution;
  let fixture: ComponentFixture<SelectInstitution>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectInstitution]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectInstitution);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
