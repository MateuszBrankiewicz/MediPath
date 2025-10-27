import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { EditUserProfile } from './edit-user-profile';

describe('EditUserProfile', () => {
  let component: EditUserProfile;
  let fixture: ComponentFixture<EditUserProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditUserProfile],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditUserProfile);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
