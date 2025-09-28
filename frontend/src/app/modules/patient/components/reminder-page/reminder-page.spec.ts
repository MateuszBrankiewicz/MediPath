import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
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
        {
          provide: UserNotificationsService,
          useValue: {
            notifications,
            unreadCount: unread,
            markAsRead: jasmine
              .createSpy('markAsRead')
              .and.returnValue(of(void 0)),
            markAllAsRead: jasmine
              .createSpy('markAllAsRead')
              .and.returnValue(of(void 0)),
            refresh: jasmine
              .createSpy('refresh')
              .and.returnValue(of([] as UserNotification[])),
            lastError: signal<unknown>(null),
            isConnecting: signal(false),
            isConnected: signal(true),
          },
        },
        {
          provide: ToastService,
          useValue: {
            showError: jasmine.createSpy('showError'),
            showSuccess: jasmine.createSpy('showSuccess'),
          },
        },
        {
          provide: TranslationService,
          useValue: {
            translate: () => '',
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
