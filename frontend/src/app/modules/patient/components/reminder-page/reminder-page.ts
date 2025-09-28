import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { finalize } from 'rxjs';
import { UserNotification } from '../../../../core/services/notifications/user-notifications.model';
import { UserNotificationsService } from '../../../../core/services/notifications/user-notifications.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import { TranslationService } from '../../../../core/services/translation/translation.service';

@Component({
  selector: 'app-reminder-page',
  imports: [CommonModule, ButtonModule, TagModule, DatePipe],
  templateUrl: './reminder-page.html',
  styleUrl: './reminder-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReminderPage {
  private readonly notificationsService = inject(UserNotificationsService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly translationService = inject(TranslationService);

  protected readonly notifications = this.notificationsService.notifications;
  protected readonly unreadCount = this.notificationsService.unreadCount;

  protected readonly totalCount = computed(() => this.notifications().length);

  protected readonly todayCount = computed(() => {
    return this.notifications().filter((notification) =>
      this.isToday(notification.createdAt),
    ).length;
  });

  protected readonly readRate = computed(() => {
    const total = this.totalCount();
    if (total === 0) {
      return 0;
    }

    const read = total - this.unreadCount();
    return Math.round((read / total) * 100);
  });

  protected readonly hasNotifications = computed(() => this.totalCount() > 0);

  protected readonly isRefreshing = signal(false);
  protected readonly isMarkingAll = signal(false);

  protected onRefresh(): void {
    if (this.isRefreshing()) {
      return;
    }

    this.isRefreshing.set(true);

    this.notificationsService
      .refresh()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isRefreshing.set(false)),
      )
      .subscribe({
        error: (error: unknown) => {
          console.error('Failed to refresh notifications', error);
          this.toastService.showError('notifications.refreshError');
        },
      });
  }

  protected onMarkAsRead(notification: UserNotification): void {
    if (notification.read) {
      return;
    }

    this.notificationsService
      .markAsRead(notification.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        error: (error: unknown) => {
          console.error('Failed to mark notification as read', error);
          this.toastService.showError('notifications.markSingleError');
        },
      });
  }

  protected onMarkAllAsRead(): void {
    if (this.isMarkingAll() || this.unreadCount() === 0) {
      return;
    }

    this.isMarkingAll.set(true);

    this.notificationsService
      .markAllAsRead()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isMarkingAll.set(false)),
      )
      .subscribe({
        next: () => {
          this.toastService.showSuccess('notifications.markAllSuccess');
        },
        error: (error: unknown) => {
          console.error('Failed to mark all notifications as read', error);
          this.toastService.showError('notifications.markAllError');
        },
      });
  }

  protected translate(
    key: string,
    params?: Record<string, string | number>,
  ): string {
    return this.translationService.translate(key, params);
  }

  private isToday(date: Date): boolean {
    const today = new Date();
    return (
      date.getFullYear() === today.getFullYear() &&
      date.getMonth() === today.getMonth() &&
      date.getDate() === today.getDate()
    );
  }
}
