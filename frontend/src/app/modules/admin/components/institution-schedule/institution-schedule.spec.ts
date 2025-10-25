import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { InstitutionSchedule } from './institution-schedule';

describe('InstitutionSchedule', () => {
  let component: InstitutionSchedule;
  let fixture: ComponentFixture<InstitutionSchedule>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionSchedule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionSchedule);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
