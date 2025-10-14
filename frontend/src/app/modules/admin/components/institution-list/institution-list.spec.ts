import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstitutionList } from './institution-list';

describe('InstitutionList', () => {
  let component: InstitutionList;
  let fixture: ComponentFixture<InstitutionList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstitutionList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
