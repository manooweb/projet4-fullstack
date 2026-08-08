interface SessionFixture {
  id: number
  name: string
  description: string
  date: string
  teacher_id: number
  users: number[]
  createdAt: string
  updatedAt: string
}

const ADMIN_EMAIL = 'yoga@studio.com'

function loginAsAdmin(): void {
  cy.fixture<SessionFixture>('sessions/yoga-session.json').then((session) => {
    cy.loginAs('users/admin.json', ADMIN_EMAIL, [session])
  })
}

function interceptTeachers(): void {
  cy.intercept('GET', '/api/teacher', {
    fixture: 'teachers/list.json',
  }).as('teachers')
}

function interceptSessionDetail(): void {
  cy.fixture<SessionFixture>('sessions/yoga-session.json').then((session) => {
    cy.intercept('GET', `/api/session/${session.id}`, {
      statusCode: 200,
      body: session,
    }).as('sessionDetail')
  })
}

describe('Session form spec', () => {
  it('Should display an empty session form to create a new session', () => {
    loginAsAdmin()
    interceptTeachers()

    cy.contains('mat-card-header button', 'Create').click()

    cy.wait('@teachers')
    cy.get('h1').contains('Create session').should('be.visible')
    cy.get('mat-select[formControlName=teacher_id]').click()
    cy.get('mat-option').should('contain.text', 'Teacher Learn')
  })

  it('Should display a correctly populated session form to edit a session', () => {
    loginAsAdmin()
    interceptTeachers()
    interceptSessionDetail()

    cy.contains('mat-card-actions button', 'Edit').click()

    cy.wait('@teachers')
    cy.wait('@sessionDetail')
    cy.get('h1').contains('Update session').should('be.visible')
    cy.get('input[formControlName=name]').should('have.value', 'Yoga Session')
    cy.get('textarea[formControlName=description]').should('have.value', 'A relaxing yoga session')
    cy.get('input[formControlName=date]').should('have.value', '2026-08-05')
    cy.get('mat-select[formControlName=teacher_id]').should('contain.text', 'Teacher Learn')

    cy.intercept('PUT', '/api/session/1', {
      fixture: 'sessions/yoga-session.json',
    }).as('updateSession')

    cy.get('button[type=submit]').click()

    cy.wait('@updateSession').its('request.body').should('deep.equal', {
      name: 'Yoga Session',
      description: 'A relaxing yoga session',
      date: '2026-08-05',
      teacher_id: 1,
    })
    cy.url().should('include', '/sessions')
  })
})
