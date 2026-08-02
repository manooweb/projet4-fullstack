import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';

import { AuthService } from '../../core/service/auth.service';
import { By } from '@angular/platform-browser';
import { RegisterComponent } from './register.component';
import { RegisterRequest } from 'src/app/core/models/registerRequest.interface';
import { of, throwError } from 'rxjs';

describe('RegisterComponent integration', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  const mockAuthService: jest.Mocked<Pick<AuthService, 'register'>> = {
    register: jest.fn(),
  };
  const mockRouter: jest.Mocked<Pick<Router, 'navigate'>> = {
    navigate: jest.fn<Router['navigate']>(),
  };
  const registerRequest: RegisterRequest = {
    email: 'test@example.com',
    firstName: 'Test',
    lastName: 'Demo',
    password: 'password',
  }

  beforeEach(async () => {
    mockAuthService.register.mockReset();
    mockRouter.navigate.mockReset();

    await TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
      ],
      imports: [
        BrowserAnimationsModule,
        MatCardModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ReactiveFormsModule,
        RegisterComponent,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should register the user and navigate to login when registration succeeds', () => {
    mockAuthService.register.mockReturnValue(of(void 0));

    const form = component.form;
    form.patchValue(registerRequest);

    component.submit();

    expect(mockAuthService.register).toHaveBeenCalledWith(registerRequest);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('should not navigate to login and display an error message when registration fails', () => {
    mockAuthService.register.mockReturnValue(throwError(() => new Error('Registration failed')));

    const form = component.form;
    form.patchValue(registerRequest);

    component.submit();

    expect(mockAuthService.register).toHaveBeenCalledWith(registerRequest);
    expect(mockRouter.navigate).not.toHaveBeenCalled();
    expect(component.onError).toBe(true);

    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('p.error'));

    expect(errorMessage).not.toBeNull();
    expect(errorMessage.nativeElement.textContent).toContain('An error occurred');
  });
});
