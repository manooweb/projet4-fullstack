describe('Logout spec', () => {
  it('Logout successfull', () => {
    cy.loginAs('users/member.json', 'member@studio.com')

    cy.contains('span.link', 'Logout').click()

    cy.url().should('include', '/login')
    cy.contains('a.link', 'Login').should('be.visible')
    cy.contains('a.link', 'Register').should('be.visible')
    cy.contains('span.link', 'Logout').should('not.exist')
  })
})
