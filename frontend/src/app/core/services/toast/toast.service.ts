import { Injectable, inject } from '@angular/core';
import { MessageService } from 'primeng/api';
import { TranslationService } from '../translation/translation.service';

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private messageService = inject(MessageService);
  private translationService = inject(TranslationService);

  showSuccess(messageKey: string, titleKey?: string): void {
    this.messageService.add({
      severity: 'success',
      summary: this.translationService.translate(
        titleKey || 'toast.success.title',
      ),
      detail: this.translationService.translate(messageKey),
      life: 4000,
    });
  }

  showError(messageKey: string, titleKey?: string): void {
    this.messageService.add({
      severity: 'error',
      summary: this.translationService.translate(
        titleKey || 'toast.error.title',
      ),
      detail: this.translationService.translate(messageKey),
      life: 5000,
    });
  }

  showInfo(messageKey: string, titleKey?: string): void {
    this.messageService.add({
      severity: 'info',
      summary: this.translationService.translate(
        titleKey || 'toast.success.title',
      ),
      detail: this.translationService.translate(messageKey),
      life: 4000,
    });
  }

  showWarn(messageKey: string, titleKey?: string): void {
    this.messageService.add({
      severity: 'warn',
      summary: this.translationService.translate(
        titleKey || 'toast.error.title',
      ),
      detail: this.translationService.translate(messageKey),
      life: 4000,
    });
  }

  showSuccessMessage(message: string, title?: string): void {
    this.messageService.add({
      severity: 'success',
      summary:
        title || this.translationService.translate('toast.success.title'),
      detail: message,
      life: 4000,
    });
  }

  showErrorMessage(message: string, title?: string): void {
    this.messageService.add({
      severity: 'error',
      summary: title || this.translationService.translate('toast.error.title'),
      detail: message,
      life: 5000,
    });
  }
}
