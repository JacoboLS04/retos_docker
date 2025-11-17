import { Given, When, Then, Before, After } from '@cucumber/cucumber';
import assert from 'assert';
import { startHealthServer, setReady, setAlive, getBootTimeIso } from '../../src/health.js';
import http from 'http';

let server: any;
let healthResponse: any;
let statusCode: number;

Before(async function() {
  // Start health server on random port for testing
  server = startHealthServer({ port: 0 });
});

After(function() {
  if (server) {
    server.close();
  }
});

function makeRequest(path: string): Promise<{ status: number, body: any }> {
  return new Promise((resolve, reject) => {
    const address = server.address();
    const options = {
      hostname: 'localhost',
      port: address.port,
      path: path,
      method: 'GET',
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => {
        resolve({
          status: res.statusCode || 500,
          body: JSON.parse(data),
        });
      });
    });

    req.on('error', reject);
    req.end();
  });
}

Given('the notification orchestrator is ready', function() {
  setReady(true);
});

Given('the notification orchestrator is alive', function() {
  setAlive(true);
});

Given('the notification orchestrator is not ready', function() {
  setReady(false);
});

When('I request the health status', async function() {
  const response = await makeRequest('/health');
  statusCode = response.status;
  healthResponse = response.body;
});

When('I check the readiness endpoint', async function() {
  const response = await makeRequest('/health/ready');
  statusCode = response.status;
  healthResponse = response.body;
});

When('I check the liveness endpoint', async function() {
  const response = await makeRequest('/health/live');
  statusCode = response.status;
  healthResponse = response.body;
});

Then('the health status should be {string}', function(expectedStatus: string) {
  assert.strictEqual(healthResponse.status, expectedStatus);
});

Then('the readiness check should be {string}', function(expectedStatus: string) {
  const readyCheck = healthResponse.checks.find((c: any) => c.name === 'Readiness check');
  assert.strictEqual(readyCheck.status, expectedStatus);
});

Then('the liveness check should be {string}', function(expectedStatus: string) {
  const liveCheck = healthResponse.checks.find((c: any) => c.name === 'Liveness check');
  assert.strictEqual(liveCheck.status, expectedStatus);
});

Then('the readiness status should be {int}', function(expectedCode: number) {
  assert.strictEqual(statusCode, expectedCode);
});

Then('the readiness should indicate {string}', function(expectedValue: string) {
  assert.strictEqual(healthResponse.data.status, expectedValue);
});

Then('the liveness status should be {int}', function(expectedCode: number) {
  assert.strictEqual(statusCode, expectedCode);
});

Then('the liveness should indicate {string}', function(expectedValue: string) {
  assert.strictEqual(healthResponse.data.status, expectedValue);
});
