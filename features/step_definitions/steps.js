const { Given, When, Then, Before, After, setDefaultTimeout } = require('@cucumber/cucumber');
const assert = require('assert');
const request = require('supertest');
const nock = require('nock');

// Increase timeout for steps that may take longer (server startup, etc.)
setDefaultTimeout(10000);

let app;
let server;
let response;
let testUserId;

// Configure test environment
Before(function () {
  process.env.NODE_ENV = 'test';
  process.env.AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://auth-service:8080';
  process.env.PROFILE_SERVICE_URL = process.env.PROFILE_SERVICE_URL || 'http://profile-service:8081';
  
  // Clean previous mocks
  nock.cleanAll();
  nock.enableNetConnect('127.0.0.1');
  
  // Load the app
  const appModule = require('../../src/index');
  app = appModule.app;
});

After(async function () {
  nock.cleanAll();
  if (server && server.close) {
    await new Promise(resolve => server.close(resolve));
  }
});

// ========== STEPS FOR SERVICE HEALTH ==========

Given('the API Gateway service is running', async function () {
  // Server is loaded in Before, here we just verify that app exists
  assert.ok(app, 'App must be defined');
});

When('I check the service health status', async function () {
  response = await request(app).get('/health');
});

Then('the service should respond that it is functioning correctly', function () {
  assert.strictEqual(response.status, 200, 'Status must be 200');
});

Then('the status should be {string}', function (expectedStatus) {
  assert.ok(response.body.status, 'Body must have status property');
  assert.strictEqual(response.body.status, expectedStatus, `Status must be ${expectedStatus}`);
});

// ========== STEPS FOR USER MANAGEMENT ==========

Given('a user with identifier {string} exists in the system', function (userId) {
  testUserId = userId;
  
  // Mock auth service
  nock('http://auth-service:8080')
    .get(`/auth/users/${userId}`)
    .reply(200, {
      id: userId,
      username: 'test_user',
      email: 'user@example.com'
    });
  
  // Mock profile service
  nock('http://profile-service:8081')
    .get(`/profiles/${userId}`)
    .reply(200, {
      bio: 'Test user',
      location: 'Colombia'
    });
  
  // Mock for profile update
  nock('http://profile-service:8081')
    .put(`/profiles/${userId}`)
    .reply(200, (uri, requestBody) => {
      return { id: userId, ...requestBody };
    });
  
  // Mock for deletion
  nock('http://auth-service:8080')
    .delete(`/auth/users/${userId}`)
    .reply(200, { message: 'User deleted from auth' });
    
  nock('http://profile-service:8081')
    .delete(`/profiles/${userId}`)
    .reply(200, { message: 'Profile deleted' });
});

When('I request to view that user\'s complete information', async function () {
  response = await request(app).get(`/api/users/${testUserId}`);
});

Then('I should receive their personal data', function () {
  assert.strictEqual(response.status, 200, 'Status must be 200');
  assert.ok(response.body.id, 'Must have id');
  assert.strictEqual(response.body.id, testUserId, 'Id must match');
  assert.ok(response.body.username, 'Must have username');
  assert.ok(response.body.email, 'Must have email');
});

Then('I should receive their profile information', function () {
  assert.ok(response.body.profile, 'Must have profile property');
  assert.ok(response.body.profile.bio, 'Profile must have bio');
});

When('I update their biography to {string}', async function (newBiography) {
  response = await request(app)
    .put(`/api/users/${testUserId}`)
    .send({ profile: { bio: newBiography } });
});

Then('the information should be saved correctly', function () {
  assert.strictEqual(response.status, 200, 'Status must be 200');
});

Then('the user\'s biography should be {string}', function (expectedBio) {
  assert.ok(response.body.profile, 'Must have profile property');
  assert.strictEqual(response.body.profile.bio, expectedBio, `Bio must be ${expectedBio}`);
});

When('I request to delete that user from the system', async function () {
  response = await request(app).delete(`/api/users/${testUserId}`);
});

Then('the user should be deleted successfully', function () {
  assert.strictEqual(response.status, 200, 'Status must be 200');
  assert.ok(response.body.message, 'Must have message');
});

Then('the user\'s deletion should be notified', function () {
  // In test mode the event is not really published, but the endpoint must respond OK
  assert.ok(response.body.message.match(/deleted successfully/i), 'Message must confirm deletion');
});

// ========== STEPS FOR AUTHENTICATION ==========

Given('I want to create a new account', function () {
  // Prepare mock for registration
  nock('http://auth-service:8080')
    .post('/register')
    .reply(201, {
      id: 'new-user-1',
      username: 'juan_perez'
    });
});

When('I provide my username {string} and password', async function (username) {
  response = await request(app)
    .post('/api/auth/register')
    .send({ username, password: 'password123' });
});

Then('my account should be created successfully', function () {
  assert.strictEqual(response.status, 201, 'Status must be 201');
});

Then('I should receive my user identifier', function () {
  assert.ok(response.body.id, 'Must have id');
});

Given('I have a registered account with username {string}', function (username) {
  // Prepare mock for login
  nock('http://auth-service:8080')
    .post('/login')
    .reply(200, {
      token: 'jwt-token-abc123',
      userId: 'user-login-1',
      username
    });
});

When('I log in with my username and correct password', async function () {
  response = await request(app)
    .post('/api/auth/login')
    .send({ username: 'maria_garcia', password: 'password456' });
});

Then('I should receive an access token', function () {
  assert.strictEqual(response.status, 200, 'Status must be 200');
  assert.ok(response.body.token, 'Must have token');
});

Then('the system should confirm my identity', function () {
  assert.ok(response.body.userId, 'Must have userId');
});
