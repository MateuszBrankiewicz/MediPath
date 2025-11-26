import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { UserNotification } from '../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../core/services/notifications/user-notifications.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { ReminderPage } from './reminder-page';

describe('ReminderPage', () => {
  let component: ReminderPage;
  let fixture: ComponentFixture<ReminderPage>;

  beforeEach(async () => {
    const notifications = signal<UserNotification[]>([]);
    const unread = signal(0);

    await TestBed.configureTestingModule({
      imports: [ReminderPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: UserNotificationsService,
          useValue: {
            notifications,
            unreadCount: unread,
            markAsRead: vi.fn().mockReturnValue(of(void 0)),
            markAllAsRead: vi.fn().mockReturnValue(of(void 0)),
            refresh: vi.fn().mockReturnValue(of([] as UserNotification[])),
            lastError: signal<unknown>(null),
            isConnecting: signal(false),
            isConnected: signal(true),
            notifications$: of([]),
            getAllNotifications: vi.fn(),
            notificationsArray$: of([]),
            getNotifications: vi.fn().mockReturnValue(of([])),
          },
        },
        {
          provide: ToastService,
          useValue: {
            showError: vi.fn(),
            showSuccess: vi.fn(),
          },
        },
        {
          provide: TranslationService,
          useValue: {
            translate: () => '',
            language: vi.fn().mockReturnValue('en'),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ReminderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
