import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  SupportedLanguage,
  TranslationService,
} from '../../../../core/services/translation/translation.service';

interface LanguageOption {
  code: SupportedLanguage;
  name: string;
}

interface AccountSettingsForm {
  language: SupportedLanguage;
  darkMode: boolean;
  personalReminders: boolean;
  visitsReminders: boolean;
}

@Component({
  selector: 'app-account-settings',
  imports: [CommonModule, FormsModule],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountSettings {
  private translationService = inject(TranslationService);

  public readonly isLoading = signal(false);

  public settings = signal<AccountSettingsForm>({
    language: this.translationService.language(),
    darkMode: false,
    personalReminders: true,
    visitsReminders: true,
  });

  public readonly languageOptions: LanguageOption[] = [
    { code: 'en', name: 'English' },
    { code: 'pl', name: 'Polski' },
  ];

  translate(key: string, params?: Record<string, string | number>): string {
    return this.translationService.translate(key, params);
  }

  async onLanguageChange(selectedLanguage: LanguageOption) {
    if (
      selectedLanguage &&
      selectedLanguage.code !== this.settings().language
    ) {
      this.settings.update((settings) => ({
        ...settings,
        language: selectedLanguage.code,
      }));

      await this.translationService.setLanguage(selectedLanguage.code);
    }
  }

  onToggleChange(
    field: keyof Omit<AccountSettingsForm, 'language'>,
    value: boolean,
  ) {
    this.settings.update((settings) => ({
      ...settings,
      [field]: value,
    }));
  }

  onSetDefault() {
    this.settings.set({
      language: 'pl',
      darkMode: false,
      personalReminders: true,
      visitsReminders: true,
    });

    this.translationService.setLanguage('pl');
  }

  async onSaveChanges() {
    this.isLoading.set(true);

    try {
      // Here you would typically save the settings to a backend service
      // For now, we'll just simulate a save operation
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // The language is already saved via the translation service
      // Other settings would be saved to user preferences
      console.log('Settings saved:', this.settings());
    } catch (error) {
      console.error('Error saving settings:', error);
    } finally {
      this.isLoading.set(false);
    }
  }

  onDeactivateAccount() {
    // This would typically show a confirmation dialog and handle account deactivation
    console.log('Deactivate account requested');
  }
}
