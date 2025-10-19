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
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogService } from 'primeng/dynamicdialog';
import { PaginatorModule } from 'primeng/paginator';
import { ProgressSpinner } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { FilterParams } from '../../../../core/models/filter.model';
import { NotificationMessage } from '../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../core/services/notifications/user-notifications.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { PaginatedComponentBase } from '../../../shared/components/base/paginated-component.base';
import { FilterComponent } from '../../../shared/components/ui/filter-component/filter-component';
import { AddReminderDialog } from './components/add-reminder-dialog/add-reminder-dialog';

@Component({
  selector: 'app-reminder-page',
  imports: [
    CommonModule,
    ButtonModule,
    TagModule,
    DatePipe,
    PaginatorModule,
    ProgressSpinner,
    ToggleButtonModule,
    FormsModule,
    FilterComponent,
  ],
  templateUrl: './reminder-page.html',
  styleUrl: './reminder-page.scss',
  providers: [DialogService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReminderPage
  extends PaginatedComponentBase<NotificationMessage>
  implements OnInit
{
  private readonly notificationsService = inject(UserNotificationsService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly notifications = signal<NotificationMessage[]>([]);
  protected readonly translationService = inject(TranslationService);
  protected readonly isRefreshing = signal(false);
  protected readonly isMarkingAll = signal(false);
  protected readonly filtersDefaults: FilterParams = {
    searchTerm: '',
    status: 'all',
    dateFrom: null,
    dateTo: null,
    sortField: 'date',
    sortOrder: 'desc',
  };
  protected readonly filters = signal<FilterParams>({
    ...this.filtersDefaults,
  });

  private dialogService = inject(DialogService);

  protected readonly hasNotifications = computed(
    () => this.notifications().length !== 0,
  );

  protected readonly notificationsCount = computed(() => {
    return this.receivedNotifications().length;
  });

  protected readonly unreadNotifications = computed(() => {
    return this.receivedNotifications().filter(
      (notifcation) => notifcation.read === false,
    ).length;
  });

  protected readonly todaysNotifications = computed(() => {
    return this.receivedNotifications().filter((notification) => {
      return this.isToday(notification.timestamp);
    }).length;
  });

  private readonly upcomingNotifications = computed(() => {
    return this.receivedNotifications().filter((notification) => {
      if (!notification.timestamp) return false;
      return new Date(notification.timestamp) > new Date();
    });
  });

  private readonly receivedNotifications = computed(() => {
    return this.notifications().filter((notification) => {
      if (!notification.timestamp) return false;
      return new Date(notification.timestamp) <= new Date();
    });
  });

  private readonly baseList = computed(() =>
    this.showPlanned()
      ? this.upcomingNotifications()
      : this.receivedNotifications(),
  );

  private readonly filteredAndSorted = computed(() => {
    const list = this.baseList();
    const { searchTerm, dateFrom, dateTo, sortField, sortOrder } =
      this.filters();

    const term = (searchTerm ?? '').trim().toLowerCase();
    const from = dateFrom ? new Date(dateFrom) : null;
    const to = dateTo ? new Date(dateTo) : null;
    if (from) {
      from.setHours(0, 0, 0, 0);
    }
    if (to) {
      to.setHours(23, 59, 59, 999);
    }

    let filtered = list.filter((n) => {
      if (from || to) {
        if (!n.timestamp) return false;
        const ts = new Date(n.timestamp);
        if (from && ts < from) return false;
        if (to && ts > to) return false;
      }
      if (term.length > 0) {
        const title = (n.title ?? '').toLowerCase();
        const content = (n.content ?? '').toLowerCase();
        if (!title.includes(term) && !content.includes(term)) return false;
      }
      return true;
    });

    const dir = sortOrder === 'asc' ? 1 : -1;
    filtered = [...filtered].sort((a, b) => {
      if (sortField === 'title') {
        const at = (a.title || a.content || '').toLowerCase();
        const bt = (b.title || b.content || '').toLowerCase();
        if (at < bt) return -1 * dir;
        if (at > bt) return 1 * dir;
        return 0;
      }
      if (sortField === 'read') {
        const av = a.read ? 1 : 0;
        const bv = b.read ? 1 : 0;
        if (av < bv) return -1 * dir;
        if (av > bv) return 1 * dir;
        return 0;
      }
      // default: date
      const ad = a.timestamp ? new Date(a.timestamp).getTime() : 0;
      const bd = b.timestamp ? new Date(b.timestamp).getTime() : 0;
      if (ad < bd) return -1 * dir;
      if (ad > bd) return 1 * dir;
      return 0;
    });

    return filtered;
  });

  protected override get sourceData() {
    return this.filteredAndSorted();
  }

  protected getCountToPaginate() {
    return this.filteredAndSorted().length;
  }

  protected showPlanned = signal(false);

  protected readonly paginatedNotifications = computed(() => {
    return this.paginatedData();
  });

  ngOnInit(): void {
    this.notificationsService.notifications$.subscribe((message) => {
      console.log(message);
    });
    this.initNotifcations();
  }

  protected onFiltersChange(params: FilterParams) {
    this.filters.set(params);
    // reset to first page whenever filters change
    this.first.set(0);
  }

  protected readonly sortByOptions = computed(() => [
    {
      label: this.translationService.translate('shared.filters.date'),
      value: 'date',
    },
    {
      label: this.translationService.translate(
        'patient.addReminder.titleShort',
      ),
      value: 'title',
    },
    {
      label: this.translationService.translate('notifications.read'),
      value: 'read',
    },
  ]);

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
    this.isRefreshing.set(true);
    this.notificationsService.getAllNotifications();
    this.notificationsService.notificationsArray$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res) => {
        const notificationsArray: NotificationMessage[] = Array.isArray(res)
          ? (res as NotificationMessage[])
          : (Object.values(res ?? {}) as NotificationMessage[]);
        this.notifications.set(notificationsArray);
        this.isRefreshing.set(false);
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
      if (res) {
        this.isMarkingAll.set(true);
        this.notificationsService
          .addNotification(res)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (response) => {
              this.isMarkingAll.set(false);
              this.toastService.showSuccess(
                this.translationService.translate(
                  'notifications.toast.addSuccess',
                ),
              );
              this.initNotifcations();
              console.log(response);
            },
            error: (err) => {
              this.isMarkingAll.set(false);
              this.toastService.showError(
                this.translationService.translate(
                  'notifications.toast.addError',
                ),
              );
              console.error(err);
            },
          });
      }
    });
  }

  protected markAllAsRead() {
    this.isMarkingAll.set(true);
    this.notificationsService
      .markAllAsRead()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate(
              'notifications.toast.markAllSuccess',
            ),
          );
          this.initNotifcations();
          this.isMarkingAll.set(false);
        },
        error: (err) => {
          console.error(err);
          this.toastService.showError(
            this.translationService.translate(
              'notifications.toast.markAllError',
            ),
          );
          this.isMarkingAll.set(false);
        },
      });
  }

  protected markAsRead(notification: NotificationMessage) {
    this.isMarkingAll.set(true);
    this.notificationsService
      .markAsRead(notification)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate(
              'notifications.toast.markOneSuccess',
            ),
          );
          this.initNotifcations();
          this.isMarkingAll.set(false);
        },
        error: (err) => {
          console.error(err);
          this.toastService.showError(
            this.translationService.translate(
              'notifications.toast.markOneError',
            ),
          );
          this.isMarkingAll.set(false);
        },
      });
  }

  protected deleteNotification(notification: NotificationMessage) {
    this.isMarkingAll.set(true);
    this.notificationsService
      .deleteNotification(notification)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.showSuccess(
            this.translationService.translate(
              'notifications.toast.deleteSuccess',
            ),
          );
          this.initNotifcations();
          this.isMarkingAll.set(false);
        },
        error: (err) => {
          console.error(err);
          this.toastService.showError(
            this.translationService.translate(
              'notifications.toast.deleteError',
            ),
          );
          this.isMarkingAll.set(false);
        },
      });
  }

  protected editReminder(notification: NotificationMessage) {
    const ref = this.dialogService.open(AddReminderDialog, {
      style: {
        width: '40%',
        overflow: 'hidden',
      },
      header: 'Edit notification',
      data: {
        notification: notification,
      },
    });

    ref?.onClose.subscribe((res) => {
      if (res) {
        this.isMarkingAll.set(true);
        this.notificationsService
          .addNotification(res)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (response) => {
              this.isMarkingAll.set(false);
              this.toastService.showSuccess(
                this.translationService.translate(
                  'notifications.toast.addSuccess',
                ),
              );
              this.initNotifcations();
              console.log(response);
            },
            error: () => {
              this.isMarkingAll.set(false);
              this.toastService.showError(
                this.translationService.translate(
                  'notifications.toast.addError',
                ),
              );
            },
          });
      }
    });
  }
}
