import { Session } from '../../src/app/core/models/session.interface';

describe('Session detail spec', () => {
  it('Should display Participate button when authenticated user is not a participant', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.url().should('include', '/sessions');
    cy.contains('span.link', 'Account').should('be.visible');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teacherResponse
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    cy.url().should('include', '/sessions/detail/1');
    cy.contains('mat-card-title', sessionResponse[0].name).should('be.visible');
    cy.contains('button', 'Participate').should('be.visible');
    cy.contains('mat-card-subtitle', `${teacherResponse.firstName} ${teacherResponse.lastName.toUpperCase()}`).should('be.visible');
    cy.contains('div.description p', 'Description:').should('be.visible');
    cy.contains('div.description', sessionResponse[0].description).should('be.visible');
  });

  it('Should display Do not participate button when authenticated user click on Participate button', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionsResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [] as number[],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionsResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teacherResponse
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionsResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    let isParticipate = sessionsResponse[0].users.some(u => u === userSession.id);
    expect(isParticipate).to.be.false;

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      request => {
        request.reply({
          statusCode: 200,
          body: {
            ...sessionsResponse[0],
            users: isParticipate ? [userSession.id] : []
          }
        });
      }).as('sessionAfterParticipateDetail');

    cy.intercept(
      {
        method: 'POST',
        url: '/api/session/1/participate/1',
      },
      request => {
        isParticipate = true;

        request.reply({
          statusCode: 200,
        });
      }).as('sessionParticipate');

    cy.get('button').contains('Participate').click();

    cy.wait('@sessionParticipate')
      .its('request')
      .should(request => {
        expect(request.method).to.equal('POST');
        expect(request.url).to.include('/api/session/1/participate/1');
      });
    cy.wait('@sessionAfterParticipateDetail');

    cy.get('button').contains('Do not participate').should('be.visible');
  });

  it('Should display Do not participate button when authenticated user is a participant', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [1],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.url().should('include', '/sessions');
    cy.contains('span.link', 'Account').should('be.visible');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teacherResponse
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    cy.url().should('include', '/sessions/detail/1');
    cy.contains('mat-card-title', sessionResponse[0].name).should('be.visible');
    cy.contains('button', 'Do not participate').should('be.visible');
    cy.contains('mat-card-subtitle', `${teacherResponse.firstName} ${teacherResponse.lastName.toUpperCase()}`).should('be.visible');
    cy.contains('div.description p', 'Description:').should('be.visible');
    cy.contains('div.description', sessionResponse[0].description).should('be.visible');
  });

  it('Should display Participate button when authenticated user click on Do not participate button', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: false
    };

    const teacherResponse = {
      id: 1,
      lastName: "Learn",
      firstName: "Teacher",
    };

    const sessionsResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [1] as number[],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionsResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teacherResponse
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionsResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    let isParticipate = sessionsResponse[0].users.some(u => u === userSession.id);
    expect(isParticipate).to.be.true;

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      request => {
        request.reply({
          statusCode: 200,
          body: {
            ...sessionsResponse[0],
            users: isParticipate ? [userSession.id] : []
          }
        });
      }).as('sessionAfterParticipateDetail');

    cy.intercept(
      {
        method: 'DELETE',
        url: '/api/session/1/participate/1',
      },
      request => {
        isParticipate = false;

        request.reply({
          statusCode: 200,
        });
      }).as('sessionUnParticipate');

    cy.get('button').contains('Do not participate').click();

    cy.wait('@sessionUnParticipate')
      .its('request')
      .should(request => {
        expect(request.method).to.equal('DELETE');
        expect(request.url).to.include('/api/session/1/participate/1');
      });
    cy.wait('@sessionAfterParticipateDetail');

    cy.get('button').contains('Participate').should('be.visible');
  });

  it('Should display delete button when user is authenticated as an admin', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true
    };

    const teachersResponse = [
      {
        id: 1,
        lastName: "Learn",
        firstName: "Teacher",
      }];

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse
      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.url().should('include', '/sessions');
    cy.contains('span.link', 'Account').should('be.visible');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teachersResponse[0]
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    cy.url().should('include', '/sessions/detail/1');
    cy.contains('mat-card-title', sessionResponse[0].name).should('be.visible');
    cy.contains('button', 'Participate').should('not.exist');
    cy.contains('button', 'Delete').should('be.visible');
    cy.contains('mat-card-subtitle', `${teachersResponse[0].firstName} ${teachersResponse[0].lastName.toUpperCase()}`).should('be.visible');
    cy.contains('div.description p', 'Description:').should('be.visible');
    cy.contains('div.description', sessionResponse[0].description).should('be.visible');
  });

  it('Should delete the session when user is authenticated as an admin click on the delete button', () => {
    const userSession = {
      token: 'fake-jwt-admin',
      type: 'Bearer',
      id: 1,
      username: 'test@studio.com',
      firstName: 'Test',
      lastName: 'Demo',
      admin: true
    };

    const teachersResponse = [
      {
        id: 1,
        lastName: "Learn",
        firstName: "Teacher",
      },
      {
        id: 2,
        lastName: "Learn",
        firstName: "Teacher",
      }
    ];

    const sessionResponse = [{
      id: 1,
      name: "Yoga Session",
      description: "A relaxing yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 1,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    },
    {
      id: 2,
      name: "Summer Yoga Session",
      description: "A relaxing summer yoga session",
      date: "2026-08-05T15:49:18",
      teacher_id: 2,
      users: [],
      createdAt: "2026-08-05T15:49:18",
      updatedAt: "2026-08-05T15:49:18"
    }];

    cy.visit('/login');

    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: userSession,
    }).as('login');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session',
      },
      {
        statusCode: 200,
        body: sessionResponse

      }).as('sessions');


    cy.get('input[formControlName=email]').clear().type("yoga@studio.com");
    cy.get('input[formControlName=password]').clear().type(`${"test!1234"}`);

    cy.get('button[type=submit]').click();

    cy.wait('@login');
    cy.wait('@sessions');

    cy.url().should('include', '/sessions');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/1',
      },
      {
        statusCode: 200,
        body: teachersResponse[0]
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/teacher/2',
      },
      {
        statusCode: 200,
        body: teachersResponse[1]
      }).as('teacherDetail');

    cy.intercept(
      {
        method: 'GET',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
        body: sessionResponse[0]
      }).as('sessionDetail');

    cy.get('button').contains('Detail').click();

    cy.intercept(
      {
        method: 'DELETE',
        url: '/api/session/1',
      },
      {
        statusCode: 200,
      }).as('sessionDelete');

    const sessionsAfterDelete = sessionResponse.filter(
      session => session.id !== 1
    );

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: sessionsAfterDelete
    }).as('sessionsAfterDelete');

    cy.get('button').contains('Delete').click();

    cy.wait('@sessionDelete');
    cy.wait('@sessionsAfterDelete');

    cy.url().should('include', '/sessions');
    cy.contains('mat-card-title', sessionResponse[0].name).should('be.visible');
  });
});
