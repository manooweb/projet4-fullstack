import {
  type SessionResponse,
  type TeacherResponse,
  summerYogaSession,
  teacher,
  yogaSession,
} from '../support/factories/session-detail'

const MEMBER_ID = 2
const MEMBER_EMAIL = 'member@studio.com'
const ADMIN_EMAIL = 'yoga@studio.com'
type SessionsState = SessionResponse[] | (() => SessionResponse[])

function loginAsMember(sessions: SessionResponse[]): void {
  cy.loginAs('users/member.json', MEMBER_EMAIL, sessions)
}

function loginAsAdmin(sessions: SessionsState): void {
  cy.loginAs('users/admin.json', ADMIN_EMAIL, sessions)
}

function openSessionDetail(session: SessionResponse, sessionTeacher: TeacherResponse): void {
  cy.intercept('GET', `/api/teacher/${sessionTeacher.id}`, {
    statusCode: 200,
    body: sessionTeacher,
  }).as('teacherDetail')
  cy.intercept('GET', `/api/session/${session.id}`, {
    statusCode: 200,
    body: session,
  }).as('sessionDetail')

  cy.contains('button', 'Detail').click()
  cy.wait('@teacherDetail')
  cy.wait('@sessionDetail')
  cy.url().should('include', `/sessions/detail/${session.id}`)
}

describe('Session detail spec', () => {
  it('Should display Participate button when authenticated user is not a participant', () => {
    const session = yogaSession()
    loginAsMember([session])
    openSessionDetail(session, teacher)

    cy.contains('mat-card-title', session.name).should('be.visible')
    cy.contains('button', 'Participate').should('be.visible')
    cy.contains('mat-card-subtitle', `${teacher.firstName} ${teacher.lastName.toUpperCase()}`).should('be.visible')
    cy.contains('div.description p', 'Description:').should('be.visible')
    cy.contains('div.description', session.description).should('be.visible')
  })

  it('Should display Do not participate button when authenticated user clicks Participate', () => {
    const session = yogaSession()
    let isParticipating = false
    loginAsMember([session])
    openSessionDetail(session, teacher)

    cy.intercept('GET', `/api/session/${session.id}`, (request) => {
      request.reply({
        statusCode: 200,
        body: yogaSession(isParticipating ? [MEMBER_ID] : []),
      })
    }).as('sessionAfterParticipation')
    cy.intercept('POST', `/api/session/${session.id}/participate/${MEMBER_ID}`, (request) => {
      isParticipating = true
      request.reply({ statusCode: 200 })
    }).as('participate')

    cy.contains('button', 'Participate').click()

    cy.wait('@participate').its('request').should((request) => {
      expect(request.method).to.equal('POST')
      expect(request.url).to.include(`/api/session/${session.id}/participate/${MEMBER_ID}`)
    })
    cy.wait('@sessionAfterParticipation')
    cy.contains('button', 'Do not participate').should('be.visible')
  })

  it('Should display Do not participate button when authenticated user is already a participant', () => {
    const session = yogaSession([MEMBER_ID])
    loginAsMember([session])
    openSessionDetail(session, teacher)

    cy.contains('mat-card-title', session.name).should('be.visible')
    cy.contains('button', 'Do not participate').should('be.visible')
    cy.contains('mat-card-subtitle', `${teacher.firstName} ${teacher.lastName.toUpperCase()}`).should('be.visible')
    cy.contains('div.description p', 'Description:').should('be.visible')
    cy.contains('div.description', session.description).should('be.visible')
  })

  it('Should display Participate button when authenticated user clicks Do not participate', () => {
    const session = yogaSession([MEMBER_ID])
    let isParticipating = true
    loginAsMember([session])
    openSessionDetail(session, teacher)

    cy.intercept('GET', `/api/session/${session.id}`, (request) => {
      request.reply({
        statusCode: 200,
        body: yogaSession(isParticipating ? [MEMBER_ID] : []),
      })
    }).as('sessionAfterParticipation')
    cy.intercept('DELETE', `/api/session/${session.id}/participate/${MEMBER_ID}`, (request) => {
      isParticipating = false
      request.reply({ statusCode: 200 })
    }).as('stopParticipating')

    cy.contains('button', 'Do not participate').click()

    cy.wait('@stopParticipating').its('request').should((request) => {
      expect(request.method).to.equal('DELETE')
      expect(request.url).to.include(`/api/session/${session.id}/participate/${MEMBER_ID}`)
    })
    cy.wait('@sessionAfterParticipation')
    cy.contains('button', 'Participate').should('be.visible')
  })

  it('Should display delete button when user is authenticated as an admin', () => {
    const session = yogaSession()
    loginAsAdmin([session])
    openSessionDetail(session, teacher)

    cy.contains('mat-card-title', session.name).should('be.visible')
    cy.contains('button', 'Participate').should('not.exist')
    cy.contains('button', 'Delete').should('be.visible')
    cy.contains('mat-card-subtitle', `${teacher.firstName} ${teacher.lastName.toUpperCase()}`).should('be.visible')
    cy.contains('div.description p', 'Description:').should('be.visible')
    cy.contains('div.description', session.description).should('be.visible')
  })

  it('Should delete the session when an admin clicks Delete', () => {
    const deletedSession = yogaSession()
    const remainingSession = summerYogaSession()
    let sessions = [deletedSession, remainingSession]
    loginAsAdmin(() => sessions)
    openSessionDetail(deletedSession, teacher)

    cy.intercept('DELETE', `/api/session/${deletedSession.id}`, (request) => {
      sessions = [remainingSession]
      request.reply({ statusCode: 200 })
    }).as('deleteSession')

    cy.contains('button', 'Delete').click()

    cy.wait('@deleteSession').its('request.url').should('include', `/api/session/${deletedSession.id}`)
    cy.wait('@sessions').its('response.body').should('deep.equal', [remainingSession])
    cy.location('pathname').should('eq', '/sessions')
    cy.contains('mat-card-title', new RegExp(`^${deletedSession.name}$`)).should('not.exist')
    cy.contains('mat-card-title', new RegExp(`^${remainingSession.name}$`)).should('be.visible')
  })
})
