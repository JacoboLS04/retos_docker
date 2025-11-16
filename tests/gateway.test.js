const request = require('supertest');
const nock = require('nock');
const { app } = require('../src/index');

describe('Gateway additional tests', () => {
  afterEach(() => nock.cleanAll());

  test('POST /api/auth/login forwards to auth service and returns token', async () => {
    const payload = { username: 'jdoe', password: 'secret' };

    nock('http://auth-service:8080')
      .post('/login', payload)
      .reply(200, { token: 'jwt-token-123', userId: 'user-1' });

    const res = await request(app).post('/api/auth/login').send(payload);
    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('token', 'jwt-token-123');
    expect(res.body).toHaveProperty('userId', 'user-1');
  });

  test('PUT /api/users/:userId updates auth and profile when both provided', async () => {
    const userId = 'u-42';
    const body = { auth: { email: 'new@example.com' }, profile: { bio: 'updated bio' } };

    nock('http://auth-service:8080')
      .put(`/auth/users/${userId}`, body.auth)
      .reply(200, { id: userId, email: body.auth.email });

    nock('http://profile-service:8081')
      .put(`/profiles/${userId}`, body.profile)
      .reply(200, { id: userId, bio: body.profile.bio });

    const res = await request(app).put(`/api/users/${userId}`).send(body);
    expect(res.status).toBe(200);
    expect(res.body.auth).toHaveProperty('email', 'new@example.com');
    expect(res.body.profile).toHaveProperty('bio', 'updated bio');
  });

  test('PUT /api/users/:userId with only profile updates profile and returns null auth', async () => {
    const userId = 'u-99';
    const body = { profile: { location: 'mars' } };

    nock('http://profile-service:8081')
      .put(`/profiles/${userId}`, body.profile)
      .reply(200, { id: userId, location: body.profile.location });

    const res = await request(app).put(`/api/users/${userId}`).send(body);
    expect(res.status).toBe(200);
    expect(res.body.auth).toBeNull();
    expect(res.body.profile).toHaveProperty('location', 'mars');
  });
});
