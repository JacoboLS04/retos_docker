# Notification Orchestrator

Microservicio orquestador de notificaciones que procesa eventos de usuarios y envía notificaciones a través de múltiples canales (EMAIL, SMS).

## 📋 Características

- ✅ **Health Checks**: Endpoints `/health`, `/health/ready`, `/health/live` para monitoreo
- 🔄 **Procesamiento de Eventos**: Consume eventos de RabbitMQ y orquesta notificaciones
- 📊 **Observabilidad**: Integración con Grafana/Loki para logs
- 🧪 **Testing Completo**: Tests unitarios (Jest) y BDD (Cucumber)
- 🐳 **Containerizado**: Dockerfile incluido para despliegue
- 🔧 **CI/CD**: Pipeline Jenkins configurado

## 🚀 Inicio Rápido

### Prerrequisitos

- Node.js 18+ 
- PostgreSQL 12+
- RabbitMQ 3.8+

### Instalación

```bash
# Clonar repositorio
git clone <repository-url>
cd notification-orchestrator

# Instalar dependencias
npm install

# Compilar TypeScript
npm run build
```

### Configuración

Variables de entorno (archivo `.env`):

```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=notification_db
DB_USER=postgres
DB_PASSWORD=postgres

# RabbitMQ
RABBIT_URL=amqp://guest:guest@localhost:5672

# Health Server
HEALTH_PORT=8080
NODE_ENV=production
```

### Ejecución

```bash
# Desarrollo
npm run dev

# Producción
npm start
```

## 🧪 Testing

### Tests Unitarios (Jest)

```bash
# Ejecutar todos los tests
npm test

# Con cobertura
npm test -- --coverage

# Modo watch
npm test -- --watch
```

### Tests BDD (Cucumber)

```bash
# Ejecutar tests Cucumber
npm run test:bdd
```

Los reportes se generan en:
- HTML: `reports/cucumber-report.html`
- JSON: `reports/cucumber-report.json`

## 📡 Health Endpoints

### GET `/health`

Retorna el estado general del servicio:

```json
{
  "status": "UP",
  "version": "1.0.0",
  "name": "notification-orchestrator",
  "checks": [
    {
      "name": "Readiness check",
      "status": "UP",
      "data": {
        "from": "2025-11-17T10:00:00.000Z",
        "status": "READY"
      }
    },
    {
      "name": "Liveness check",
      "status": "UP",
      "data": {
        "from": "2025-11-17T10:00:00.000Z",
        "status": "ALIVE"
      }
    }
  ]
}
```

### GET `/health/ready`

Endpoint de readiness para Kubernetes:
- `200`: Servicio listo para recibir tráfico
- `503`: Servicio no está listo

### GET `/health/live`

Endpoint de liveness para Kubernetes:
- `200`: Servicio está vivo
- `503`: Servicio necesita reiniciarse

## 🔄 Eventos Soportados

### USER_REGISTERED
Envía email de bienvenida cuando un usuario se registra.

### USER_LOGIN
Envía alerta de seguridad por EMAIL y SMS cuando un usuario inicia sesión.

### PASSWORD_RESET_REQUESTED
Envía email con enlace de recuperación de contraseña.

### PASSWORD_CHANGED
Envía confirmación por EMAIL y SMS cuando se cambia la contraseña.

## 🐳 Docker

```bash
# Construir imagen
docker build -t notification-orchestrator:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e DB_HOST=postgres \
  -e RABBIT_URL=amqp://rabbitmq:5672 \
  notification-orchestrator:latest
```

## 🔧 CI/CD (Jenkins)

El proyecto incluye un `Jenkinsfile` con el siguiente pipeline:

1. **Checkout**: Clona el repositorio
2. **Install Dependencies**: Instala dependencias de Node.js
3. **Lint & Build**: Compila TypeScript
4. **Unit Tests**: Ejecuta tests con Jest y genera reportes
5. **BDD Tests**: Ejecuta tests Cucumber y publica reportes HTML/JSON
6. **Build Docker Image**: Construye imagen Docker (solo en ramas principales)
7. **Security Scan**: Escanea vulnerabilidades con npm audit
8. **Deploy**: Despliega a staging/producción según la rama

### Requisitos Jenkins

Plugins necesarios:
- NodeJS Plugin
- Docker Pipeline
- HTML Publisher
- Cucumber Reports
- JUnit Plugin

Configuración:
- Crear credential `db-user` y `db-password`
- Configurar Node.js 20 en Global Tool Configuration

### Reportes Generados

- **Jest Coverage**: Cobertura de código HTML
- **JUnit XML**: Resultados de tests unitarios
- **Cucumber HTML**: Reporte visual de tests BDD
- **Cucumber JSON**: Métricas y tendencias de BDD

## 📁 Estructura del Proyecto

```
notification-orchestrator/
├── src/
│   ├── index.ts              # Punto de entrada
│   ├── orchestrator.ts       # Lógica de orquestación
│   ├── rabbit.ts             # Cliente RabbitMQ
│   ├── db.ts                 # Cliente PostgreSQL
│   ├── health.ts             # Health checks
│   └── templates/            # Plantillas de notificaciones
├── __tests__/                # Tests unitarios
├── features/                 # Tests BDD Cucumber
│   ├── *.feature            # Escenarios Gherkin
│   └── support/             # Step definitions
├── Jenkinsfile              # Pipeline CI/CD
├── Dockerfile               # Imagen Docker
├── jest.config.js           # Configuración Jest
├── cucumber.js              # Configuración Cucumber
└── tsconfig.json            # Configuración TypeScript
```

## 📊 Métricas y Monitoreo

El servicio expone métricas a través de los health endpoints que pueden ser consumidos por:

- Prometheus (scraping de `/health`)
- Grafana (visualización)
- Kubernetes (liveness/readiness probes)

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

Asegúrate de que:
- ✅ Todos los tests pasen (`npm test && npm run test:bdd`)
- ✅ El código compile sin errores (`npm run build`)
- ✅ Se mantenga la cobertura de código

## 📝 Licencia

ISC

## 👥 Autores

Tu equipo de desarrollo
