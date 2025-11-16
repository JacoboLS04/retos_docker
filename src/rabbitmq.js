const amqplib = require('amqplib');

// Configurable via env
const RABBIT_URL = process.env.RABBIT_URL || process.env.RABBITMQ_URL || 'amqp://guest:guest@rabbitmq:5672';
const USER_QUEUE = process.env.USER_EVENTS_QUEUE || 'user.events.queue';
const NOTIF_QUEUE = process.env.NOTIFICATION_EVENTS_QUEUE || 'notification.events.queue';

let connection = null;
let channel = null;

async function initializeRabbitMQ() {
  if (process.env.NODE_ENV === 'test') {
    // skip real connections in tests
    return null;
  }

  if (connection && channel) return { connection, channel };

  try {
    connection = await amqplib.connect(RABBIT_URL);
    channel = await connection.createChannel();

    await channel.assertQueue(USER_QUEUE, { durable: true });
    await channel.assertQueue(NOTIF_QUEUE, { durable: true });

    connection.on('error', (err) => {
      console.error('RabbitMQ connection error:', err && err.message);
    });
    connection.on('close', () => {
      console.warn('RabbitMQ connection closed, will attempt reconnect');
      connection = null;
      channel = null;
      setTimeout(() => initializeRabbitMQ().catch(() => {}), 5000);
    });

    console.log('Connected to RabbitMQ at', RABBIT_URL);
    return { connection, channel };
  } catch (err) {
    console.error('Failed to initialize RabbitMQ:', err && err.message);
    // retry after delay
    connection = null;
    channel = null;
    setTimeout(() => initializeRabbitMQ().catch(() => {}), 5000);
    return null;
  }
}

async function publishEvent(event, queue = USER_QUEUE) {
  if (process.env.NODE_ENV === 'test') return false;

  if (!channel) {
    const init = await initializeRabbitMQ();
    if (!init) return false;
  }

  try {
    const payload = Buffer.from(JSON.stringify(event));
    channel.sendToQueue(queue, payload, { persistent: true });
    console.log(`Published event to ${queue}:`, event.type || '(no-type)');
    return true;
  } catch (err) {
    console.error('Error publishing event to RabbitMQ:', err && err.message);
    return false;
  }
}

module.exports = { initializeRabbitMQ, publishEvent };
