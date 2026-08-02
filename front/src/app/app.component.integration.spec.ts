import { TestBed } from '@angular/core/testing';
import { MatToolbarModule } from '@angular/material/toolbar';
import { provideRouter, Router } from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { firstValueFrom } from 'rxjs';

import { SessionInformation } from './core/models/sessionInformation.interface';
import { SessionService } from './core/service/session.service';
import { AppComponent } from './app.component';

describe('AppComponent integration', () => {
  let component: AppComponent;
  let sessionService: SessionService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatToolbarModule,
        AppComponent,
      ],
      providers: [provideRouter([])],
    }).compileComponents();

    component = TestBed.createComponent(AppComponent).componentInstance;
    sessionService = TestBed.inject(SessionService);
    router = TestBed.inject(Router);
  });

  it('should log out the user and navigate to the home page', async () => {
    const sessionInformation: SessionInformation = {
      token: 'mock-token',
      type: 'Bearer',
      id: 1,
      username: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      admin: false,
    };
    const navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    sessionService.logIn(sessionInformation);

    component.logout();

    expect(sessionService.sessionInformation).toBeUndefined();
    expect(sessionService.isLogged).toBe(false);
    await expect(firstValueFrom(component.$isLogged())).resolves.toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['']);
  });
});
