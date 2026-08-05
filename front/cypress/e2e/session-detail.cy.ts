describe('Session detail spec', () => {
  it('Session detail successfull', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const participantResponse = { 
      id: 1, 
      lastName: "Demo",
      firstName: "Test",
    };

    const teacherResponse = { 
      id: 1, 
      lastName: "Learn", 
      firstName: "Teacher", 
    };

    const sessionResponse = { 
      id: 1, 
      name: "Yoga Session", 
      description: "A relaxing yoga session", 
      date: "2026-08-05T15:49:18", 
      teacher_id: 1, 
      users: [],
      createdAt: "2026-08-05T15:49:18", 
      updatedAt: "2026-08-05T15:49:18"
    };

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
        body: [
        sessionResponse
      ]
      }).as('sessions');


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
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teacherResponse
      }).as('me');

      cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('me');

    cy.get('button').contains('Detail').click();

    cy.url().should('include', '/sessions/detail/1');
    cy.contains('mat-card-title', sessionResponse.name).should('be.visible');
    cy.contains('mat-card-subtitle', `${teacherResponse.firstName} ${teacherResponse.lastName.toUpperCase()}`).should('be.visible');
    cy.contains('div.description p', 'Description:').should('be.visible');
    cy.contains('div.description', sessionResponse.description).should('be.visible');
  });
});