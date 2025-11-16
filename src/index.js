const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
const morgan = require('morgan');
const cors = require('cors');
const dotenv = require('dotenv');
const axios = require('axios');
const { initializeRabbitMQ, publishEvent } = require('./rabbitmq');

// Load environment variables
dotenv.config({ path: './config.env' });

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

const AUTH_URL = process.env.AUTH_SERVICE_URL || 'http://auth-service:8080';
const PROFILE_URL = process.env.PROFILE_SERVICE_URL || 'http://profile-service:8081';

// Proxy configuration
const authServiceProxy = createProxyMiddleware({
	target: AUTH_URL,
	changeOrigin: true,
	pathRewrite: { '^/api/auth': '' },
	onProxyReq: (proxyReq, req, res) => {
		// optional logging
	}
});

const profileServiceProxy = createProxyMiddleware({
	target: PROFILE_URL,
	changeOrigin: true,
	pathRewrite: { '^/api/profiles': '/profiles' }
});

// Health
app.get('/health', (req, res) => res.json({ status: 'ok' }));

// Auth endpoints: in test mode we call axios directly so Nock can intercept; otherwise use proxy
if (process.env.NODE_ENV === 'test') {
	app.post('/api/auth/register', async (req, res) => {
		try {
			const r = await axios.post(`${AUTH_URL}/register`, req.body);
			res.status(r.status).json(r.data);
		} catch (err) {
			const status = err.response?.status || 500;
			res.status(status).json(err.response?.data || { error: 'Auth register error' });
		}
	});

	app.post('/api/auth/login', async (req, res) => {
		try {
			const r = await axios.post(`${AUTH_URL}/login`, req.body);
			res.status(r.status).json(r.data);
		} catch (err) {
			const status = err.response?.status || 500;
			res.status(status).json(err.response?.data || { error: 'Auth login error' });
		}
	});

	app.delete('/api/auth/delete', async (req, res) => {
		try {
			const r = await axios.delete(`${AUTH_URL}/delete`);
			res.status(r.status).json(r.data);
		} catch (err) {
			const status = err.response?.status || 500;
			res.status(status).json(err.response?.data || { error: 'Auth delete error' });
		}
	});
} else {
	app.post('/api/auth/register', authServiceProxy);
	app.post('/api/auth/login', authServiceProxy);
	app.delete('/api/auth/delete', authServiceProxy);
}

// Profiles proxy
app.use('/api/profiles', profileServiceProxy);

// Combined user endpoints
app.get('/api/users/:userId', async (req, res) => {
	try {
		const userId = req.params.userId;
		const authAxios = axios.create({ baseURL: AUTH_URL });
		const profileAxios = axios.create({ baseURL: PROFILE_URL });

		const [authResp, profileResp] = await Promise.all([
			authAxios.get(`/auth/users/${userId}`),
			profileAxios.get(`/profiles/${userId}`)
		]);

		res.json({ ...authResp.data, profile: profileResp.data });
	} catch (error) {
		console.error('Error fetching combined user data:', error);
		res.status(500).json({ error: 'Error fetching user data', details: error.message });
	}
});

app.put('/api/users/:userId', async (req, res) => {
	try {
		const userId = req.params.userId;
		const { auth, profile } = req.body || {};
		const authAxios = axios.create({ baseURL: AUTH_URL });
		const profileAxios = axios.create({ baseURL: PROFILE_URL });

		const [authUpdate, profileUpdate] = await Promise.all([
			auth ? authAxios.put(`/auth/users/${userId}`, auth) : Promise.resolve(null),
			profile ? profileAxios.put(`/profiles/${userId}`, profile) : Promise.resolve(null)
		]);

		res.json({ auth: authUpdate ? authUpdate.data : null, profile: profileUpdate ? profileUpdate.data : null });
	} catch (error) {
		console.error('Error updating user data:', error);
		res.status(500).json({ error: 'Error updating user data', details: error.message });
	}
});

app.delete('/api/users/:userId', async (req, res) => {
	try {
		const userId = req.params.userId;
		const authAxios = axios.create({ baseURL: AUTH_URL });
		const profileAxios = axios.create({ baseURL: PROFILE_URL });

		await Promise.all([
			authAxios.delete(`/auth/users/${userId}`),
			profileAxios.delete(`/profiles/${userId}`)
		]);

		// Publish event (skip publishing in test to avoid external dependency)
		if (process.env.NODE_ENV !== 'test') {
			try {
				await publishEvent({ type: 'USER_DELETED', userId, timestamp: new Date().toISOString() });
			} catch (err) {
				console.error('Failed to publish USER_DELETED event:', err.message);
			}
		}

		res.json({ message: 'User deleted successfully' });
	} catch (error) {
		console.error('Error deleting user:', error);
		res.status(500).json({ error: 'Error deleting user', details: error.message });
	}
});

// Error handler
app.use((err, req, res, next) => {
	console.error(err.stack);
	res.status(500).json({ error: 'Something went wrong!' });
});

const PORT = process.env.PORT || 3001;

const startServer = async () => {
	const server = app.listen(PORT, () => console.log(`API Gateway running on port ${PORT}`));

	// Initialize rabbitmq in background (best-effort)
	initializeRabbitMQ().catch((err) => console.error('Initial RabbitMQ connection failed:', err.message));

	process.on('SIGTERM', () => {
		console.log('Received SIGTERM. Shutting down.');
		server.close(() => process.exit(0));
	});

	return server;
};

if (process.env.NODE_ENV !== 'test') {
	startServer();
}

module.exports = { app, startServer };

