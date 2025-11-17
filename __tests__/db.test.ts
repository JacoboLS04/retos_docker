import { pool, saveReceivedEvent, saveDispatchRequest } from '../src/db.js';

// Mock pg Pool
jest.mock('pg', () => {
  const mockQuery = jest.fn();
  const mockPool = {
    query: mockQuery,
  };
  return {
    __esModule: true,
    default: {
      Pool: jest.fn(() => mockPool),
    },
    Pool: jest.fn(() => mockPool),
  };
});

describe('Database Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('saveReceivedEvent', () => {
    it('should save event to received_events table', async () => {
      const event = {
        id: 'evt-123',
        type: 'USER_REGISTERED',
        userId: 456,
        email: 'test@example.com',
      };

      await saveReceivedEvent(event);

      expect(pool.query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO received_events'),
        [
          event.id,
          event.type,
          '456',
          JSON.stringify(event),
        ]
      );
    });

    it('should handle events without userId', async () => {
      const event = {
        id: 'evt-456',
        type: 'SYSTEM_EVENT',
      };

      await saveReceivedEvent(event);

      expect(pool.query).toHaveBeenCalledWith(
        expect.any(String),
        [
          event.id,
          event.type,
          '',
          JSON.stringify(event),
        ]
      );
    });

    it('should handle database errors gracefully', async () => {
      const event = {
        id: 'evt-789',
        type: 'TEST_EVENT',
      };

      (pool.query as jest.Mock).mockRejectedValue(new Error('DB connection failed'));

      // Should not throw, but log error
      await expect(saveReceivedEvent(event)).resolves.toBeUndefined();
    });
  });

  describe('saveDispatchRequest', () => {
    it('should save dispatch request with all fields', async () => {
      // Asegurar que el mock no tiene un estado de rechazo del test anterior
      (pool.query as jest.Mock).mockResolvedValue({});

      const dispatchRequest = {
        id: 'req-123',
        eventId: 'evt-456',
        channels: ['email', 'sms'],
        template: 'welcomeEmail',
        templateVersion: '1.0',
        recipient: {
          email: 'user@example.com',
          phone: '+1234567890',
        },
        payload: {
          name: 'John Doe',
          verificationLink: 'https://example.com/verify',
        },
      };

      await saveDispatchRequest(dispatchRequest);

      expect(pool.query).toHaveBeenCalledWith(
        expect.stringContaining('INSERT INTO dispatch_requests'),
        [
          dispatchRequest.id,
          dispatchRequest.eventId,
          JSON.stringify(dispatchRequest.channels),
          dispatchRequest.template,
          dispatchRequest.templateVersion,
          JSON.stringify(dispatchRequest.recipient),
          JSON.stringify(dispatchRequest.payload),
        ]
      );
    });

    it('should handle database errors', async () => {
      const dispatchRequest = {
        id: 'req-789',
        eventId: 'evt-123',
        channels: ['email'],
        template: 'testTemplate',
        templateVersion: '1.0',
        recipient: {},
        payload: {},
      };

      (pool.query as jest.Mock).mockRejectedValue(new Error('Insert failed'));

      await expect(saveDispatchRequest(dispatchRequest)).rejects.toThrow('Insert failed');
    });
  });
});
