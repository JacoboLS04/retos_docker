import http from "http";
import { readFileSync } from "fs";
import path from "path";

// Directorio base compatible con Jest y runtime (evita conflicto con __dirname global de CJS)
const _dirname = path.join(process.cwd(), process.env.NODE_ENV === 'production' ? 'dist' : 'src');

const pkgPath = path.join(_dirname, "..", "package.json");
let pkg: { name?: string; version?: string } = {};
try {
    const raw = readFileSync(pkgPath, "utf8");
    pkg = JSON.parse(raw);
} catch (err) {
    // noop, usaremos valores por defecto
}

let bootTime = new Date();
let ready = false;
let alive = true;

export function setReady(value: boolean) {
    ready = value;
}

export function setAlive(value: boolean) {
    alive = value;
}

function makeHealthResponse() {
    const from = bootTime.toISOString();
    const readinessStatus = ready ? "READY" : "NOT_READY";
    const livenessStatus = alive ? "ALIVE" : "DEAD";

    const overall = ready && alive ? "UP" : "DOWN";

    return {
        status: overall,
        version: pkg.version ?? "unknown",
        name: pkg.name ?? "service",
        checks: [
            {
                name: "Readiness check",
                status: ready ? "UP" : "DOWN",
                data: {
                    from,
                    status: readinessStatus,
                },
            },
            {
                name: "Liveness check",
                status: alive ? "UP" : "DOWN",
                data: {
                    from,
                    status: livenessStatus,
                },
            },
        ],
    };
}

export function startHealthServer(opts?: { port?: number }) {
    const port = opts?.port ?? (process.env.HEALTH_PORT ? Number(process.env.HEALTH_PORT) : 8080);

    const server = http.createServer((req, res) => {
        const url = req.url || "";
        if (url === "/health" || url === "/health/") {
            const body = makeHealthResponse();
            res.writeHead(200, { "Content-Type": "application/json" });
            res.end(JSON.stringify(body));
            return;
        }

        if (url === "/health/ready") {
            const body = makeHealthResponse().checks[0];
            res.writeHead(ready ? 200 : 503, { "Content-Type": "application/json" });
            res.end(JSON.stringify(body));
            return;
        }

        if (url === "/health/live") {
            const body = makeHealthResponse().checks[1];
            res.writeHead(alive ? 200 : 503, { "Content-Type": "application/json" });
            res.end(JSON.stringify(body));
            return;
        }

        // ruta no encontrada
        res.writeHead(404, { "Content-Type": "application/json" });
        res.end(JSON.stringify({ error: "not_found" }));
    });

    server.listen(port, () => {
        console.log(`🔎 Health endpoints listening on http://0.0.0.0:${port}/health`);
    });

    return server;
}

// Exponer estado inicial y utilidades
export function getBootTimeIso() {
    return bootTime.toISOString();
}

export function initBootTime(date?: Date) {
    bootTime = date ?? new Date();
}
