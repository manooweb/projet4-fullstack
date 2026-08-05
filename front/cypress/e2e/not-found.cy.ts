describe('Page not found spec', () => {
  it('Page not found', () => {
    cy.visit('/wrong-url')

    cy.url().should('include', '/404')

    cy.contains('h1', 'Page not found !').should('be.visible');
  })
});