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
});