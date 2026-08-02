import { HttpClientModule } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from '@jest/globals';

import { SessionApiService } from './session-api.service';

describe('SessionsService', () => {
  let service: SessionApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports:[
        HttpClientModule
      ]
    });
    service = TestBed.inject(SessionApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
