describe('Auth Guard spec', () => {
  it('Should redirect to login if not authenticated', () => {
    cy.visit('/me')

    cy.url().should('include', '/login')

    cy.visit('/sessions')

    cy.url().should('include', '/login')
  })
  
  it('Should not redirect if not authenticated', () => {
    cy.visit('/register')

    cy.url().should('include', '/register')

    cy.visit('/login')

    cy.url().should('include', '/login')
  })
});
