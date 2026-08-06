describe('Login spec', () => {
  it('Login successfull', () => {
    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        id: 1,
        username: 'yoga@studio.com',
        firstName: 'firstName',
        lastName: 'lastName',
        admin: true
      },
    });

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      []);

    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.url().should('include', '/sessions');
  });

  it('Login fails', () => {
    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: {
        message: 'Invalid credentials'
      }
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      []);


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"wrongpassword"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login')
      .its('response')
      .should('have.property', 'statusCode', 401);

    cy.url().should('include', '/login');
    cy.contains('p.error', 'An error occurred').should('be.visible');
  });

  it('Should toggle password visibility when the toggle button is clicked once', () => {
    cy.visit('/login');

    cy.get('input[formControlName=email]')
      .clear()
      .type('yoga@studio.com');

    cy.get('input[formControlName=password]')
      .clear()
      .type('test!1234');

    cy.get('button[mat-icon-button]').click();
    cy.get('input[formControlName=password]').should('have.attr', 'type', 'text');
  });
});
