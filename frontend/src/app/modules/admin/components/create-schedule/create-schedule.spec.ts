import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { CreateSchedule } from './create-schedule';

describe('CreateSchedule', () => {
  let component: CreateSchedule;
  let fixture: ComponentFixture<CreateSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateSchedule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
