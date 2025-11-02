# Monitoring Service (Go)

Servicio ligero para registrar y monitorear microservicios con Prometheus/Blackbox.

## Endpoints
- POST `/register` con cuerpo `{"name":"svc","url":"http://svc:8080/health"}`
- GET `/services` lista los servicios registrados y su último estado
- GET `/metrics` expone métricas de Prometheus (`service_up`, `service_last_check_timestamp`)
- POST `/alert` recibe webhooks de Alertmanager y los reenvía a un microservicio de notificaciones

## Variables de entorno
- `PORT` (por defecto `8085`)
- `TARGETS_FILE` (por defecto `/out/targets.json`) Ruta donde se escribe el archivo file_sd para Prometheus
- `NOTIFICATIONS_ENDPOINT` (opcional) URL del servicio de notificaciones para reenviar alertas

Nota sobre permisos: si ejecutas los tests o el servicio sin privilegios, evita usar rutas del sistema como `/out`. Define `TARGETS_FILE` apuntando a un directorio de trabajo (p. ej. `./test_output/targets.json`). El código crea automáticamente el directorio padre de esa ruta.

---

## Pruebas: ¿Qué valida cada test?

Archivo `main_test.go` (pruebas unitarias):
- `TestRegisterHandler`
   - Caso válido: registra un servicio y escribe el archivo de targets.
   - Casos inválidos: rechaza registro sin `name` o sin `url` con `400 Bad Request`.
- `TestListHandler`
   - Devuelve el listado de servicios registrados con su último estado y timestamp.
- `TestRunChecks`
   - Ejecuta chequeos de salud contra un servidor HTTP simulado y actualiza `lastUp` y `lastTime`.
- `TestAlertHandler`
   - Acepta un webhook de alerta y lo reenvía a `NOTIFICATIONS_ENDPOINT` (simulado en la prueba).

Archivo `integration_test.go` (pruebas de integración, tag `integration`):
- `TestIntegration_ServiceLifecycle`
   - Levanta el servicio real en un puerto de pruebas.
   - Registra un servicio y verifica que se cree `targets.json` en la ruta definida por `TARGETS_FILE`.
   - Lista servicios y valida que el registrado aparece.
   - Consulta `/metrics` y valida que existan las métricas principales.
   - Envía un webhook a `/alert` y espera `200 OK`.

---

## Cómo ejecutar las pruebas localmente

### Windows (PowerShell) con `test.bat`
```powershell
# Ejecutar todas las pruebas
.\test.bat test

# Solo unit tests
.\test.bat test-unit

# Solo integración (requiere tag)
.\test.bat test-integration

# Ejecutar servicio
.\test.bat run

# Compilar
.\test.bat build
```

### Unix/Linux (Makefile)
```bash
# Todas las pruebas (unit + integration)
make test

# Unit tests
make test-unit

# Integration tests
make test-integration

# End-to-end (E2E) contra el stack completo
# Requiere que el stack esté arriba y accesible desde el host
# Variables útiles:
#   MONITORING_BASE_URL (por defecto http://localhost:8085)
#   PROMETHEUS_BASE_URL (opcional, por defecto http://localhost:9090)
#   TARGET_SERVICE_URL  (por defecto http://prometheus:9090/-/healthy)
make test-e2e

# Ejecutar servicio
make run

# Compilar
make build
```

### Comandos Go directos
```bash
# Unit tests con cobertura
go test -v -cover ./...

# Integration tests (requiere tag "integration")
go test -v -tags=integration ./...

# Generar reporte HTML de cobertura (ejemplo en el paquete actual)
go test -coverprofile=coverage.out . && \
   go tool cover -html=coverage.out -o coverage.html
```

### Pruebas E2E (Go directas)
```bash
# Ajusta variables según tu entorno
export MONITORING_BASE_URL=http://localhost:8085
export PROMETHEUS_BASE_URL=http://localhost:9090   # opcional
export TARGET_SERVICE_URL=http://prometheus:9090/-/healthy

go test -v -tags=e2e ./e2e -timeout 2m
```

Para evitar problemas de permisos al escribir `targets.json`, exporta `TARGETS_FILE` a una ruta dentro de tu proyecto:
```bash
export TARGETS_FILE="$PWD/test_output/targets.json"
mkdir -p "$(dirname "$TARGETS_FILE")"
```

---

## Reportes en Jenkins
- El pipeline convierte la salida de `go test` a JUnit con `go-junit-report`, por lo que verás:
   - Tendencias de pruebas (verde/rojo, flakiness) en Jenkins.
   - Reportes de cobertura en HTML (unit e integration).
- Allure (opcional): si integras un adaptador Allure para Go (por ejemplo, `github.com/ozontech/allure-go`) y generas `allure-results/`, el pipeline publicará automáticamente el reporte Allure (si el plugin está instalado en Jenkins).

### Requisitos del agente de Jenkins
- Go instalado (se usa la herramienta `go-1.21`).
- Plugin JUnit y Publish HTML (para mostrar resultados y cobertura).
- Plugin Allure (opcional) si deseas reportes Allure.

---

## Solución de problemas (FAQ)
**Permiso denegado al crear `/out`**
- Define `TARGETS_FILE` a una ruta dentro del workspace (p. ej. `./test_output/targets.json`). El servicio creará el directorio padre si no existe.

**Las pruebas de integración fallan por puerto en uso**
- El test usa por defecto el puerto `8086` (variable `PORT`). Ajusta `PORT` si hay conflicto.

**El reenvío de alertas falla en integración**
- El servicio intenta contactar `NOTIFICATIONS_ENDPOINT` (por defecto `http://ms-notifications:8080/alerts`). En pruebas unitarias se simula; en integración puedes definir una URL mock o ignorar el log de error si no afecta las aserciones.

---

## Desarrollo
Variables principales:
- `filePath` (ruta efectiva de `TARGETS_FILE`)
- Métricas Prometheus: `service_up`, `service_last_check_timestamp`

El servicio realiza chequeos periódicos (configurables con `SCRAPE_INTERVAL_SECONDS`) y actualiza métricas y estados de los servicios registrados.
