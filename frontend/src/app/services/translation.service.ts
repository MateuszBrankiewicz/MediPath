import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export type SupportedLanguage = 'en' | 'pl';

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private currentLanguage = signal<SupportedLanguage>('pl');
  private translations = signal<Record<string, string>>({});

  constructor(private http: HttpClient) {
    const currentLanguageLs : SupportedLanguage = localStorage.getItem("LANGUAGE_KEY") as SupportedLanguage;
    if(currentLanguageLs){
        this.currentLanguage.set(currentLanguageLs)
    }else{
        localStorage.setItem("LANGUAGE_KEY", this.currentLanguage())
    }
    this.loadTranslations(this.currentLanguage());
  }

  get language() {
    return this.currentLanguage.asReadonly();
  }

  async setLanguage(lang: SupportedLanguage) {
    this.currentLanguage.set(lang);
    localStorage.setItem("LANGUAGE_KEY", this.currentLanguage())

    await this.loadTranslations(lang);
  }

  private async loadTranslations(lang: SupportedLanguage) {
    try {
      const translations = await firstValueFrom(
        this.http.get<Record<string, string>>(`/assets/locale/messages.${lang}.json`)
      );
      this.translations.set(translations);
    } catch (error) {
      console.error('Failed to load translations:', error);
      this.translations.set({});
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
      { code: 'pl' as const, name: 'Polski' }
    ];
  }
}
