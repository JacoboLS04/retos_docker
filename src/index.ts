import { startOrchestrator } from "./orchestrator.js";
import { startHealthServer, setReady, setAlive, initBootTime } from "./health.js";

async function main() {
    // inicializamos boot time para mostrar uptime
    initBootTime(new Date());

    // arrancar servidor de health (no bloquea)
    startHealthServer({ port: Number(process.env.HEALTH_PORT || 8080) });

    // inicialmente consideramos service alive, pero not-ready hasta conectar a Rabbit
    setAlive(true);
    setReady(false);

    // iniciar orquestador en background y actualizar readiness según resultado
    startOrchestrator()
        .then((ok) => {
            setReady(Boolean(ok));
            if (!ok) {
                console.error("Orchestrator no pudo inicializarse correctamente");
            }
        })
        .catch((err) => {
            console.error("❌ Error en el orquestador:", err);
            setReady(false);
        });
}

main().catch((err) => {
    console.error("❌ Error lanzando la aplicación:", err);
    setAlive(false);
});
