import { inject, Injectable } from '@angular/core';
import { TranslationService } from '../translation/translation.service';

@Injectable({
  providedIn: 'root',
})
export class DateTimeService {
  private readonly translationService = inject(TranslationService);

  private readonly weekdayKeys = [
    'weekdays.sunday',
    'weekdays.monday',
    'weekdays.tuesday',
    'weekdays.wednesday',
    'weekdays.thursday',
    'weekdays.friday',
    'weekdays.saturday',
  ];

  private readonly weekdayShortKeys = [
    'weekdays.short.sunday',
    'weekdays.short.monday',
    'weekdays.short.tuesday',
    'weekdays.short.wednesday',
    'weekdays.short.thursday',
    'weekdays.short.friday',
    'weekdays.short.saturday',
  ];

  private readonly monthKeys = [
    'months.january',
    'months.february',
    'months.march',
    'months.april',
    'months.may',
    'months.june',
    'months.july',
    'months.august',
    'months.september',
    'months.october',
    'months.november',
    'months.december',
  ];

  public getDayName(date: Date): string {
    return this.translationService.translate(this.weekdayKeys[date.getDay()]);
  }

  public getShortDayName(date: Date): string {
    return this.translationService.translate(
      this.weekdayShortKeys[date.getDay()],
    );
  }

  public getMonthName(monthIndex: number): string {
    if (monthIndex < 0 || monthIndex > 11) {
      throw new Error(`Invalid month index: ${monthIndex}. Must be 0-11.`);
    }
    return this.translationService.translate(this.monthKeys[monthIndex]);
  }

  public getMonthNameFromDate(date: Date): string {
    return this.getMonthName(date.getMonth());
  }

  public formatMonthYear(date: Date): string {
    const monthName = this.getMonthNameFromDate(date);
    return `${monthName} ${date.getFullYear()}`;
  }

  public formatDateToISO(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  public getWeekdayNames(): string[] {
    return [
      this.translationService.translate('weekdays.monday'),
      this.translationService.translate('weekdays.tuesday'),
      this.translationService.translate('weekdays.wednesday'),
      this.translationService.translate('weekdays.thursday'),
      this.translationService.translate('weekdays.friday'),
      this.translationService.translate('weekdays.saturday'),
      this.translationService.translate('weekdays.sunday'),
    ];
  }

  public getShortWeekdayNames(): string[] {
    return [
      this.translationService.translate('weekdays.short.monday'),
      this.translationService.translate('weekdays.short.tuesday'),
      this.translationService.translate('weekdays.short.wednesday'),
      this.translationService.translate('weekdays.short.thursday'),
      this.translationService.translate('weekdays.short.friday'),
      this.translationService.translate('weekdays.short.saturday'),
      this.translationService.translate('weekdays.short.sunday'),
    ];
  }

  public getMonthNames(): string[] {
    return this.monthKeys.map((key) => this.translationService.translate(key));
  }
}
