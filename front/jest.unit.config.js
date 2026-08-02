const baseConfig = require('./jest.config');

module.exports = {
  ...baseConfig,
  coverageDirectory: '../coverage-reports/front-jest-unit',
  coverageThreshold: undefined,
  testPathIgnorePatterns: [
    ...baseConfig.testPathIgnorePatterns,
    '\\.integration\\.spec\\.ts$',
  ],
};
