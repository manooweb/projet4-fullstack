import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
} from '@jest/globals';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { AuthService } from './auth.service';
import { SessionInformation } from '../models/sessionInformation.interface';

describe('AuthService integration', () => {
  let testContext: {
    service: AuthService;
    http: HttpTestingController;
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    testContext = {
      service: TestBed.inject(AuthService),
      http: TestBed.inject(HttpTestingController),
    };
  });

  afterEach(() => {
    testContext.http.verify();
  });

  it('should return session information when login succeeds', async () => {
    const sessionInformation: SessionInformation = {
      token: 'test-token',
      type: 'Bearer',
      id: 1,
      username: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      admin: false,
    };

    const loginRequest = {
      email: 'test@example.com',
      password: 'password',
    };

    const responsePromise = firstValueFrom(
      testContext.service.login(loginRequest)
    );

    const request = testContext.http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(loginRequest);

    request.flush(sessionInformation);

    await expect(responsePromise).resolves.toEqual(sessionInformation);
  });

  it('should complete registration when the request succeeds', async () => {
    const registerRequest = {
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'Demo',
      password: 'password',
    };

    const responsePromise = firstValueFrom(
      testContext.service.register(registerRequest)
    );

    const request = testContext.http.expectOne('/api/auth/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(registerRequest);

    request.flush(null);

    await expect(responsePromise).resolves.toEqual(null);
  });
});
