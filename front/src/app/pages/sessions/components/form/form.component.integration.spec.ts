import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { By } from '@angular/platform-browser';
import {
  ActivatedRoute,
  Router,
  RouterLink,
  convertToParamMap,
} from '@angular/router';
import { beforeEach, describe, expect, it, jest } from '@jest/globals';
import { of } from 'rxjs';

import { SessionInformation } from '../../../../core/models/sessionInformation.interface';
import { Session } from '../../../../core/models/session.interface';
import { Teacher } from '../../../../core/models/teacher.interface';
import { SessionService } from '../../../../core/service/session.service';
import { SessionApiService } from '../../../../core/service/session-api.service';
import { TeacherService } from '../../../../core/service/teacher.service';
import { FormComponent } from './form.component';

describe('FormComponent integration', () => {
  let component: FormComponent;
  let fixture: ComponentFixture<FormComponent>;
  const mockSessionApiService: jest.Mocked<
    Pick<SessionApiService, 'create' | 'detail'>
  > = {
    create: jest.fn<SessionApiService['create']>(),
    detail: jest.fn<SessionApiService['detail']>(),
  };
  const mockTeacherService: jest.Mocked<Pick<TeacherService, 'all'>> = {
    all: jest.fn<TeacherService['all']>(),
  };
  const mockRouter = {
    navigate: jest.fn<Router['navigate']>(),
    url: '/sessions/create',
  };
  const mockActivatedRoute = {
    snapshot: {
      paramMap: convertToParamMap({}),
    },
  };
  const mockMatSnackBar: jest.Mocked<Pick<MatSnackBar, 'open'>> = {
    open: jest.fn<MatSnackBar['open']>(),
  };
  const sessionInformation: SessionInformation = {
    token: 'mock-token',
    type: 'Bearer',
    id: 1,
    username: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true,
  };
  const teachers: Teacher[] = [
    {
      id: 2,
      firstName: 'Teacher',
      lastName: 'Demo',
      createdAt: new Date('2026-08-01'),
      updatedAt: new Date('2026-08-02'),
    },
  ];

  const createComponent = (): void => {
    fixture = TestBed.createComponent(FormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  beforeEach(async () => {
    mockSessionApiService.create.mockReset();
    mockSessionApiService.detail.mockReset();
    mockTeacherService.all.mockReset();
    mockRouter.navigate.mockReset();
    mockRouter.url = '/sessions/create';
    mockActivatedRoute.snapshot.paramMap = convertToParamMap({});
    mockMatSnackBar.open.mockReset();
    mockTeacherService.all.mockReturnValue(of(teachers));

    await TestBed.configureTestingModule({
      imports: [FormComponent],
      providers: [
        SessionService,
        { provide: SessionApiService, useValue: mockSessionApiService },
        { provide: TeacherService, useValue: mockTeacherService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: mockActivatedRoute,
        },
      ],
    })
      .overrideComponent(FormComponent, {
        add: {
          providers: [
            { provide: MatSnackBar, useValue: mockMatSnackBar },
          ],
        },
      })
      .compileComponents();

    TestBed.inject(SessionService).logIn(sessionInformation);
  });

  it('should create a session when the required form values are provided', () => {
    createComponent();

    expect(component.onUpdate).toBe(false);
    expect(mockSessionApiService.detail).not.toHaveBeenCalled();
    expect(mockTeacherService.all).toHaveBeenCalled();
    expect(component.sessionForm?.getRawValue()).toEqual({
      name: '',
      date: '',
      teacher_id: '',
      description: '',
    });
    expect(component.sessionForm?.invalid).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Create session');

    const sessionToCreate = {
      name: 'Yoga Session',
      date: '2026-08-10',
      teacher_id: 2,
      description: 'A yoga session for all levels',
    };
    const createdSession: Session = {
      id: 10,
      ...sessionToCreate,
      date: new Date(sessionToCreate.date),
      users: [],
    };
    mockSessionApiService.create.mockReturnValue(of(createdSession));

    component.sessionForm?.patchValue(sessionToCreate);

    expect(component.sessionForm?.valid).toBe(true);

    component.submit();

    expect(mockSessionApiService.create).toHaveBeenCalledWith(sessionToCreate);
    expect(mockMatSnackBar.open).toHaveBeenCalledWith(
      'Session created !',
      'Close',
      { duration: 3000 }
    );
    expect(mockRouter.navigate).toHaveBeenCalledWith(['sessions']);
  });

  it('should provide a link back to the sessions list', () => {
    createComponent();

    const backLink = fixture.debugElement.query(By.directive(RouterLink));

    expect(backLink).not.toBeNull();
    expect(backLink.attributes['routerLink']).toBe('/sessions');
  });

  it('should initialize a prefilled form when editing an existing session', () => {
    const sessionToEdit: Session = {
      id: 10,
      name: 'Yoga Session',
      description: 'A yoga session for all levels',
      date: new Date('2026-08-10'),
      teacher_id: 2,
      users: [1],
      createdAt: new Date('2026-08-01'),
      updatedAt: new Date('2026-08-02'),
    };
    mockRouter.url = '/sessions/update/10';
    mockActivatedRoute.snapshot.paramMap = convertToParamMap({ id: '10' });
    mockSessionApiService.detail.mockReturnValue(of(sessionToEdit));

    createComponent();

    expect(component.onUpdate).toBe(true);
    expect(mockSessionApiService.detail).toHaveBeenCalledWith('10');
    expect(component.sessionForm?.getRawValue()).toEqual({
      name: 'Yoga Session',
      date: '2026-08-10',
      teacher_id: 2,
      description: 'A yoga session for all levels',
    });
    expect(component.sessionForm?.valid).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Update session');
  });
});
