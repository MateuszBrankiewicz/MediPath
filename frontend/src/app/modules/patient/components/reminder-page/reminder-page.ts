import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { TagModule } from 'primeng/tag';
import { NotificationMessage } from '../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../core/services/notifications/user-notifications.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AddReminderDialog } from './components/add-reminder-dialog/add-reminder-dialog';

@Component({
  selector: 'app-reminder-page',
  imports: [CommonModule, ButtonModule, TagModule, DatePipe, PaginatorModule],
  templateUrl: './reminder-page.html',
  styleUrl: './reminder-page.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReminderPage implements OnInit {
  private readonly notificationsService = inject(UserNotificationsService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly notifications = signal<NotificationMessage[]>([]);
  protected readonly translationService = inject(TranslationService);
  protected readonly first = signal(0);
  protected readonly rows = signal(10);
  protected readonly isRefreshing = signal(false);
  protected readonly isMarkingAll = signal(false);

  private dialogService = inject(DialogService);

  protected readonly hasNotifications = computed(
    () => this.notifications().length !== 0,
  );

  protected readonly notificationsCount = computed(() => {
    return this.notifications().length;
  });

  protected readonly unreadNotifications = computed(() => {
    return this.notifications().filter(
      (notifcation) => notifcation.read === false,
    ).length;
  });

  protected readonly todaysNotifications = computed(() => {
    return this.notifications().filter((notification) => {
      return this.isToday(notification.timestamp);
    }).length;
  });

  protected readonly notificationsPointer = computed(() => {
    const readCount = this.notifications().filter(
      (notification) => notification.read === true,
    ).length;
    return readCount / this.notificationsCount();
  });

  protected readonly paginatedNotifications = computed(() => {
    return this.notifications().slice(this.first(), this.first() + this.rows());
  });

  ngOnInit(): void {
    this.notificationsService.notifications$.subscribe((message) => {
      console.log(message);
    });
    this.initNotifcations();
  }

  protected onPageChange(event: PaginatorState) {
    this.first.set(event.first ?? 0);
    this.rows.set(event.rows ?? 10);
  }

  private isToday(dateString?: string): boolean {
    if (!dateString) return false;
    const today = new Date();
    const date = new Date(dateString);
    return (
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()
    );
  }

  private initNotifcations() {
    this.notificationsService
      .getAllNotifications()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        const notificationsArray: NotificationMessage[] = Array.isArray(res)
          ? (res as NotificationMessage[])
          : (Object.values(res ?? {}) as NotificationMessage[]);
        this.notifications.set(notificationsArray);
        console.log(this.notifications());
      });
  }

  protected openAddNotificationDialog() {
    const ref = this.dialogService.open(AddReminderDialog, {
      style: {
        width: '40%',
        overflow: 'hidden',
      },
      header: 'Add notification',
    });

    ref?.onClose.subscribe((res) => {
      console.log(res);
    });
  }
}
