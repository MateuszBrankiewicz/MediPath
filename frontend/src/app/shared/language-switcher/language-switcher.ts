import { Component, inject, computed } from '@angular/core';
import { TranslationService } from '../../services/translation.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-language-switcher',
  imports: [ButtonModule],
  templateUrl: './language-switcher.html',
  styleUrl: './language-switcher.scss'
})
export class LanguageSwitcher {
  private translationService = inject(TranslationService);

  currentLanguage = this.translationService.language;
  
  buttonLabel = computed(() => {
    return this.currentLanguage() === 'pl' ? 'EN' : 'PL';
  });

  async toggleLanguage() {
    const newLang = this.currentLanguage() === 'pl' ? 'en' : 'pl';
    await this.translationService.setLanguage(newLang);
  }
}
