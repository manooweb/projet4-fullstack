import { Session } from '../../src/app/core/models/session.interface';

describe('Session detail spec', () => {
  it('Should display an empty session form to create a new session', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher',
      },
      {
        statusCode: 200,
        body: [teacherResponse]
      }).as('teacherDetail');

    cy.contains('mat-card-header button', 'Create').click();

    cy.wait('@teacherDetail');

    cy.get('h1').contains('Create session').should('be.visible');
    cy.get('mat-select[formControlName=teacher_id]').click();
    cy.get('mat-option').should('contain.text', 'Teacher Learn');
  });

  it('Should display an correctly populated session form to edit a session', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher',
      },
      {
        statusCode: 200,
        body: [teacherResponse]
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

    cy.contains('mat-card-actions button', 'Edit').click();

    cy.wait('@teacherDetail');

    cy.get('h1').contains('Update session').should('be.visible');
    cy.get('input[formControlName=name]').should('have.value', 'Yoga Session');
    cy.get('textarea[formControlName=description]').should('have.value', 'A relaxing yoga session');
    cy.get('input[formControlName=date]').should('have.value', '2026-08-05');
    cy.get('mat-select[formControlName=teacher_id]').should('contain.text', 'Teacher Learn');

    cy.intercept(
      {
        method: 'PUT',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

      cy.get('button[type=submit]').click();
  });
});
