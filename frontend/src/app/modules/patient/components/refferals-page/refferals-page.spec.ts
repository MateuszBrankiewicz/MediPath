import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { RefferalsPage } from './refferals-page';

describe('RefferalsPage', () => {
  let component: RefferalsPage;
  let fixture: ComponentFixture<RefferalsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RefferalsPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RefferalsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
