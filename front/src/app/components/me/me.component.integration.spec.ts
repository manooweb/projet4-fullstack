import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { firstValueFrom, of } from 'rxjs';

import { SessionInformation } from '../../core/models/sessionInformation.interface';
import { User } from '../../core/models/user.interface';
import { SessionService } from '../../core/service/session.service';
import { UserService } from '../../core/service/user.service';
import { MeComponent } from './me.component';

describe('MeComponent integration', () => {
  let component: MeComponent;
  let fixture: ComponentFixture<MeComponent>;
  let sessionService: SessionService;
  const mockUserService: jest.Mocked<
    Pick<UserService, 'delete' | 'getById'>
  > = {
    delete: jest.fn<UserService['delete']>(),
    getById: jest.fn<UserService['getById']>(),
  };
  const mockMatSnackBar: jest.Mocked<Pick<MatSnackBar, 'open'>> = {
    open: jest.fn<MatSnackBar['open']>(),
  };
  const mockRouter: jest.Mocked<Pick<Router, 'navigate'>> = {
    navigate: jest.fn<Router['navigate']>(),
  };
  const sessionInformation: SessionInformation = {
    token: 'mock-token',
    type: 'Bearer',
    id: 1,
    username: 'test@example.com',
    firstName: 'Test',
    lastName: 'Demo',
    admin: false,
  };
  const user: User = {
    id: 1,
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'Demo',
    password: 'password',
    admin: false,
    createdAt: new Date('2026-08-01'),
    updatedAt: new Date('2026-08-02'),
  };

  beforeEach(async () => {
    mockUserService.getById.mockReset();
    mockUserService.delete.mockReset();
    mockMatSnackBar.open.mockReset();
    mockRouter.navigate.mockReset();
    mockUserService.getById.mockReturnValue(of(user));

    await TestBed.configureTestingModule({
      imports: [MeComponent],
      providers: [
        SessionService,
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter },
      ],
    })
      .overrideComponent(MeComponent, {
        add: {
          providers: [
            { provide: MatSnackBar, useValue: mockMatSnackBar },
          ],
        },
      })
      .compileComponents();

    sessionService = TestBed.inject(SessionService);
    sessionService.logIn(sessionInformation);

    fixture = TestBed.createComponent(MeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load and display the authenticated user information', () => {
    expect(mockUserService.getById).toHaveBeenCalledWith('1');
    expect(component.user).toEqual(user);
    expect(fixture.nativeElement.textContent).toContain('Name: Test DEMO');
    expect(fixture.nativeElement.textContent).toContain('Email: test@example.com');
  });

  it('should delete the authenticated user, log out and navigate home', async () => {
    mockUserService.delete.mockReturnValue(of(void 0));

    component.delete();

    expect(mockUserService.delete).toHaveBeenCalledWith('1');
    expect(mockMatSnackBar.open).toHaveBeenCalledWith(
      'Your account has been deleted !',
      'Close',
      { duration: 3000 }
    );
    expect(sessionService.sessionInformation).toBeUndefined();
    expect(sessionService.isLogged).toBe(false);
    await expect(firstValueFrom(sessionService.$isLogged())).resolves.toBe(false);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
