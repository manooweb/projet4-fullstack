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

import { Session } from '../models/session.interface';
import { SessionApiService } from './session-api.service';

describe('SessionApiService integration', () => {
  let testContext: {
    service: SessionApiService;
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
      service: TestBed.inject(SessionApiService),
      http: TestBed.inject(HttpTestingController),
    };
  });

  afterEach(() => {
    testContext.http.verify();
  });

  it('should return all sessions when requested', async () => {
    const sessions: Session[] = [
      {
        id: 1,
        name: 'Yoga session',
        description: 'First session',
        date: new Date('2026-08-10'),
        teacher_id: 1,
        users: [1, 2],
        createdAt: new Date(),
        updatedAt: new Date(),
      },
      {
        id: 2,
        name: 'Advanced yoga session',
        description: 'Second session',
        date: new Date('2026-08-11'),
        teacher_id: 2,
        users: [3],
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    const responsePromise = firstValueFrom(
      testContext.service.all()
    );

    const request = testContext.http.expectOne('/api/session');
    expect(request.request.method).toBe('GET');

    request.flush(sessions);

    await expect(responsePromise).resolves.toEqual(sessions);
  });

  it('should return session details when requested', async () => {
    const sessionId = 1;
    const session: Session = {
      id: sessionId,
      name: 'Yoga session',
      description: 'Session details',
      date: new Date('2026-08-10'),
      teacher_id: 1,
      users: [1, 2],
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    const responsePromise = firstValueFrom(
      testContext.service.detail(sessionId.toString())
    );

    const request = testContext.http.expectOne(`/api/session/${sessionId}`);
    expect(request.request.method).toBe('GET');

    request.flush(session);

    await expect(responsePromise).resolves.toEqual(session);
  });

  it('should complete deletion when the request succeeds', async () => {
    const sessionId = 1;

    const responsePromise = firstValueFrom(
      testContext.service.delete(sessionId.toString())
    );

    const request = testContext.http.expectOne(`/api/session/${sessionId}`);
    expect(request.request.method).toBe('DELETE');

    request.flush(null);

    await expect(responsePromise).resolves.toBeNull();
  });

  it('should return the created session when creation succeeds', async () => {
    const sessionToCreate: Session = {
      name: 'New yoga session',
      description: 'New session description',
      date: new Date('2026-08-12'),
      teacher_id: 1,
      users: [],
    };
    const createdSession: Session = {
      ...sessionToCreate,
      id: 3,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    const responsePromise = firstValueFrom(
      testContext.service.create(sessionToCreate)
    );

    const request = testContext.http.expectOne('/api/session');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(sessionToCreate);

    request.flush(createdSession);

    await expect(responsePromise).resolves.toEqual(createdSession);
  });

  it('should return the updated session when update succeeds', async () => {
    const sessionId = 1;
    const sessionToUpdate: Session = {
      id: sessionId,
      name: 'Updated yoga session',
      description: 'Updated session description',
      date: new Date('2026-08-13'),
      teacher_id: 2,
      users: [1, 3],
    };

    const responsePromise = firstValueFrom(
      testContext.service.update(sessionId.toString(), sessionToUpdate)
    );

    const request = testContext.http.expectOne(`/api/session/${sessionId}`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(sessionToUpdate);

    request.flush(sessionToUpdate);

    await expect(responsePromise).resolves.toEqual(sessionToUpdate);
  });

  it('should complete participation when the request succeeds', async () => {
    const sessionId = 1;
    const userId = 2;

    const responsePromise = firstValueFrom(
      testContext.service.participate(
        sessionId.toString(),
        userId.toString()
      )
    );

    const request = testContext.http.expectOne(
      `/api/session/${sessionId}/participate/${userId}`
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();

    request.flush(null);

    await expect(responsePromise).resolves.toBeNull();
  });

  it('should complete cancellation when unparticipation succeeds', async () => {
    const sessionId = 1;
    const userId = 2;

    const responsePromise = firstValueFrom(
      testContext.service.unParticipate(
        sessionId.toString(),
        userId.toString()
      )
    );

    const request = testContext.http.expectOne(
      `/api/session/${sessionId}/participate/${userId}`
    );
    expect(request.request.method).toBe('DELETE');

    request.flush(null);

    await expect(responsePromise).resolves.toBeNull();
  });
});
