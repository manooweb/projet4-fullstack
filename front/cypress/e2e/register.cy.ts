describe('Register spec', () => {
  it('Register successful', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200
    }).as('register');

    cy.visit('/register');

    cy.get('input[formControlName=firstName]').type("firstName");
    cy.get('input[formControlName=lastName]').type("lastName");
    cy.get('input[formControlName=email]').type("test@studio.com");
    cy.get('input[formControlName=password]').type(`${"test!1234"}`);

    cy.contains('button', 'Submit').click();

    cy.wait('@register').its('request.body').should('deep.equal', {
      firstName: 'firstName',
      lastName: 'lastName',
      email: 'test@studio.com',
      password: 'test!1234'
    });

    cy.url().should('include', '/login');
    cy.contains('mat-card-title', 'Login').should('be.visible');
  });

  it('Register fails', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 400,
      body: {
        message: 'Error: Email is already taken!'
      }
    }).as('register');

    cy.visit('/register');

    cy.get('input[formControlName=firstName]').type("firstName");
    cy.get('input[formControlName=lastName]').type("lastName");
    cy.get('input[formControlName=email]').type("existing.email@studio.com");
    cy.get('input[formControlName=password]').type(`${"test!1234"}`);

    cy.contains('button', 'Submit').click();

    cy.wait('@register').its('response')
      .should( response => {
        expect(response).to.have.property('statusCode', 400);
        expect(response.body).to.have.property('message', 'Error: Email is already taken!');
      });

    cy.url().should('include', '/register');
    cy.contains('p.error', 'An error occurred').should('be.visible');
  });
});
