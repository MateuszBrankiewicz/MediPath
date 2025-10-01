import { CommonModule, DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
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
export class ReminderPage implements OnInit {
  hasNotifications = signal(false);
  private readonly notificationsService = inject(UserNotificationsService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly translationService = inject(TranslationService);

  protected readonly isRefreshing = signal(false);
  protected readonly isMarkingAll = signal(false);
  notifications: any;

  protected translate(
    key: string,
    params?: Record<string, string | number>,
  ): string {
    return this.translationService.translate(key, params);
  }

  ngOnInit(): void {
    this.notificationsService.notifications$.subscribe((message) => {
      console.log(message);
    });
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
