const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const frontDirectory = path.resolve(__dirname, '..');
const reportsDirectory = path.resolve(frontDirectory, '../coverage-reports');
const unitResults = path.join(reportsDirectory, '.front-jest-unit-results.json');
const integrationResults = path.join(reportsDirectory, '.front-jest-integration-results.json');
const globalResults = path.join(reportsDirectory, '.front-jest-global-results.json');
const globalIndex = path.join(reportsDirectory, 'front-jest/lcov-report/index.html');
const unitIndex = path.join(reportsDirectory, 'front-jest-unit/lcov-report/index.html');
const integrationIndex = path.join(reportsDirectory, 'front-jest-integration/lcov-report/index.html');
const ratioTarget = 30;

const ratioMarkerStart = '<!-- FRONT_TEST_RATIO_START -->';
const ratioMarkerEnd = '<!-- FRONT_TEST_RATIO_END -->';
const navigationMarkerStart = '<!-- FRONT_COVERAGE_NAVIGATION_START -->';
const navigationMarkerEnd = '<!-- FRONT_COVERAGE_NAVIGATION_END -->';

function runJest(config, resultsFile) {
  const jestExecutable = require.resolve('jest/bin/jest');
  const result = spawnSync(
    process.execPath,
    [
      jestExecutable,
      '--config',
      config,
      '--coverage',
      '--runInBand',
      '--json',
      `--outputFile=${resultsFile}`,
    ],
    { cwd: frontDirectory, stdio: 'inherit' },
  );

  return result.status ?? 1;
}

function readTestCount(resultsFile) {
  if (!fs.existsSync(resultsFile)) {
    return 0;
  }

  const results = JSON.parse(fs.readFileSync(resultsFile, 'utf8'));
  return results.numTotalTests ?? 0;
}

function relativeLink(sourceIndex, targetIndex) {
  return path.relative(path.dirname(sourceIndex), targetIndex).split(path.sep).join('/');
}

function replaceOrInsert(html, startMarker, endMarker, block, insertionToken, afterToken) {
  const existingStart = html.indexOf(startMarker);
  const existingEnd = html.indexOf(endMarker);

  if (existingStart >= 0 && existingEnd >= existingStart) {
    return html.slice(0, existingStart)
      + block
      + html.slice(existingEnd + endMarker.length);
  }

  const tokenPosition = html.indexOf(insertionToken);
  if (tokenPosition < 0) {
    throw new Error(`Unable to locate ${insertionToken} in the Jest coverage report`);
  }

  const insertionPoint = afterToken
    ? tokenPosition + insertionToken.length
    : tokenPosition;
  return html.slice(0, insertionPoint) + block + html.slice(insertionPoint);
}

function addRatio(unitCount, integrationCount) {
  const totalCount = unitCount + integrationCount;
  const ratio = totalCount === 0 ? 0 : (integrationCount / totalCount) * 100;
  const targetReached = ratio >= ratioTarget;
  const status = targetReached ? 'TARGET REACHED' : 'BELOW TARGET';
  const color = targetReached ? '#4c8c2b' : '#c2410c';
  const block = `
${ratioMarkerStart}
<div style="border: 1px solid ${color}; margin: 0 0 1em; padding: 0.75em;">
  <strong>Integration test ratio:</strong>
  ${integrationCount} / ${totalCount} tests (${ratio.toFixed(2)}%), minimum ${ratioTarget.toFixed(2)}%
  - <strong style="color: ${color};">${status}</strong>
</div>
${ratioMarkerEnd}
`;

  let html = fs.readFileSync(globalIndex, 'utf8');
  html = replaceOrInsert(html, ratioMarkerStart, ratioMarkerEnd, block, '<h1>All files</h1>', true);
  fs.writeFileSync(globalIndex, html);
}

function addNavigation(sourceIndex, links, heading = '') {
  const renderedLinks = links
    .map(({ label, target }) => `<a href="${relativeLink(sourceIndex, target)}">${label}</a>`)
    .join(' | ');
  const block = `
${navigationMarkerStart}
<div style="margin: 1em 0;">
  ${heading}${renderedLinks}
</div>
${navigationMarkerEnd}
`;

  let html = fs.readFileSync(sourceIndex, 'utf8');
  html = replaceOrInsert(html, navigationMarkerStart, navigationMarkerEnd, block, '</table>', true);
  fs.writeFileSync(sourceIndex, html);
}

function createEmptyIntegrationReport() {
  if (fs.existsSync(integrationIndex)) {
    return;
  }

  fs.mkdirSync(path.dirname(integrationIndex), { recursive: true });
  fs.writeFileSync(integrationIndex, `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Integration test coverage</title>
</head>
<body style="font-family: sans-serif; margin: 2em;">
  <h1>Integration test coverage</h1>
  <p>No integration tests found. Add files matching <code>*.integration.spec.ts</code>.</p>
  <table><tbody><tr><td>No coverage data available.</td></tr></tbody></table>
</body>
</html>
`);
}

function removeTemporaryResults() {
  for (const resultsFile of [unitResults, integrationResults, globalResults]) {
    if (fs.existsSync(resultsFile)) {
      fs.unlinkSync(resultsFile);
    }
  }
}

fs.mkdirSync(reportsDirectory, { recursive: true });

const unitStatus = runJest('jest.unit.config.js', unitResults);
const integrationStatus = runJest('jest.integration.config.js', integrationResults);
const globalStatus = runJest('jest.config.js', globalResults);

try {
  createEmptyIntegrationReport();

  if (!fs.existsSync(globalIndex) || !fs.existsSync(unitIndex)) {
    throw new Error('Jest did not generate the expected global and unit coverage reports');
  }

  const unitCount = readTestCount(unitResults);
  const integrationCount = readTestCount(integrationResults);

  addRatio(unitCount, integrationCount);
  addNavigation(globalIndex, [
    { label: 'Unit tests', target: unitIndex },
    { label: 'Integration tests', target: integrationIndex },
  ], '<strong>Detailed coverage reports:</strong> ');
  addNavigation(unitIndex, [
    { label: 'Back to global coverage', target: globalIndex },
  ]);
  addNavigation(integrationIndex, [
    { label: 'Back to global coverage', target: globalIndex },
  ]);
} finally {
  removeTemporaryResults();
}

process.exitCode = unitStatus || integrationStatus || globalStatus;
