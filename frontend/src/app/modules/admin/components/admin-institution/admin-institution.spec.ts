import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminInstitution } from './admin-institution';

describe('AdminInstitution', () => {
  let component: AdminInstitution;
  let fixture: ComponentFixture<AdminInstitution>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminInstitution]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminInstitution);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
