# Yoga

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 19.2.16.

## Start the project

Git clone:

> git clone https://github.com/OpenClassrooms-Student-Center/P5-Full-Stack-testing

Go inside folder:

> cd yoga

Install dependencies:

> npm install

Launch Front-end:

> npm run start;


### Test

Test files are grouped by category:

- `*.spec.ts`: unit tests;
- `*.integration.spec.ts`: Angular integration tests;
- `*.cy.ts`: Cypress end-to-end scenarios.

#### E2E

Launching e2e test:

> npm run e2e

Generate coverage report (you should launch e2e test before):

> npm run e2e:coverage

Report is available here:

> front/coverage/lcov-report/index.html

#### Front-end tests

The test file name defines its category:

- `*.spec.ts`: unit test. The tested class or component is isolated and its
  collaborators are replaced with mocks or spies.
- `*.integration.spec.ts`: integration test. The test verifies the interaction
  of several real Angular elements, such as a component with its template and
  form, routing, or a service with Angular's HTTP testing infrastructure.

Using `TestBed` alone does not make a test an integration test. The category
depends on whether collaborators are mocked or integrated into the scenario.

Run all tests:

> npm run test

Run only unit tests:

> npm run test:unit

Run only integration tests:

> npm run test:integration

for following change:

> npm run test:watch

Generate the global, unit, and integration coverage reports:

> npm run test:coverage

Reports are available here:

- `coverage-reports/front-jest/lcov-report/index.html`: combined coverage and
  integration-test ratio;
- `coverage-reports/front-jest-unit/lcov-report/index.html`: unit-test coverage;
- `coverage-reports/front-jest-integration/lcov-report/index.html`:
  integration-test coverage.

The global report enforces a minimum of 80% for statements, branches,
functions, and lines. The integration-test ratio target is 30% and is displayed
for tracking purposes without failing the test command.
