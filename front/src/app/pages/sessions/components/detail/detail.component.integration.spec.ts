import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { By } from '@angular/platform-browser';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { of } from 'rxjs';

import { SessionInformation } from '../../../../core/models/sessionInformation.interface';
import { Session } from '../../../../core/models/session.interface';
import { Teacher } from '../../../../core/models/teacher.interface';
import { SessionService } from '../../../../core/service/session.service';
import { SessionApiService } from '../../../../core/service/session-api.service';
import { TeacherService } from '../../../../core/service/teacher.service';
import { DetailComponent } from './detail.component';

describe('DetailComponent integration', () => {
  let component: DetailComponent;
  let fixture: ComponentFixture<DetailComponent>;
  const mockSessionApiService: jest.Mocked<
    Pick<SessionApiService, 'delete' | 'detail' | 'participate' | 'unParticipate'>
  > = {
    delete: jest.fn<SessionApiService['delete']>(),
    detail: jest.fn<SessionApiService['detail']>(),
    participate: jest.fn<SessionApiService['participate']>(),
    unParticipate: jest.fn<SessionApiService['unParticipate']>(),
  };
  const mockTeacherService: jest.Mocked<Pick<TeacherService, 'detail'>> = {
    detail: jest.fn<TeacherService['detail']>(),
  };
  const mockRouter: jest.Mocked<Pick<Router, 'navigate'>> = {
    navigate: jest.fn<Router['navigate']>(),
  };
  const mockMatSnackBar: jest.Mocked<Pick<MatSnackBar, 'open'>> = {
    open: jest.fn<MatSnackBar['open']>(),
  };
  const sessionInformation: SessionInformation = {
    token: 'mock-token',
    type: 'Bearer',
    id: 1,
    username: 'test@example.com',
    firstName: 'Test',
    lastName: 'User',
    admin: false,
  };
  const session: Session = {
    id: 10,
    name: 'Yoga Session',
    description: 'A yoga session for all levels',
    date: new Date('2026-08-10'),
    teacher_id: 2,
    users: [],
    createdAt: new Date('2026-08-01'),
    updatedAt: new Date('2026-08-02'),
  };
  const teacher: Teacher = {
    id: 2,
    firstName: 'Teacher',
    lastName: 'Demo',
    createdAt: new Date('2026-08-01'),
    updatedAt: new Date('2026-08-02'),
  };
  const adminSessionInformation: SessionInformation = {
    ...sessionInformation,
    admin: true,
  };

  const createComponent = (): void => {
    fixture = TestBed.createComponent(DetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  beforeEach(async () => {
    mockSessionApiService.delete.mockReset();
    mockSessionApiService.detail.mockReset();
    mockSessionApiService.participate.mockReset();
    mockSessionApiService.unParticipate.mockReset();
    mockTeacherService.detail.mockReset();
    mockRouter.navigate.mockReset();
    mockMatSnackBar.open.mockReset();
    mockTeacherService.detail.mockReturnValue(of(teacher));

    await TestBed.configureTestingModule({
      imports: [DetailComponent],
      providers: [
        SessionService,
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: '10' }),
            },
          },
        },
      ],
    })
      .overrideComponent(DetailComponent, {
        add: {
          providers: [
            { provide: MatSnackBar, useValue: mockMatSnackBar },
          ],
        },
      })
      .compileComponents();

    TestBed.inject(SessionService).logIn(sessionInformation);
  });

  it('should load and display the session and its teacher for a non-participating user', () => {
    mockSessionApiService.detail.mockReturnValue(of(session));

    createComponent();

    expect(mockSessionApiService.detail).toHaveBeenCalledWith('10');
    expect(mockTeacherService.detail).toHaveBeenCalledWith('2');
    expect(component.session).toEqual(session);
    expect(component.teacher).toEqual(teacher);
    expect(component.isParticipate).toBe(false);
    expect(fixture.nativeElement.textContent).toContain('Yoga Session');
    expect(fixture.nativeElement.textContent).toContain('Teacher DEMO');

    const buttonTexts = fixture.debugElement
      .queryAll(By.css('button'))
      .map(button => button.nativeElement.textContent);

    expect(buttonTexts.join(' ')).toContain('Participate');
    expect(buttonTexts.join(' ')).not.toContain('Do not participate');
  });

  it('should participate, then refresh the session information', () => {
    const participatingSession: Session = {
      ...session,
      users: [sessionInformation.id],
    };
    mockSessionApiService.detail
      .mockReturnValueOnce(of(session))
      .mockReturnValueOnce(of(participatingSession));
    mockSessionApiService.participate.mockReturnValue(of(void 0));

    createComponent();

    component.participate();
    fixture.detectChanges();

    expect(mockSessionApiService.participate).toHaveBeenCalledWith('10', '1');
    expect(mockSessionApiService.detail).toHaveBeenCalledTimes(2);
    expect(component.session).toEqual(participatingSession);
    expect(component.isParticipate).toBe(true);
  });

  it('should cancel participation, then refresh the session information', () => {
    const participatingSession: Session = {
      ...session,
      users: [sessionInformation.id],
    };
    mockSessionApiService.detail
      .mockReturnValueOnce(of(participatingSession))
      .mockReturnValueOnce(of(session));
    mockSessionApiService.unParticipate.mockReturnValue(of(void 0));

    createComponent();

    component.unParticipate();
    fixture.detectChanges();

    expect(mockSessionApiService.unParticipate).toHaveBeenCalledWith('10', '1');
    expect(mockSessionApiService.detail).toHaveBeenCalledTimes(2);
    expect(component.session).toEqual(session);
    expect(component.isParticipate).toBe(false);
  });

  it('should delete the session and navigate to the sessions list when the user is an admin', () => {
    mockSessionApiService.detail.mockReturnValue(of(session));
    mockSessionApiService.delete.mockReturnValue(of(void 0));
    TestBed.inject(SessionService).logIn(adminSessionInformation);

    createComponent();

    component.delete();

    expect(component.isAdmin).toBe(true);
    expect(mockSessionApiService.delete).toHaveBeenCalledWith('10');
    expect(mockMatSnackBar.open).toHaveBeenCalledWith(
      'Session deleted !',
      'Close',
      { duration: 3000 }
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
  });
});
