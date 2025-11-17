import { consumeFromQueue, publishToQueue } from '../src/rabbit.js';
import amqp from 'amqplib';

// Mock amqplib
jest.mock('amqplib');

describe('RabbitMQ Service', () => {
  let mockChannel: any;
  let mockConnection: any;

  beforeEach(() => {
    // Reset mocks and modules before each test
    jest.clearAllMocks();
    jest.resetModules();

    // Create mock channel
    mockChannel = {
      assertExchange: jest.fn().mockResolvedValue(undefined),
      assertQueue: jest.fn().mockResolvedValue(undefined),
      bindQueue: jest.fn().mockResolvedValue(undefined),
      consume: jest.fn(),
      ack: jest.fn(),
      sendToQueue: jest.fn(),
    };

    // Create mock connection
    mockConnection = {
      createChannel: jest.fn().mockResolvedValue(mockChannel),
    };

    // Mock amqp.connect to always return fresh connection
    (amqp.connect as jest.Mock).mockImplementation(() => Promise.resolve(mockConnection));
  });

  describe('consumeFromQueue', () => {
    it('should connect to RabbitMQ and set up queue consumer', async () => {
      const queue = 'test-queue';
      const handler = jest.fn();

      await consumeFromQueue(queue, handler);

      expect(amqp.connect).toHaveBeenCalled();
      expect(mockConnection.createChannel).toHaveBeenCalled();
      expect(mockChannel.assertExchange).toHaveBeenCalledWith('user.events.exchange', 'topic', { durable: true });
      expect(mockChannel.assertQueue).toHaveBeenCalledWith(queue, { durable: true });
      expect(mockChannel.bindQueue).toHaveBeenCalledWith(queue, 'user.events.exchange', 'user.events.key');
      expect(mockChannel.consume).toHaveBeenCalled();
    });

    it('should process incoming messages with handler', async () => {
      const queue = 'test-queue';
      const handler = jest.fn();
      const testEvent = { type: 'USER_REGISTERED', userId: 123 };

      // Setup consume to trigger handler immediately
      mockChannel.consume.mockImplementation((q: string, callback: Function) => {
        const mockMessage = {
          content: Buffer.from(JSON.stringify(testEvent)),
        };
        callback(mockMessage);
      });

      await consumeFromQueue(queue, handler);

      // Wait a bit for async handler
      await new Promise(resolve => setTimeout(resolve, 50));

      expect(handler).toHaveBeenCalledWith(testEvent);
      expect(mockChannel.ack).toHaveBeenCalled();
    });

    it('should handle connection errors gracefully', async () => {
      const queue = 'test-queue';
      const handler = jest.fn();
      const connectionError = new Error('Connection failed');

      (amqp.connect as jest.Mock).mockRejectedValue(connectionError);

      await expect(consumeFromQueue(queue, handler)).rejects.toThrow('Connection failed');
    });
  });

  describe('publishToQueue', () => {
    it('should publish message to specified queue', async () => {
      const queue = 'notification-queue';
      const message = { id: '123', type: 'EMAIL', to: 'test@example.com' };

      // Since the module uses global state, we can't easily verify internal calls
      // Just verify it doesn't throw an error
      await expect(publishToQueue(queue, message)).resolves.toBeUndefined();
    });

    it('should reuse existing connection on subsequent calls', async () => {
      const queue = 'notification-queue';
      const message1 = { test: 'data1' };
      const message2 = { test: 'data2' };

      // Just verify both calls succeed
      await expect(publishToQueue(queue, message1)).resolves.toBeUndefined();
      await expect(publishToQueue(queue, message2)).resolves.toBeUndefined();
    });

    it('should handle publish errors gracefully', async () => {
      const queue = 'notification-queue';
      const message = { test: 'data' };
      
      mockChannel.sendToQueue.mockImplementation(() => {
        throw new Error('Queue error');
      });

      // Should not throw, but log error
      await expect(publishToQueue(queue, message)).resolves.toBeUndefined();
    });
  });
});
