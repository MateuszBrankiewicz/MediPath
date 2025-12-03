import { HttpClient } from '@angular/common/http';
import { effect, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AuthenticationService } from '../authentication/authentication';
import { UserSettingsService } from '../authentication/user-settings.service';
import { DEFAULT_LANGUAGE, SupportedLanguage } from './language.model';

@Injectable({
  providedIn: 'root',
})
export class TranslationService {
  private currentLanguage = signal<SupportedLanguage>(DEFAULT_LANGUAGE);
  private translations = signal<Record<string, string>>({});
  public readonly isLoading = signal(false);
  private http = inject(HttpClient);
  private userSettingsService = inject(UserSettingsService);
  private authService = inject(AuthenticationService);

  constructor() {
    this.applyLanguage(DEFAULT_LANGUAGE, true);

    effect(() => {
      if (this.authService.isAuthenticated()) {
        this.userSettingsService.ensureSettingsLoaded().subscribe();
      }
    });

    effect(() => {
      const language = this.userSettingsService.language();
      this.applyLanguage(language);
    });
  }

  get language() {
    return this.currentLanguage.asReadonly();
  }

  async setLanguage(lang: SupportedLanguage) {
    const currentSettings = this.userSettingsService.settings();
    if (currentSettings) {
      const newSettings = {
        ...currentSettings,
        language: lang,
      };
      this.userSettingsService.setLocalSettings(newSettings);
      this.userSettingsService.updateSettings(newSettings).subscribe();
    }
    await this.setLanguageAndLoad(lang, true);
  }

  private async loadTranslations(lang: SupportedLanguage) {
    this.isLoading.set(true);
    try {
      const translations = await firstValueFrom(
        this.http.get<Record<string, string>>(
          `/assets/locale/messages.${lang}.json`,
        ),
      );
      this.translations.set(translations);
    } catch (error) {
      console.error('Failed to load translations:', error);
      this.translations.set({});
    } finally {
      this.isLoading.set(false);
    }
  }

  translate(key: string, params?: Record<string, string | number>): string {
    const translations = this.translations();
    let text = translations[key] || key;

    if (params) {
      Object.entries(params).forEach(([param, value]) => {
        text = text.replace(`{${param}}`, value.toString());
      });
    }

    return text;
  }

  getAvailableLanguages() {
    return [
      { code: 'en' as const, name: 'English' },
      { code: 'pl' as const, name: 'Polski' },
    ];
  }

  private async setLanguageAndLoad(language: SupportedLanguage, force = false) {
    console.log('Setting language to', language);
    if (
      !force &&
      language === this.currentLanguage() &&
      Object.keys(this.translations()).length > 0
    ) {
      return;
    }

    this.currentLanguage.set(language);
    await this.loadTranslations(language);
  }

  private applyLanguage(language: SupportedLanguage, force = false) {
    console.log('Applying language', language);
    void this.setLanguageAndLoad(language, force);
  }
}
