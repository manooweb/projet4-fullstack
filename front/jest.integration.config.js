const baseConfig = require('./jest.config');

module.exports = {
  ...baseConfig,
  coverageDirectory: '../coverage-reports/front-jest-integration',
  coverageThreshold: undefined,
  passWithNoTests: true,
  testMatch: ['**/*.integration.spec.ts'],
};
