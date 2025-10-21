import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FilterButtonsComponent } from './filter-buttons.component';

describe('FilterButtonsComponent', () => {
  let component: FilterButtonsComponent<string>;
  let fixture: ComponentFixture<FilterButtonsComponent<string>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilterButtonsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(FilterButtonsComponent<string>);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit filterChange event when button is clicked', () => {
    const filters = [
      { value: 'all', labelKey: 'test.all', icon: 'pi-list' },
      { value: 'active', labelKey: 'test.active', icon: 'pi-check' },
    ];

    fixture.componentRef.setInput('filters', filters);
    fixture.componentRef.setInput('activeFilter', 'all');

    let emittedValue: string | undefined;
    component.filterChange.subscribe((value) => {
      emittedValue = value;
    });

    component['onFilterClick']('active');

    expect(emittedValue).toBe('active');
  });

  it('should correctly identify active filter', () => {
    const filters = [
      { value: 'test1', labelKey: 'test.1', icon: 'pi-1' },
      { value: 'test2', labelKey: 'test.2', icon: 'pi-2' },
    ];

    fixture.componentRef.setInput('filters', filters);
    fixture.componentRef.setInput('activeFilter', 'test1');
    fixture.detectChanges();

    const isActive = component['isActive']();
    expect(isActive('test1')).toBe(true);
    expect(isActive('test2')).toBe(false);
  });
});
