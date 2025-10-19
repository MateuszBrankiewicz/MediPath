import { TestBed } from '@angular/core/testing';
import { SortFieldConfig, SortingService } from './sorting.service';

interface TestItem {
  name: string;
  age: number;
  date: Date;
  active: boolean;
}

describe('SortingService', () => {
  let service: SortingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SortingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('sort', () => {
    const testData: TestItem[] = [
      { name: 'Charlie', age: 30, date: new Date('2023-01-01'), active: true },
      { name: 'Alice', age: 25, date: new Date('2023-03-01'), active: false },
      { name: 'Bob', age: 35, date: new Date('2023-02-01'), active: true },
    ];

    it('should sort by string field ascending', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.stringField('name', (item) => item.name),
      ];

      const sorted = service.sort(testData, 'name', 'asc', config);

      expect(sorted[0].name).toBe('Alice');
      expect(sorted[1].name).toBe('Bob');
      expect(sorted[2].name).toBe('Charlie');
    });

    it('should sort by string field descending', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.stringField('name', (item) => item.name),
      ];

      const sorted = service.sort(testData, 'name', 'desc', config);

      expect(sorted[0].name).toBe('Charlie');
      expect(sorted[1].name).toBe('Bob');
      expect(sorted[2].name).toBe('Alice');
    });

    it('should sort by number field ascending', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.numberField('age', (item) => item.age),
      ];

      const sorted = service.sort(testData, 'age', 'asc', config);

      expect(sorted[0].age).toBe(25);
      expect(sorted[1].age).toBe(30);
      expect(sorted[2].age).toBe(35);
    });

    it('should sort by date field ascending', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.dateField('date', (item) => item.date),
      ];

      const sorted = service.sort(testData, 'date', 'asc', config);

      expect(sorted[0].date.getTime()).toBe(new Date('2023-01-01').getTime());
      expect(sorted[1].date.getTime()).toBe(new Date('2023-02-01').getTime());
      expect(sorted[2].date.getTime()).toBe(new Date('2023-03-01').getTime());
    });

    it('should sort by boolean field', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.booleanField('active', (item) => item.active),
      ];

      const sorted = service.sort(testData, 'active', 'asc', config);

      expect(sorted[0].active).toBe(false);
      expect(sorted[1].active).toBe(true);
      expect(sorted[2].active).toBe(true);
    });

    it('should handle empty array', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.stringField('name', (item) => item.name),
      ];

      const sorted = service.sort([], 'name', 'asc', config);

      expect(sorted).toEqual([]);
    });

    it('should not mutate original array', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.stringField('name', (item) => item.name),
      ];

      const original = [...testData];
      service.sort(testData, 'name', 'asc', config);

      expect(testData).toEqual(original);
    });

    it('should warn and return original array for unknown field', () => {
      const config: SortFieldConfig<TestItem>[] = [
        service.stringField('name', (item) => item.name),
      ];

      spyOn(console, 'warn');
      const sorted = service.sort(testData, 'unknown', 'asc', config);

      expect(console.warn).toHaveBeenCalledWith(
        'No sort configuration found for field: unknown',
      );
      expect(sorted).toBe(testData);
    });
  });

  describe('helper methods', () => {
    it('should create string field config', () => {
      const config = service.stringField('name', (item: TestItem) => item.name);

      expect(config.field).toBe('name');
      expect(
        config.getValue({
          name: 'Test',
          age: 0,
          date: new Date(),
          active: true,
        }),
      ).toBe('test');
    });

    it('should create number field config', () => {
      const config = service.numberField('age', (item: TestItem) => item.age);

      expect(config.field).toBe('age');
      expect(
        config.getValue({ name: '', age: 42, date: new Date(), active: true }),
      ).toBe(42);
    });

    it('should create date field config', () => {
      const config = service.dateField('date', (item: TestItem) => item.date);
      const testDate = new Date('2023-01-01');

      expect(config.field).toBe('date');
      expect(
        config.getValue({ name: '', age: 0, date: testDate, active: true }),
      ).toBe(testDate.getTime());
    });

    it('should create boolean field config', () => {
      const config = service.booleanField(
        'active',
        (item: TestItem) => item.active,
      );

      expect(config.field).toBe('active');
      expect(
        config.getValue({ name: '', age: 0, date: new Date(), active: true }),
      ).toBe(1);
      expect(
        config.getValue({ name: '', age: 0, date: new Date(), active: false }),
      ).toBe(0);
    });
  });
});
