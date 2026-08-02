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

import { Teacher } from '../models/teacher.interface';
import { TeacherService } from './teacher.service';

describe('TeacherService integration', () => {
  let testContext: {
    service: TeacherService;
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
      service: TestBed.inject(TeacherService),
      http: TestBed.inject(HttpTestingController),
    };
  });

  afterEach(() => {
    testContext.http.verify();
  });

  it('should return teacher details when requested', async () => {
    const teacherId = 1;
    const teacher: Teacher = {
      id: teacherId,
      lastName: 'User',
      firstName: 'Test',
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    const responsePromise = firstValueFrom(
      testContext.service.detail(teacherId.toString())
    );

    const request = testContext.http.expectOne(`/api/teacher/${teacherId}`);
    expect(request.request.method).toBe('GET');

    request.flush(teacher);

    await expect(responsePromise).resolves.toEqual(teacher);
  });

  it('should return all teachers when requested', async () => {
    const teachers: Teacher[] = [
      {
        id: 1,
        lastName: 'User',
        firstName: 'Test',
        createdAt: new Date(),
        updatedAt: new Date(),
      },
      {
        id: 2,
        lastName: 'Teacher',
        firstName: 'Second',
        createdAt: new Date(),
        updatedAt: new Date(),
      },
    ];

    const responsePromise = firstValueFrom(
      testContext.service.all()
    );

    const request = testContext.http.expectOne('/api/teacher');
    expect(request.request.method).toBe('GET');

    request.flush(teachers);

    await expect(responsePromise).resolves.toEqual(teachers);
  });
});
