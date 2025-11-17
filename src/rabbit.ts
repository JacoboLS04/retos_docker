import amqp, { ConsumeMessage } from "amqplib";

const RABBIT_URL = process.env.RABBIT_URL || "amqp://guest:guest@rabbitmq:5672";
const EXCHANGE = "user.events.exchange";
const ROUTING_KEY = "user.events.key";

// Usamos any para evitar choques de tipos con amqplib
let connection: any = null;
let channel: any = null;

export async function consumeFromQueue(
    queue: string,
    handler: (msg: any) => void | Promise<void>
) {
    try {
        connection = await amqp.connect(RABBIT_URL);
        channel = await connection.createChannel();

        await channel.assertExchange(EXCHANGE, "topic", { durable: true });
        await channel.assertQueue(queue, { durable: true });
        await channel.bindQueue(queue, EXCHANGE, ROUTING_KEY);

        console.log(`✅ Escuchando eventos en cola: ${queue}`);

        channel.consume(queue, (msg: ConsumeMessage | null) => {
            if (msg) {
                try {
                    const event = JSON.parse(msg.content.toString());
                    console.log("📩 Evento recibido:", event);

                    Promise.resolve(handler(event)).catch(err => {
                        console.error("❌ Error en handler:", err);
                    });
                } catch (err) {
                    console.error("❌ Error procesando evento:", err);
                }
                channel.ack(msg);
            }
        });
    } catch (err) {
        console.error("❌ Error conectando a RabbitMQ:", err);
        throw err;
    }
}

export async function publishToQueue(queue: string, message: any) {
    try {
        if (!connection || !channel) {
            connection = await amqp.connect(RABBIT_URL);
            channel = await connection.createChannel();
        }

        await channel.assertQueue(queue, { durable: true });
        channel.sendToQueue(queue, Buffer.from(JSON.stringify(message)), {
            persistent: true
        });

        console.log(`📤 Mensaje publicado en ${queue}:`, message);
    } catch (err) {
        console.error("❌ Error publicando mensaje:", err);
    }
}
