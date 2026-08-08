const MEMBER_ID = 2
const ADMIN_ID = 1

function openAccount(userId: number, accountFixture: string): void {
  cy.intercept('GET', `/api/user/${userId}`, {
    fixture: accountFixture,
  }).as('me')

  cy.contains('span.link', 'Account').click()
  cy.wait('@me')
  cy.url().should('include', '/me')
}

describe('Account spec', () => {
  it('Should display user information correctly when the user is authenticated as a regular user', () => {
    cy.loginAs('users/member.json', 'member@studio.com')
    openAccount(MEMBER_ID, 'accounts/member.json')

    cy.contains('h1', 'User information').should('be.visible')
    cy.contains('p', 'Name: Member YOGA').should('be.visible')
    cy.contains('p', 'Email: member@studio.com').should('be.visible')
    cy.contains('p', 'Delete my account:').should('be.visible')
    cy.contains('button', 'Delete').should('be.visible')
  })

  it('Should not display delete button when the user is authenticated as an admin user', () => {
    cy.loginAs('users/admin.json', 'yoga@studio.com')
    openAccount(ADMIN_ID, 'accounts/admin.json')

    cy.contains('h1', 'User information').should('be.visible')
    cy.contains('p', 'Name: Yoga STUDIO').should('be.visible')
    cy.contains('p', 'Email: yoga@studio.com').should('be.visible')
    cy.contains('p', 'You are admin').should('be.visible')
    cy.contains('p', 'Delete my account:').should('not.exist')
    cy.contains('button', 'Delete').should('not.exist')
  })

  it('Should user be able to delete itself when the user is authenticated as a regular user', () => {
    cy.loginAs('users/member.json', 'member@studio.com')
    openAccount(MEMBER_ID, 'accounts/member.json')
    cy.intercept('DELETE', `/api/user/${MEMBER_ID}`, {
      statusCode: 200,
    }).as('deleteUser')

    cy.contains('button', 'Delete').click()

    cy.wait('@deleteUser')
    cy.url().should('include', '/login')
    cy.contains('mat-card-title', 'Login').should('be.visible')
  })

  it('Should return back to sessions list when clicking the back button on account page', () => {
    cy.loginAs('users/member.json', 'member@studio.com')
    openAccount(MEMBER_ID, 'accounts/member.json')

    cy.contains('button mat-icon', 'arrow_back').click()

    cy.url().should('include', '/sessions')
    cy.contains('mat-card-title', 'Sessions available').should('be.visible')
  })
})
