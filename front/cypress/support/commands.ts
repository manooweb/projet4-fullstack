declare namespace Cypress {
  interface Chainable {
    loginAs(userFixture: string, email: string): Chainable<void>
  }
}

const LOGIN_PASSWORD = 'test!1234'

Cypress.Commands.add('loginAs', (userFixture: string, email: string) => {
  cy.intercept('POST', '/api/auth/login', {
    fixture: userFixture,
  }).as('login')
  cy.intercept('GET', '/api/session', []).as('sessions')

  cy.visit('/login')
  cy.get('input[formControlName=email]').clear().type(email)
  cy.get('input[formControlName=password]').clear().type(LOGIN_PASSWORD)
  cy.get('button[type=submit]').click()

  cy.wait('@login')
  cy.wait('@sessions')
  cy.url().should('include', '/sessions')
})
