describe('Account spec', () => {
  it('Should display user information correctly when the user is authenticated as a regular user', () => {
    const userSession = {
      token: 'fake-jwt-user',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const accountResponse = {
      id: 1,
      email: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false,
      password: '',
      createdAt: '2026-01-10T10:00:00.000Z',
      updatedAt: '2026-02-10T10:00:00.000Z'
    }
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
      []).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.url().should('include', '/sessions');
    cy.contains('span.link', 'Account').should('be.visible');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/user/1',
      },
      {
        statusCode: 200,
        body: accountResponse
      }).as('me');

    cy.get('span.link').contains('Account').click();

    cy.url().should('include', '/me');
    cy.contains('h1', 'User information').should('be.visible');
    cy.contains('p', 'Name: Test DEMO').should('be.visible');
    cy.contains('p', 'Email: test@studio.com').should('be.visible');
    cy.contains('p', 'Delete my account:').should('be.visible');
    cy.contains('button', 'Delete').should('be.visible');
  });

  it('Should not display delete button when the user is authenticated as an admin user', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true
    };

    const accountResponse = {
      id: 1,
      email: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true,
      password: '',
      createdAt: '2026-01-10T10:00:00.000Z',
      updatedAt: '2026-02-10T10:00:00.000Z'
    }
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
      []).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/user/1',
      },
      {
        statusCode: 200,
        body: accountResponse
      }).as('me');

    cy.get('span.link').contains('Account').click();

    cy.url().should('include', '/me');
    cy.contains('h1', 'User information').should('be.visible');
    cy.contains('p', 'Name: Test DEMO').should('be.visible');
    cy.contains('p', 'Email: test@studio.com').should('be.visible');
    cy.contains('p', 'You are admin').should('be.visible');
    cy.contains('p', 'Delete my account:').should('not.exist');
    cy.contains('button', 'Delete').should('not.exist');
  });

  it('Should user be able to delete itself when the user is authenticated as a regular user', () => {
    const userSession = {
      token: 'fake-jwt-user',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const accountResponse = {
      id: 1,
      email: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false,
      password: '',
      createdAt: '2026-01-10T10:00:00.000Z',
      updatedAt: '2026-02-10T10:00:00.000Z'
    }
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
      []).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/user/1',
      },
      {
        statusCode: 200,
        body: accountResponse
      }).as('me');

    cy.get('span.link').contains('Account').click();

    cy.intercept('DELETE', '/api/user/1', {
      statusCode: 200
    }).as('deleteUser');

    cy.contains('button', 'Delete').click();

    cy.wait('@deleteUser');

    cy.url().should('include', '/login');
    cy.contains('mat-card-title', 'Login').should('be.visible');
  });
});
