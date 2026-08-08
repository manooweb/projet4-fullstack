const LOGIN_EMAIL = 'yoga@studio.com'
const VALID_PASSWORD = 'test!1234'

function fillLoginForm(password: string): void {
  cy.get('input[formControlName=email]').clear().type(LOGIN_EMAIL)
  cy.get('input[formControlName=password]').clear().type(password)
}

describe('Login spec', () => {
  it('Login successfull', () => {
    cy.intercept('POST', '/api/auth/login', {
      fixture: 'users/member.json',
    }).as('login')
    cy.intercept('GET', '/api/session', []).as('sessions')

    cy.visit('/login')
    fillLoginForm(VALID_PASSWORD)

    cy.get('button[type=submit]').click()

    cy.wait('@login')
    cy.wait('@sessions')
    cy.url().should('include', '/sessions')
  });

  it('Login fails', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: {
        message: 'Invalid credentials'
      }
    }).as('login')

    cy.visit('/login')
    fillLoginForm('wrongpassword')

    cy.get('button[type=submit]').click()

    cy.wait('@login')
      .its('response')
      .should( response => {
        expect(response?.statusCode).to.equal(401)
        expect(response?.body).to.have.property('message', 'Invalid credentials')
      })

    cy.url().should('include', '/login')
    cy.contains('p.error', 'An error occurred').should('be.visible')
  });

  it('Should toggle password visibility when the toggle button is clicked once', () => {
    cy.visit('/login')
    fillLoginForm(VALID_PASSWORD)

    cy.get('button[mat-icon-button]').click()
    cy.get('input[formControlName=password]').should('have.attr', 'type', 'text')
  });
});
