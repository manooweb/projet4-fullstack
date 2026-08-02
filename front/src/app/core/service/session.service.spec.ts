import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from '@jest/globals';

import { SessionService } from './session.service';
import { SessionInformation } from '../models/sessionInformation.interface';

describe('SessionService', () => {
  let service: SessionService;
  const user: SessionInformation = {
    token: 'mockToken',
    type: 'Bearer',
    id: 1,
    username: 'Test User',
    firstName: 'Test',
    lastName: 'Demo',
    admin: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should emit false as the correct initial logged-in status', (done) => {
    service.$isLogged().subscribe((isLogged) => {
      expect(isLogged).toBe(false);
      done();
    });
  });

  it('should log in a user, update the session information and emit true as the correct logged-in status', (done) => {
    service.logIn(user);

    service.$isLogged().subscribe((isLogged) => {
      expect(isLogged).toBe(true);
      done();
    });

    expect(service.isLogged).toBe(true);
    expect(service.sessionInformation).toEqual(user);
  });

  it('should log out a user and clear the session information and emit false as the correct logged-in status', (done) => {
    service.logIn(user);
    service.logOut();

    service.$isLogged().subscribe((isLogged) => {
      expect(isLogged).toBe(false);
      done();
    });

    expect(service.isLogged).toBe(false);
    expect(service.sessionInformation).toBeUndefined();
  });
});
