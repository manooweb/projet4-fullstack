describe('Logout spec', () => {
  it('Logout successfull', () => {
    cy.visit('/login');
    
    cy.intercept('POST', '/api/auth/login', {
      body: {
        id: 1,
        username: 'userName',
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
    
    cy.get('input[formControlName=email]').clear();
    cy.get('input[formControlName=password]').clear();
    cy.get('input[formControlName=email]').type("yoga@studio.com")
    cy.get('input[formControlName=password]').type(`${"test!1234"}`)

    cy.get('button[type=submit]').click();
    
    cy.url().should('include', '/sessions');
    
    cy.contains('span.link', 'Logout').click();

    cy.url().should('include', '/login');
    cy.contains('a.link', 'Login').should('be.visible');
    cy.contains('a.link', 'Register').should('be.visible');
    cy.contains('span.link', 'Logout').should('not.exist');
  })
});
