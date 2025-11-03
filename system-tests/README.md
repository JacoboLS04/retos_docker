# System E2E Tests (Go)

Pruebas de integración de todo el sistema (microservicios + colas + DB) ejecutadas dentro de la red Docker `retos-net`.

## Requisitos
- Stack levantado con docker compose (desde `reto-automatizacion`).
- La red `retos-net` existe (compose ya la crea como red externa).

## Cómo ejecutar (PowerShell)
Dos modos de ejecución:

1) Modo contenedor (red Docker) — recomendado para validar todo el flujo

```powershell
$TESTS = "C:\\Users\\londg\\IdeaProjects\\microservicios\\retos_observabilidad\\system-tests"

docker run --rm `
  --network retos-net `
  --mount type=bind,source="$TESTS",target=/app `
  -w /app `
  golang:1.22 `
  sh -lc "go mod tidy && go test -v -tags e2e ./..."
```

2) Modo local (sin contenedor) — útil cuando no puedes descargar imágenes de Docker Hub

```powershell
# Requiere Go instalado en el host (go version >= 1.20)
cd C:\\Users\\londg\\IdeaProjects\\microservicios\\retos_observabilidad\\system-tests

# Mapeos locales (RabbitMQ/Postgres expuestos por docker-compose)
$env:AMQP_URL = "amqp://guest:guest@localhost:5672"
$env:PGHOST = "localhost"
$env:PGPORT = "5434"
$env:PGDATABASE = "notifications_db"
$env:PGUSER = "retos_user"
$env:PGPASSWORD = "retos_pass"

# Publicar directo a la cola de ms-notifications (bypassa orchestrator)
$env:TEST_PUBLISH_QUEUE = "notification.events.queue"

go mod tidy
go test -v -tags e2e -run Test_Rabbit_To_DB ./...
```

## Casos incluidos
- health_e2e_test.go: verifica health de spring-app, orchestrator, ms-notifications.
- rabbit_to_db_e2e_test.go: publica un evento a `user.events.queue` y espera un registro en Postgres `notifications_db`.

## Variables de entorno (opcionales)
- Health endpoints:
  - `SPRING_READY_URL` (default `http://retos-spring-app:8080/health/ready`)
  - `ORCH_READY_URL` (default `http://orchestrator:8080/health`)
  - `MS_NOTIF_READY_URL` (default `http://ms-notifications:8080/health`)
- RabbitMQ:
  - `AMQP_URL` (default `amqp://guest:guest@rabbitmq:5672`)
  - `TEST_PUBLISH_QUEUE` (default `notification.events.queue`) — para publicar directo a ms-notifications
- Postgres (ms-notifications):
  - `PGHOST` (default `notifications-db`)
  - `PGPORT` (default `5432`)
  - `PGDATABASE` (default `notifications_db`)
  - `PGUSER` (default `retos_user`)
  - `PGPASSWORD` (default `retos_pass`)
  - `NOTIF_TABLE` (default `notification_logs`)
  - `NOTIF_COLUMN` (default `payload->>'traceId'`)

Ajusta `NOTIF_TABLE`/`NOTIF_COLUMN` si el esquema real difiere.

## Notas
- Las pruebas usan la etiqueta de build `e2e`. Si ejecutas `go test` sin `-tags e2e`, no correrán.
- Si necesitas validar alertas de Alertmanager, agrega un test que consulte `http://alertmanager:9093/api/v2/alerts` tras detener/arrancar un servicio (la acción stop/start puede hacerse desde el pipeline de Jenkins antes/después del test).
