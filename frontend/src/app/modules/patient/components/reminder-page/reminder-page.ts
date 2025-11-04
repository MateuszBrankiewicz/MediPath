/* eslint-disable @typescript-eslint/no-explicit-any */
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
import {
  FilterFieldConfig,
  FilteringService,
} from '../../../../core/services/filtering/filtering.service';
import { NotificationMessage } from '../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../core/services/notifications/user-notifications.service';
import {
  SortFieldConfig,
  SortingService,
} from '../../../../core/services/sorting/sorting.service';
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
  private readonly sortingService = inject(SortingService);
  private readonly filteringService = inject(FilteringService);
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

  private readonly notificationFilterConfig: FilterFieldConfig<NotificationMessage> =
    this.filteringService.combineConfigs(
      this.filteringService.searchConfig<NotificationMessage>(
        (n) => n.title || '',
        (n) => n.content || '',
      ),
      this.filteringService.dateRangeConfig<NotificationMessage>(
        (n) => n.timestamp,
      ),
    );

  private readonly notificationSortConfig: SortFieldConfig<NotificationMessage>[] =
    [
      this.sortingService.dateField('date', (n) => n.timestamp),
      this.sortingService.stringField('title', (n) => n.title || n.content),
      this.sortingService.booleanField('read', (n) => n.read),
    ];

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

  private readonly receivedNotifications = computed(() => this.notifications());
  private readonly baseList = computed(() => this.receivedNotifications());

  private readonly filteredAndSorted = computed(() => {
    const list = this.baseList();
    const { searchTerm, dateFrom, dateTo, sortField, sortOrder } =
      this.filters();

    const filtered = this.filteringService.filter(
      list,
      { searchTerm, dateFrom, dateTo },
      this.notificationFilterConfig,
    );

    return this.sortingService.sort(
      filtered,
      sortField,
      sortOrder,
      this.notificationSortConfig,
    );
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
    this.getNotifications('received');
  }

  protected onFiltersChange(params: FilterParams) {
    this.filters.set(params);
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
      modal: true,
      closable: true,
      header: 'Add notification',
    });

    ref?.onClose.subscribe((res) => {
      if (res) {
        this.isMarkingAll.set(true);
        this.notificationsService
          .addNotification(res)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.isMarkingAll.set(false);
              this.toastService.showSuccess(
                this.translationService.translate(
                  'notifications.toast.addSuccess',
                ),
              );
              this.initNotifcations();
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
          this.notifications.set(
            this.notifications().map((notif) => {
              if (
                notif.title === notification.title &&
                notif.timestamp === notification.timestamp
              ) {
                notif.read = true;
              }
              return notif;
            }),
          );
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
      closable: true,
      modal: true,
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
            next: () => {
              this.isMarkingAll.set(false);
              this.toastService.showSuccess(
                this.translationService.translate(
                  'notifications.toast.addSuccess',
                ),
              );
              this.initNotifcations();
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

  private getNotifications(param: string) {
    this.isRefreshing.set(true);
    this.notificationsService
      .getNotifications(param)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((res: any) => {
        const notificationsArray: NotificationMessage[] = Array.isArray(res)
          ? res
          : Array.isArray(res?.notifications)
            ? res.notifications
            : [];
        this.notifications.set(notificationsArray);
        this.isRefreshing.set(false);
      });
  }

  protected changePlanned() {
    this.showPlanned.set(!this.showPlanned());
    if (this.showPlanned()) {
      this.getNotifications('upcoming');
    } else {
      this.getNotifications('received');
    }
  }
}
