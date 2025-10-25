import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';

import { AccountSettings } from './account-settings';

describe('AccountSettings', () => {
  let component: AccountSettings;
  let fixture: ComponentFixture<AccountSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountSettings],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
