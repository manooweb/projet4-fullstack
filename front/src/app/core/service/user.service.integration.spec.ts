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

import { User } from '../models/user.interface';
import { UserService } from './user.service';

describe('UserService integration', () => {
  let testContext: {
    service: UserService;
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
      service: TestBed.inject(UserService),
      http: TestBed.inject(HttpTestingController),
    };
  });

  afterEach(() => {
    testContext.http.verify();
  });

  it('should return user details when requested', async () => {
    const userId = 1;
    const user: User = {
      id: userId,
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'Demo',
      password: 'password',
      admin: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    const responsePromise = firstValueFrom(
      testContext.service.getById(userId.toString())
    );

    const request = testContext.http.expectOne(`/api/user/${userId}`);
    expect(request.request.method).toBe('GET');

    request.flush(user);

    await expect(responsePromise).resolves.toEqual(user);
  });

  it('should complete deletion when the request succeeds', async () => {
    const userId = 1;

    const responsePromise = firstValueFrom(
      testContext.service.delete(userId.toString())
    );

    const request = testContext.http.expectOne(`/api/user/${userId}`);
    expect(request.request.method).toBe('DELETE');

    request.flush(null);

    await expect(responsePromise).resolves.toBeNull();
  });
});
