import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddReminderDialog } from './add-reminder-dialog';

describe('AddReminderDialog', () => {
  let component: AddReminderDialog;
  let fixture: ComponentFixture<AddReminderDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddReminderDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddReminderDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
