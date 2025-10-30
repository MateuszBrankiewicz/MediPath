import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DialogService } from 'primeng/dynamicdialog';
import { VisitDialogService } from './visit-dialog.service';

describe('VisitDialogService', () => {
  let service: VisitDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        VisitDialogService,
        DialogService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });
    service = TestBed.inject(VisitDialogService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
