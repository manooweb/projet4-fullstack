import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { SessionService } from 'src/app/core/service/session.service';

import { AuthService } from '../../core/service/auth.service';
import { LoginComponent } from './login.component';
import { of, throwError } from 'rxjs';
import { LoginRequest } from 'src/app/core/models/loginRequest.interface';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('LoginComponent integration', () => {
  let debugElement: DebugElement;
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  const mockAuthService: jest.Mocked<Pick<AuthService, 'login'>> = {
    login: jest.fn<AuthService['login']>(),
  };
  const mockSessionService: jest.Mocked<Pick<SessionService, 'logIn'>> = {
    logIn: jest.fn<SessionService['logIn']>(),
  };
  const mockRouter: jest.Mocked<Pick<Router, 'navigate'>> = {
    navigate: jest.fn<Router['navigate']>(),
  };

  beforeEach(async () => {
    mockAuthService.login.mockReset();
    mockSessionService.logIn.mockReset();
    mockRouter.navigate.mockReset();

    await TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: SessionService, useValue: mockSessionService },
        { provide: Router, useValue: mockRouter },
      ],
      imports: [
        BrowserAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        LoginComponent,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should log in the user and navigate to sessions when authentication succeeds', async () => {
    const userEmail = 'test@example.com';
    const userPassword = 'password';

    const loginRequest: LoginRequest = {
      email: userEmail,
      password: userPassword
    }

    const mockSessionInformation: SessionInformation = {
      token: 'mockToken',
      type: 'Bearer',
      id: 1,
      username: userEmail,
      firstName: 'Test',
      lastName: 'Demo',
      admin: false,
    };

    mockAuthService.login.mockReturnValue(of(mockSessionInformation));

    const form = component.form;
    form.patchValue(loginRequest);

    component.submit();

    expect(mockAuthService.login).toHaveBeenCalledWith(loginRequest);
    expect(mockSessionService.logIn).toHaveBeenCalledWith(mockSessionInformation);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/sessions']);
  });

  it('should not log in the user, nor navigate to sessions and display an error message when authentication fails', async () => {
    const userEmail = 'test@example.com';
    const userPassword = 'password';

    const loginRequest: LoginRequest = {
      email: userEmail,
      password: userPassword
    }

    mockAuthService.login.mockReturnValue(throwError(() => new Error('Authentication failed')));

    const form = component.form;
    form.patchValue(loginRequest);

    component.submit();

    expect(mockAuthService.login).toHaveBeenCalledWith(loginRequest);
    expect(mockSessionService.logIn).not.toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
    expect(component.onError).toBe(true);

    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('p.error'));

    expect(errorMessage).not.toBeNull();
    expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
  });
});
