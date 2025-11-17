import pkg from 'pg';
const { Pool } = pkg;

export const pool = new Pool({
    host: process.env.DB_HOST,
    port: Number(process.env.DB_PORT || 5432),
    database: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
});

export async function saveReceivedEvent(event: any) {
    const query = `
        INSERT INTO received_events (id, event_type, user_id, payload)
        VALUES ($1, $2, $3, $4)
    `;
    try {
        await pool.query(query, [
            event.id,
            event.type,
            String(event.userId ?? ""),
            JSON.stringify(event)   // 👈
        ]);
        console.log("✅ Evento guardado en received_events:", event.id);
    } catch (err) {
        console.error("❌ Error guardando received_event:", err);
        console.error("Evento problemático:", event);
    }
}


export async function saveDispatchRequest(req: any) {
    console.log("💾 Guardando solicitud de envío:", req);
    const query = `
    INSERT INTO dispatch_requests (id, event_id, channels, template, template_version, recipient, payload)
    VALUES ($1, $2, $3, $4, $5, $6, $7)
  `;
    await pool.query(query, [
        req.id,
        req.eventId,
        JSON.stringify(req.channels),
        req.template,
        req.templateVersion,
        JSON.stringify(req.recipient),
        JSON.stringify(req.payload),
    ]);
}
