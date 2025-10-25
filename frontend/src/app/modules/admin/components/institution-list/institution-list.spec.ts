import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { InstitutionList } from './institution-list';

describe('InstitutionList', () => {
  let component: InstitutionList;
  let fixture: ComponentFixture<InstitutionList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InstitutionList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(InstitutionList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
