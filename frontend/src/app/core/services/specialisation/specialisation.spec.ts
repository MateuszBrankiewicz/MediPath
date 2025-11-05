import { TestBed } from '@angular/core/testing';

import { Specialisation } from './specialisation';

describe('Specialisation', () => {
  let service: Specialisation;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Specialisation);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
