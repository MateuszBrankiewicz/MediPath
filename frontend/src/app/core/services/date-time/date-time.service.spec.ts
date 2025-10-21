import { TestBed } from '@angular/core/testing';
import { TranslationService } from '../translation/translation.service';
import { DateTimeService } from './date-time.service';

describe('DateTimeService', () => {
  let service: DateTimeService;
  let translationServiceSpy: jasmine.SpyObj<TranslationService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('TranslationService', ['translate']);

    TestBed.configureTestingModule({
      providers: [
        DateTimeService,
        { provide: TranslationService, useValue: spy },
      ],
    });

    service = TestBed.inject(DateTimeService);
    translationServiceSpy = TestBed.inject(
      TranslationService,
    ) as jasmine.SpyObj<TranslationService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get day name for Monday', () => {
    const monday = new Date('2025-10-20'); // Monday
    translationServiceSpy.translate.and.returnValue('Monday');

    const result = service.getDayName(monday);

    expect(translationServiceSpy.translate).toHaveBeenCalledWith(
      'weekdays.monday',
    );
    expect(result).toBe('Monday');
  });

  it('should get short day name', () => {
    const date = new Date('2025-10-20');
    translationServiceSpy.translate.and.returnValue('MON');

    const result = service.getShortDayName(date);

    expect(translationServiceSpy.translate).toHaveBeenCalledWith(
      'weekdays.short.monday',
    );
    expect(result).toBe('MON');
  });

  it('should get month name by index', () => {
    translationServiceSpy.translate.and.returnValue('October');

    const result = service.getMonthName(9); // October

    expect(translationServiceSpy.translate).toHaveBeenCalledWith(
      'months.october',
    );
    expect(result).toBe('October');
  });

  it('should throw error for invalid month index', () => {
    expect(() => service.getMonthName(-1)).toThrowError();
    expect(() => service.getMonthName(12)).toThrowError();
  });

  it('should format date to ISO string', () => {
    const date = new Date('2025-10-19');
    const result = service.formatDateToISO(date);
    expect(result).toBe('2025-10-19');
  });

  it('should format month and year', () => {
    const date = new Date('2025-10-19');
    translationServiceSpy.translate.and.returnValue('Październik');

    const result = service.formatMonthYear(date);

    expect(result).toBe('Październik 2025');
  });
});
