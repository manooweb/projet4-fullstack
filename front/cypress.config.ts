import { defineConfig } from 'cypress'

export default defineConfig({
  videosFolder: 'cypress/videos',
  screenshotsFolder: 'cypress/screenshots',
  fixturesFolder: 'cypress/fixtures',
  video: false,
  env: {
    codeCoverage: {
      exclude: ['src/main.ts'],
    },
  },
  e2e: {
    setupNodeEvents(on, config) {
      const registerCodeCoverageTasks = require('@cypress/code-coverage/task')

      return registerCodeCoverageTasks(on, config)
    },
    baseUrl: 'http://localhost:4200',
  },
})
