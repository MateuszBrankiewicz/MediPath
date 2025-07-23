import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectWithSearch } from './select-with-search';

describe('SelectWithSearch', () => {
  let component: SelectWithSearch;
  let fixture: ComponentFixture<SelectWithSearch>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectWithSearch]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SelectWithSearch);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
