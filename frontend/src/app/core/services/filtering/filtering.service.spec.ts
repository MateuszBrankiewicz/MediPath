import { TestBed } from '@angular/core/testing';
import { FilterFieldConfig, FilteringService } from './filtering.service';

interface TestItem {
  id: number;
  name: string;
  status: string;
  date: Date;
  score: number | null;
}

describe('FilteringService', () => {
  let service: FilteringService;
  let testData: TestItem[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FilteringService],
    });
    service = TestBed.inject(FilteringService);

    testData = [
      {
        id: 1,
        name: 'John Doe',
        status: 'active',
        date: new Date('2024-01-15'),
        score: 85,
      },
      {
        id: 2,
        name: 'Jane Smith',
        status: 'inactive',
        date: new Date('2024-02-20'),
        score: 92,
      },
      {
        id: 3,
        name: 'Bob Johnson',
        status: 'active',
        date: new Date('2024-03-10'),
        score: null,
      },
      {
        id: 4,
        name: 'Alice Brown',
        status: 'pending',
        date: new Date('2024-01-25'),
        score: 78,
      },
    ];
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('filter()', () => {
    it('should return all items when no filters are applied', () => {
      const config: FilterFieldConfig<TestItem> = {};
      const result = service.filter(testData, {}, config);

      expect(result).toEqual(testData);
      expect(result.length).toBe(4);
    });

    it('should filter by search term (case insensitive)', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter(testData, { searchTerm: 'john' }, config);

      expect(result.length).toBe(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Bob Johnson');
    });

    it('should trim search term', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter(
        testData,
        { searchTerm: '  jane  ' },
        config,
      );

      expect(result.length).toBe(1);
      expect(result[0].name).toBe('Jane Smith');
    });

    it('should search across multiple fields', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name, (item) => item.status],
      };

      const result = service.filter(
        testData,
        { searchTerm: 'pending' },
        config,
      );

      expect(result.length).toBe(1);
      expect(result[0].name).toBe('Alice Brown');
    });

    it('should handle null/undefined search field values', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.score],
      };

      const result = service.filter(testData, { searchTerm: '85' }, config);

      expect(result.length).toBe(1);
      expect(result[0].name).toBe('John Doe');
    });

    it('should filter by status (case insensitive)', () => {
      const config: FilterFieldConfig<TestItem> = {
        statusField: (item) => item.status,
      };

      const result = service.filter(testData, { status: 'ACTIVE' }, config);

      expect(result.length).toBe(2);
      expect(result[0].status).toBe('active');
      expect(result[1].status).toBe('active');
    });

    it('should not filter when status is "all"', () => {
      const config: FilterFieldConfig<TestItem> = {
        statusField: (item) => item.status,
      };

      const result = service.filter(testData, { status: 'all' }, config);

      expect(result.length).toBe(4);
    });

    it('should handle status with cancelled/canceled variations', () => {
      const dataWithCancelled = [
        ...testData,
        {
          id: 5,
          name: 'Test',
          status: 'cancelled',
          date: new Date(),
          score: 0,
        },
        {
          id: 6,
          name: 'Test2',
          status: 'canceled',
          date: new Date(),
          score: 0,
        },
      ];

      const config: FilterFieldConfig<TestItem> = {
        statusField: (item) => item.status,
      };

      const result1 = service.filter(
        dataWithCancelled,
        { status: 'cancelled' },
        config,
      );
      expect(result1.length).toBe(2);

      const result2 = service.filter(
        dataWithCancelled,
        { status: 'canceled' },
        config,
      );
      expect(result2.length).toBe(2);
    });

    it('should filter by date range', () => {
      const config: FilterFieldConfig<TestItem> = {
        dateField: (item) => item.date,
      };

      const result = service.filter(
        testData,
        {
          dateFrom: new Date('2024-01-20'),
          dateTo: new Date('2024-02-25'),
        },
        config,
      );

      expect(result.length).toBe(2);
      expect(result[0].name).toBe('Jane Smith');
      expect(result[1].name).toBe('Alice Brown');
    });

    it('should filter by dateFrom only', () => {
      const config: FilterFieldConfig<TestItem> = {
        dateField: (item) => item.date,
      };

      const result = service.filter(
        testData,
        { dateFrom: new Date('2024-02-01') },
        config,
      );

      expect(result.length).toBe(2);
      expect(result[0].name).toBe('Jane Smith');
      expect(result[1].name).toBe('Bob Johnson');
    });

    it('should filter by dateTo only', () => {
      const config: FilterFieldConfig<TestItem> = {
        dateField: (item) => item.date,
      };

      const result = service.filter(
        testData,
        { dateTo: new Date('2024-02-01') },
        config,
      );

      expect(result.length).toBe(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Alice Brown');
    });

    it('should handle string dates', () => {
      const dataWithStringDates = testData.map((item) => ({
        ...item,
        date: item.date.toISOString(),
      }));

      const config: FilterFieldConfig<(typeof dataWithStringDates)[0]> = {
        dateField: (item) => item.date,
      };

      const result = service.filter(
        dataWithStringDates,
        { dateFrom: new Date('2024-02-01') },
        config,
      );

      expect(result.length).toBe(2);
    });

    it('should apply custom filters', () => {
      const config: FilterFieldConfig<TestItem> = {
        customFilters: [(item) => item.score !== null && item.score > 80],
      };

      const result = service.filter(testData, {}, config);

      expect(result.length).toBe(2);
      expect(result[0].name).toBe('John Doe');
      expect(result[1].name).toBe('Jane Smith');
    });

    it('should apply multiple custom filters (AND logic)', () => {
      const config: FilterFieldConfig<TestItem> = {
        customFilters: [
          (item) => item.score !== null && item.score > 80,
          (item) => item.status === 'active',
        ],
      };

      const result = service.filter(testData, {}, config);

      expect(result.length).toBe(1);
      expect(result[0].name).toBe('John Doe');
    });

    it('should combine all filter types', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
        statusField: (item) => item.status,
        dateField: (item) => item.date,
        customFilters: [(item) => item.score !== null],
      };

      const result = service.filter(
        testData,
        {
          searchTerm: 'o',
          status: 'active',
          dateFrom: new Date('2024-01-01'),
          dateTo: new Date('2024-02-01'),
        },
        config,
      );

      expect(result.length).toBe(1);
      expect(result[0].name).toBe('John Doe');
    });

    it('should return empty array when no items match', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter(
        testData,
        { searchTerm: 'nonexistent' },
        config,
      );

      expect(result).toEqual([]);
    });
  });

  describe('Helper methods', () => {
    describe('searchConfig()', () => {
      it('should create search configuration', () => {
        const config = service.searchConfig<TestItem>(
          (item) => item.name,
          (item) => item.status,
        );

        expect(config.searchFields).toBeDefined();
        expect(config.searchFields?.length).toBe(2);
      });

      it('should work with filter method', () => {
        const config = service.searchConfig<TestItem>((item) => item.name);

        const result = service.filter(testData, { searchTerm: 'jane' }, config);

        expect(result.length).toBe(1);
      });
    });

    describe('dateRangeConfig()', () => {
      it('should create date range configuration', () => {
        const config = service.dateRangeConfig<TestItem>((item) => item.date);

        expect(config.dateField).toBeDefined();
      });

      it('should work with filter method', () => {
        const config = service.dateRangeConfig<TestItem>((item) => item.date);

        const result = service.filter(
          testData,
          { dateFrom: new Date('2024-02-01') },
          config,
        );

        expect(result.length).toBe(2);
      });
    });

    describe('statusConfig()', () => {
      it('should create status configuration', () => {
        const config = service.statusConfig<TestItem>((item) => item.status);

        expect(config.statusField).toBeDefined();
      });

      it('should work with filter method', () => {
        const config = service.statusConfig<TestItem>((item) => item.status);

        const result = service.filter(testData, { status: 'active' }, config);

        expect(result.length).toBe(2);
      });
    });

    describe('combineConfigs()', () => {
      it('should combine multiple configurations', () => {
        const searchConf = service.searchConfig<TestItem>((item) => item.name);
        const statusConf = service.statusConfig<TestItem>(
          (item) => item.status,
        );
        const dateConf = service.dateRangeConfig<TestItem>((item) => item.date);

        const combined = service.combineConfigs(
          searchConf,
          statusConf,
          dateConf,
        );

        expect(combined.searchFields).toBeDefined();
        expect(combined.statusField).toBeDefined();
        expect(combined.dateField).toBeDefined();
      });

      it('should merge search fields from multiple configs', () => {
        const config1 = service.searchConfig<TestItem>((item) => item.name);
        const config2 = service.searchConfig<TestItem>((item) => item.status);

        const combined = service.combineConfigs(config1, config2);

        expect(combined.searchFields?.length).toBe(2);
      });

      it('should merge custom filters', () => {
        const config1: Partial<FilterFieldConfig<TestItem>> = {
          customFilters: [(item) => item.score !== null],
        };
        const config2: Partial<FilterFieldConfig<TestItem>> = {
          customFilters: [(item) => item.status === 'active'],
        };

        const combined = service.combineConfigs(config1, config2);

        expect(combined.customFilters?.length).toBe(2);
      });

      it('should work with filter method', () => {
        const combined = service.combineConfigs(
          service.searchConfig<TestItem>((item) => item.name),
          service.statusConfig<TestItem>((item) => item.status),
        );

        const result = service.filter(
          testData,
          { searchTerm: 'john', status: 'active' },
          combined,
        );

        expect(result.length).toBe(1);
        expect(result[0].name).toBe('John Doe');
      });
    });
  });

  describe('Edge cases', () => {
    it('should handle empty array', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter([], { searchTerm: 'test' }, config);

      expect(result).toEqual([]);
    });

    it('should handle undefined options', () => {
      const config: FilterFieldConfig<TestItem> = {};
      const result = service.filter(testData, {}, config);

      expect(result.length).toBe(4);
    });

    it('should handle null values in filter options', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
        dateField: (item) => item.date,
      };

      const result = service.filter(
        testData,
        { searchTerm: undefined, dateFrom: null, dateTo: null },
        config,
      );

      expect(result.length).toBe(4);
    });

    it('should handle items with null date field', () => {
      const dataWithNullDate = [
        ...testData,
        {
          id: 5,
          name: 'No Date',
          status: 'active',
          date: null as unknown as Date,
          score: 50,
        },
      ];

      const config: FilterFieldConfig<TestItem> = {
        dateField: (item) => item.date,
      };

      const result = service.filter(
        dataWithNullDate,
        { dateFrom: new Date('2024-01-01') },
        config,
      );

      expect(result.length).toBe(4);
      expect(result.every((item) => item.date !== null)).toBe(true);
    });

    it('should handle empty search term', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter(testData, { searchTerm: '' }, config);

      expect(result.length).toBe(4);
    });

    it('should handle whitespace-only search term', () => {
      const config: FilterFieldConfig<TestItem> = {
        searchFields: [(item) => item.name],
      };

      const result = service.filter(testData, { searchTerm: '   ' }, config);

      expect(result.length).toBe(4);
    });
  });
});
