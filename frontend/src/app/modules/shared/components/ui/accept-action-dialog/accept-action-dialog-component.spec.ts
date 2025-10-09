import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcceptActionDialogComponent } from './accept-action-dialog-component';

describe('AcceptActionDialogComponent', () => {
  let component: AcceptActionDialogComponent;
  let fixture: ComponentFixture<AcceptActionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AcceptActionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AcceptActionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
