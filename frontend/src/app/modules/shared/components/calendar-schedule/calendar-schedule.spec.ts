import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarSchedule } from './calendar-schedule';

describe('CalendarSchedule', () => {
  let component: CalendarSchedule;
  let fixture: ComponentFixture<CalendarSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarSchedule],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
