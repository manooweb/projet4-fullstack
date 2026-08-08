interface RegistrationData {
  firstName: string
  lastName: string
  email: string
  password: string
}

const NEW_MEMBER: RegistrationData = {
  firstName: 'FirstName',
  lastName: 'LastName',
  email: 'new.member@studio.com',
  password: 'test!1234',
}

function fillRegistrationForm(registration: RegistrationData): void {
  cy.get('input[formControlName=firstName]').type(registration.firstName)
  cy.get('input[formControlName=lastName]').type(registration.lastName)
  cy.get('input[formControlName=email]').type(registration.email)
  cy.get('input[formControlName=password]').type(registration.password)
}

describe('Register spec', () => {
  it('Register successful', () => {
    cy.intercept('POST', '/api/auth/register', {
      statusCode: 200,
    }).as('register')

    cy.visit('/register')
    fillRegistrationForm(NEW_MEMBER)
    cy.contains('button', 'Submit').click()

    cy.wait('@register').its('request.body').should('deep.equal', NEW_MEMBER)
    cy.url().should('include', '/login')
    cy.contains('mat-card-title', 'Login').should('be.visible')
  })

  it('Register fails when the email is already taken', () => {
    const existingMember: RegistrationData = {
      ...NEW_MEMBER,
      email: 'existing.member@studio.com',
    }

    cy.intercept('POST', '/api/auth/register', {
      statusCode: 400,
      body: {
        message: 'Error: Email is already taken!',
      },
    }).as('register')

    cy.visit('/register')
    fillRegistrationForm(existingMember)
    cy.contains('button', 'Submit').click()

    cy.wait('@register')
      .its('response')
      .should((response) => {
        expect(response?.statusCode).to.equal(400)
        expect(response?.body).to.have.property('message', 'Error: Email is already taken!')
      })

    cy.url().should('include', '/register')
    cy.contains('p.error', 'An error occurred').should('be.visible')
  })
})
