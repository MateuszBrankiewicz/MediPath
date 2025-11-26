import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { DialogService } from 'primeng/dynamicdialog';
import { VisitPage } from './visit-page';

describe('VisitPage', () => {
  let component: VisitPage;
  let fixture: ComponentFixture<VisitPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VisitPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
        DialogService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(VisitPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
