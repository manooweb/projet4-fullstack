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
import { firstValueFrom, of, throwError } from 'rxjs';

import { AuthService } from '../../core/service/auth.service';
import { LoginRequest } from 'src/app/core/models/loginRequest.interface';
import { SessionInformation } from 'src/app/core/models/sessionInformation.interface';
import { By } from '@angular/platform-browser';
import { LoginComponent } from './login.component';

describe('LoginComponent integration', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let sessionService: SessionService;
  const mockAuthService: jest.Mocked<Pick<AuthService, 'login'>> = {
    login: jest.fn<AuthService['login']>(),
  };
  const mockRouter: jest.Mocked<Pick<Router, 'navigate'>> = {
    navigate: jest.fn<Router['navigate']>(),
  };

  beforeEach(async () => {
    mockAuthService.login.mockReset();
    mockRouter.navigate.mockReset();

    await TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        SessionService,
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
    sessionService = TestBed.inject(SessionService);
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

    const sessionInformation: SessionInformation = {
      token: 'mockToken',
      type: 'Bearer',
      id: 1,
      username: userEmail,
      firstName: 'Test',
      lastName: 'Demo',
      admin: false,
    };

    mockAuthService.login.mockReturnValue(of(sessionInformation));

    const form = component.form;
    form.patchValue(loginRequest);

    component.submit();

    expect(mockAuthService.login).toHaveBeenCalledWith(loginRequest);
    expect(sessionService.sessionInformation).toEqual(sessionInformation);
    expect(sessionService.isLogged).toBe(true);
    await expect(firstValueFrom(sessionService.$isLogged())).resolves.toBe(true);
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
    expect(sessionService.sessionInformation).toBeUndefined();
    expect(sessionService.isLogged).toBe(false);
    await expect(firstValueFrom(sessionService.$isLogged())).resolves.toBe(false);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
    expect(component.onError).toBe(true);

    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('p.error'));

    expect(errorMessage).not.toBeNull();
    expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
  });
});
