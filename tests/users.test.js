const request = require('supertest');
const nock = require('nock');
const { app, startServer } = require('../src/index');

let server;

beforeAll(async () => {
  process.env.NODE_ENV = 'test';
  process.env.AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://auth-service:8080';
  process.env.PROFILE_SERVICE_URL = process.env.PROFILE_SERVICE_URL || 'http://profile-service:8081';
  server = await startServer();
});

afterAll(async () => {
  if (server && server.close) {
    await new Promise((resolve) => server.close(resolve));
  }
  // Wait for port to be released
  await new Promise(resolve => setTimeout(resolve, 100));
});

describe('API Gateway basic routes', () => {
  test('GET /health', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('status', 'ok');
  });

  test('GET /api/users/:userId returns combined auth+profile', async () => {
    const userId = 'user-123';

    // Mock auth service
    nock('http://auth-service:8080')
      .get(`/auth/users/${userId}`)
      .reply(200, { id: userId, username: 'jdoe', email: 'jdoe@example.com' });

    // Mock profile service
    nock('http://profile-service:8081')
      .get(`/profiles/${userId}`)
      .reply(200, { bio: 'hello', location: 'earth' });

    const res = await request(app).get(`/api/users/${userId}`);
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('id', userId);
    expect(res.body).toHaveProperty('profile');
    expect(res.body.profile).toHaveProperty('bio', 'hello');
  });

  test('DELETE /api/users/:userId deletes auth and profile and returns message', async () => {
    const userId = 'user-delete-1';

    nock('http://auth-service:8080')
      .delete(`/auth/users/${userId}`)
      .reply(200, { message: 'auth deleted' });

    nock('http://profile-service:8081')
      .delete(`/profiles/${userId}`)
      .reply(200, { message: 'profile deleted' });

    const res = await request(app).delete(`/api/users/${userId}`);
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'User deleted successfully');
  });

  test('POST /api/auth/register proxies/registers with auth service', async () => {
    const userPayload = { username: 'newuser', password: 'pass' };

    nock('http://auth-service:8080')
      .post('/register', userPayload)
      .reply(201, { id: 'new-1', username: 'newuser' });

    const res = await request(app).post('/api/auth/register').send(userPayload);
    expect(res.status).toBe(201);
    expect(res.body).toHaveProperty('id', 'new-1');
  });
});
