import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RefferalsPage } from './refferals-page';

describe('RefferalsPage', () => {
  let component: RefferalsPage;
  let fixture: ComponentFixture<RefferalsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RefferalsPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RefferalsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
