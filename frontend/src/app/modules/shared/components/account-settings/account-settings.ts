import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { SelectModule } from 'primeng/select';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { firstValueFrom } from 'rxjs';
import {
  AccountSettingsForm,
  DEFAULT_FORM,
  LanguageOption,
} from '../../../../core/models/account-settings.model';
import { UserSettings } from '../../../../core/services/authentication/user-settings.model';
import { UserSettingsService } from '../../../../core/services/authentication/user-settings.service';
import { ToastService } from '../../../../core/services/toast/toast.service';
import {
  DEFAULT_LANGUAGE,
  SupportedLanguage,
} from '../../../../core/services/translation/language.model';
import { TranslationService } from '../../../../core/services/translation/translation.service';
import { AuthenticationService } from '../../../../core/services/authentication/authentication';

@Component({
  selector: 'app-account-settings',
  imports: [CommonModule, FormsModule, SelectModule, ToggleSwitchModule],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSettings implements OnInit {
  private readonly translationService = inject(TranslationService);
  private readonly userSettingsService = inject(UserSettingsService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly authenticationService = inject(AuthenticationService);
  public readonly isLoading = signal(false);
  public readonly settings = signal<AccountSettingsForm>(DEFAULT_FORM);

  public readonly languageOptions: LanguageOption[] =
    this.translationService.getAvailableLanguages();

  ngOnInit(): void {
    this.userSettingsService
      .ensureSettingsLoaded()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((settings) => {
        this.updateStateFromSettings(settings);
      });
  }

  translate(key: string, params?: Record<string, string | number>): string {
    return this.translationService.translate(key, params);
  }

  async onLanguageChange(code: SupportedLanguage) {
    const current = this.settings();
    if (code === current.language) {
      return;
    }

    const nextSettings: AccountSettingsForm = {
      ...current,
      language: code,
    };

    this.settings.set(nextSettings);
    this.userSettingsService.setLocalSettings({ ...nextSettings });

    await this.translationService.setLanguage(code);
  }

  onToggleChange(
    field: keyof Omit<AccountSettingsForm, 'language'>,
    value: boolean,
  ) {
    const nextSettings: AccountSettingsForm = {
      ...this.settings(),
      [field]: value,
    } as AccountSettingsForm;

    this.settings.set(nextSettings);
    this.userSettingsService.setLocalSettings({ ...nextSettings });
  }

  onSetDefault() {
    this.settings.set({ ...DEFAULT_FORM });
    this.userSettingsService.setLocalSettings({ ...DEFAULT_FORM });
    void this.translationService.setLanguage(DEFAULT_LANGUAGE);
  }

  async onSaveChanges() {
    this.isLoading.set(true);

    try {
      const updated = await firstValueFrom(
        this.userSettingsService
          .updateSettings({ ...this.settings() })
          .pipe(takeUntilDestroyed(this.destroyRef)),
      );

      this.updateStateFromSettings(updated);
      await this.translationService.setLanguage(updated.language);
      this.toastService.showSuccess('accountSettings.saveSuccess');
    } catch (error) {
      console.error('Failed to save account settings', error);
      this.toastService.showError('accountSettings.saveError');
    } finally {
      this.isLoading.set(false);
    }
  }

  onDeactivateAccount() {
    this.userSettingsService
      .deactivateAccount()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.authenticationService.logout();
      });
  }

  private updateStateFromSettings(settings: UserSettings) {
    const next: AccountSettingsForm = {
      language: settings.language,
      systemNotifications: settings.systemNotifications,
      userNotifications: settings.userNotifications,
    };

    this.settings.set(next);
    this.userSettingsService.setLocalSettings({ ...next });
  }
}
