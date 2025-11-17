import amqp from 'amqplib';

async function testRabbit() {
    try {
        const conn = await amqp.connect("amqp://guest:guest@rabbitmq:5672");
        console.log("✅ Conexión exitosa!");
        await conn.close();
    } catch (err) {
        console.error("❌ Error conectando a RabbitMQ:", err);
    }
}

testRabbit();
