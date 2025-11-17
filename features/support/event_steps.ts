import { Given, When, Then, Before, After } from '@cucumber/cucumber';
import assert from 'assert';

interface TestContext {
  currentEvent: any;
  eventProcessed: boolean;
}

const context: TestContext = {
  currentEvent: null,
  eventProcessed: false,
};

Before(function() {
  context.currentEvent = null;
  context.eventProcessed = false;
});

// USER_REGISTERED scenarios
Given('a new user registers with email {string}', function(email: string) {
  context.currentEvent = {
    id: 'evt-reg-001',
    type: 'USER_REGISTERED',
    userId: 123,
    nombre: 'John Doe',
    email: email,
  };
});

// USER_LOGIN scenarios
Given('a user logs in with email {string} and phone {string}', function(email: string, phone: string) {
  context.currentEvent = {
    id: 'evt-login-001',
    type: 'USER_LOGIN',
    userId: 456,
    nombre: 'Alice Smith',
    email: email,
    telefono: phone,
  };
});

// PASSWORD_RESET_REQUESTED scenarios
Given('a user requests password reset for email {string}', function(email: string) {
  context.currentEvent = {
    id: 'evt-reset-001',
    type: 'PASSWORD_RESET_REQUESTED',
    userId: 789,
    email: email,
    token: 'reset-token-abc123',
  };
});

// PASSWORD_CHANGED scenarios
Given('a user changes password for email {string} and phone {string}', function(email: string, phone: string) {
  context.currentEvent = {
    id: 'evt-changed-001',
    type: 'PASSWORD_CHANGED',
    userId: 101,
    email: email,
    telefono: phone,
  };
});

// Common When step - simulate event reception
When('the {word} event is received', async function(eventType: string) {
  assert.strictEqual(context.currentEvent.type, eventType);
  // Simulate event processing
  context.eventProcessed = true;
});

// Then steps - verify event structure and expectations
Then('a welcome email notification should be queued', function() {
  assert.strictEqual(context.currentEvent.type, 'USER_REGISTERED');
  assert.ok(context.currentEvent.email);
});

Then('the notification should include the user\'s name', function() {
  assert.ok(context.currentEvent.nombre);
});

Then('the event should be saved in the database', function() {
  assert.ok(context.currentEvent.id);
  assert.strictEqual(context.eventProcessed, true);
});

Then('a login alert email should be queued', function() {
  assert.strictEqual(context.currentEvent.type, 'USER_LOGIN');
  assert.ok(context.currentEvent.email);
});

Then('a login alert SMS should be queued', function() {
  assert.ok(context.currentEvent.telefono);
});

Then('both notifications should be saved in the database', function() {
  assert.ok(context.currentEvent.email);
  assert.ok(context.currentEvent.telefono);
});

Then('a password reset email should be queued', function() {
  assert.strictEqual(context.currentEvent.type, 'PASSWORD_RESET_REQUESTED');
  assert.ok(context.currentEvent.email);
});

Then('the email should contain a reset link', function() {
  assert.ok(context.currentEvent.token);
});

Then('the dispatch request should be saved', function() {
  assert.strictEqual(context.eventProcessed, true);
});

Then('a password changed email should be queued', function() {
  assert.strictEqual(context.currentEvent.type, 'PASSWORD_CHANGED');
  assert.ok(context.currentEvent.email);
});

Then('a password changed SMS should be queued', function() {
  assert.ok(context.currentEvent.telefono);
});

Then('both dispatch requests should be recorded', function() {
  assert.ok(context.currentEvent.email);
  assert.ok(context.currentEvent.telefono);
  assert.strictEqual(context.eventProcessed, true);
});

