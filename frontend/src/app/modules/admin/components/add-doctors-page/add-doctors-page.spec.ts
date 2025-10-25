import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { AddDoctorsPage } from './add-doctors-page';

describe('AddDoctorsPage', () => {
  let component: AddDoctorsPage;
  let fixture: ComponentFixture<AddDoctorsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddDoctorsPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AddDoctorsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
