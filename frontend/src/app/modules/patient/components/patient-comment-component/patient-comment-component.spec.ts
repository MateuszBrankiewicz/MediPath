import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PatientCommentComponent } from './patient-comment-component';

describe('PatientCommentComponent', () => {
  let component: PatientCommentComponent;
  let fixture: ComponentFixture<PatientCommentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientCommentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PatientCommentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
