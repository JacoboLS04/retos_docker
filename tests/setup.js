const nock = require('nock');

// Ensure tests run with test env
process.env.NODE_ENV = process.env.NODE_ENV || 'test';

beforeEach(() => {
  // Clean previous interceptors
  nock.cleanAll();
  // Allow supertest to connect to local server
  nock.enableNetConnect('127.0.0.1');
});

afterEach(() => {
  nock.cleanAll();
});

afterAll(() => {
  nock.restore();
});
