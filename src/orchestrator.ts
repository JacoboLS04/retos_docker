import fs from "fs";
import path from "path";
import { consumeFromQueue, publishToQueue } from "./rabbit.js";
import { saveDispatchRequest, saveReceivedEvent } from "./db.js";
import { v4 as uuidv4 } from "uuid";

const USER_QUEUE = "user.events.queue"; // Cola de entrada de eventos

// Directorio base compatible con Jest y runtime (evita conflicto con __dirname global de CJS)
const _dirname = path.join(process.cwd(), process.env.NODE_ENV === 'production' ? 'dist' : 'src');

function loadTemplate(file: string): string {
    const filePath = path.join(_dirname, "templates", file);
    return fs.readFileSync(filePath, "utf-8");
}

export function renderTemplate(template: string, variables: Record<string, string>): string {
    let output = template;
    for (const [key, value] of Object.entries(variables)) {
        output = output.replace(new RegExp(`{{${key}}}`, "g"), value);
    }
    return output;
}

export async function handleEvent(event: any) {
    // 1. Guardar log del evento recibido
    event.id = event.id || uuidv4();
    console.log("🔖 intentando guardando de evento recibido:", event);

    await saveReceivedEvent(event);
    console.log("✅ Evento guardado en received_events:", event.id);

    try {
        switch (event.type) {
            case "USER_REGISTERED": {
                const tpl = loadTemplate("welcomeEmail.html");
                const body = renderTemplate(tpl, { nombre: event.nombre ?? "", userId: String(event.userId ?? "") });

                const dispatchReq = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["EMAIL"],
                    template: "welcomeEmail.html",
                    templateVersion: "v1",
                    recipient: { email: event.email },
                    payload: { subject: "Bienvenido a la plataforma", body },
                };

                await saveDispatchRequest(dispatchReq);
                await publishToQueue("notification.events.queue", {
                    channel: "EMAIL",
                    to: event.email,
                    subject: "Bienvenido a la plataforma",
                    body,
                });
                break;
            }

            case "USER_LOGIN": {
                const tpl = loadTemplate("loginAlert.txt");
                const body = renderTemplate(tpl, { nombre: event.nombre ?? "" });

                // EMAIL
                const emailReq = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["EMAIL"],
                    template: "loginAlert.txt",
                    templateVersion: "v1",
                    recipient: { email: event.email },
                    payload: { subject: "Alerta de seguridad - Nuevo login", body },
                };

                await saveDispatchRequest(emailReq);
                await publishToQueue("notification.events.queue", {
                    channel: "EMAIL",
                    to: event.email,
                    subject: "Alerta de seguridad - Nuevo login",
                    body,
                });

                // SMS (siempre)
                const smsReq = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["SMS"],
                    template: "smsLoginAlert",
                    templateVersion: "v1",
                    recipient: { telefono: event.telefono ?? "unknown" },
                    payload: { body: `Hola ${event.nombre ?? ""}, detectamos un nuevo inicio de sesión en tu cuenta.` },
                };

                await saveDispatchRequest(smsReq);
                await publishToQueue("notification.events.queue", {
                    channel: "SMS",
                    to: event.telefono ?? "unknown",
                    body: `Hola ${event.nombre ?? ""}, detectamos un nuevo inicio de sesión en tu cuenta.`,
                });

                break;
            }

            case "PASSWORD_RESET_REQUESTED": {
                const tpl = loadTemplate("resetPasswordRequest.html");
                const resetLink = `http://localhost:8080/api/auth/reset-password?token=${event.token}`;
                const body = renderTemplate(tpl, { resetLink });

                const req = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["EMAIL"],
                    template: "resetPasswordRequest.html",
                    templateVersion: "v1",
                    recipient: { email: event.email },
                    payload: { subject: "Recuperación de contraseña", body },
                };

                await saveDispatchRequest(req);
                await publishToQueue("notification.events.queue", {
                    channel: "EMAIL",
                    to: event.email,
                    subject: "Recuperación de contraseña",
                    body,
                });
                break;
            }

            case "PASSWORD_CHANGED": {
                const tpl = loadTemplate("passwordChanged.html");
                const body = renderTemplate(tpl, { nombre: event.nombre ?? "" });

                // EMAIL
                const emailReq = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["EMAIL"],
                    template: "passwordChanged.html",
                    templateVersion: "v1",
                    recipient: { email: event.email },
                    payload: { subject: "Contraseña actualizada", body },
                };

                await saveDispatchRequest(emailReq);
                await publishToQueue("notification.events.queue", {
                    channel: "EMAIL",
                    to: event.email,
                    subject: "Contraseña actualizada",
                    body,
                });

                // SMS (siempre)
                const smsReq = {
                    id: uuidv4(),
                    eventId: event.id,
                    channels: ["SMS"],
                    template: "smsPasswordChanged",
                    templateVersion: "v1",
                    recipient: { telefono: event.telefono ?? "unknown" },
                    payload: { body: "Tu contraseña ha sido modificada exitosamente." },
                };

                await saveDispatchRequest(smsReq);
                await publishToQueue("notification.events.queue", {
                    channel: "SMS",
                    to: event.telefono ?? "unknown",
                    body: "Tu contraseña ha sido modificada exitosamente.",
                });

                break;
            }

            default:
                console.log("⚠️ Evento no manejado por Orchestrator:", event);
        }
    } catch (err: any) {
        console.error("❌ Error procesando evento:", err);
    }
}

export async function startOrchestrator(): Promise<boolean> {
    let retries = 10;

    while (retries > 0) {
        try {
            console.log("⏳ Intentando conectar a RabbitMQ...");
            // envolver el handler para forzar Promise<void> y aislar la inferencia de tipos
            await consumeFromQueue(USER_QUEUE, async (msg: any) => {
                await handleEvent(msg);
            });
            console.log("✅ Orchestrator conectado y escuchando eventos");
            return true; // conexión exitosa
        } catch (err) {
            retries--;
            console.error("❌ Error conectando a RabbitMQ:", err);
            if (retries === 0) {
                console.error("💥 No se pudo conectar a RabbitMQ, quedando en espera...");
                setInterval(() => console.log("⏳ Aún esperando RabbitMQ..."), 30000);
                return false;
            }
            console.log(`🔁 Reintentando en 5s... (${10 - retries}/10)`);
            await new Promise(res => setTimeout(res, 5000));
        }
    }
    // no connection established after retry loop
    return false;
}
