import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Comment } from '../../../../core/models/doctor.model';
import { PatientCommentComponent } from './patient-comment-component';

describe('PatientCommentComponent', () => {
  let component: PatientCommentComponent;
  let fixture: ComponentFixture<PatientCommentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientCommentComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(PatientCommentComponent);
    component = fixture.componentInstance;

    // Mock required input for comment
    const mockComment: Comment = {
      id: '1',
      userName: 'John Doe',
      visitedInstitution: 'Test Hospital',
      content: 'Great doctor!',
      dateOfVisit: new Date(),
      numberOfStars: 5,
    };
    fixture.componentRef.setInput('comment', mockComment);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
