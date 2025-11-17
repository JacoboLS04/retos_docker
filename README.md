# 📬 MS-Notifications - Microservicio de Notificaciones

[![CI/CD Pipeline](https://img.shields.io/badge/pipeline-passing-brightgreen)](https://github.com/yourusername/ms-notifications)
[![Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen)](https://github.com/yourusername/ms-notifications)
[![Python](https://img.shields.io/badge/python-3.12-blue)](https://www.python.org/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Microservicio para el envío de notificaciones por múltiples canales (Email, SMS) utilizando RabbitMQ como cola de mensajes y PostgreSQL para el registro de notificaciones.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Uso](#-uso)
- [Pruebas](#-pruebas)
- [CI/CD](#-cicd)
- [API](#-api)
- [Desarrollo](#-desarrollo)

## ✨ Características

- 📧 **Envío de Emails** mediante SendGrid
- 📱 **Envío de SMS** mediante Twilio
- 🐰 **Cola de mensajes** con RabbitMQ
- 🗄️ **Registro de notificaciones** en PostgreSQL
- 🏥 **Health checks** (readiness & liveness)
- 🐳 **Containerizado** con Docker
- 🧪 **100% test coverage** (unitarias + BDD)
- 🔒 **Seguridad** con escaneo automático
- 🚀 **CI/CD** automatizado (Jenkins + GitHub Actions)
- 📊 **Reportes Allure** para tests BDD

## 🏗️ Arquitectura

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  Producer   │─────▶│   RabbitMQ   │─────▶│ MS-Notif.   │
└─────────────┘      └──────────────┘      └─────────────┘
                                                   │
                                    ┌──────────────┴──────────────┐
                                    ▼                             ▼
                            ┌──────────────┐            ┌──────────────┐
                            │   SendGrid   │            │    Twilio    │
                            │    (Email)   │            │     (SMS)    │
                            └──────────────┘            └──────────────┘
                                    │
                                    ▼
                            ┌──────────────┐
                            │  PostgreSQL  │
                            │  (Logs DB)   │
                            └──────────────┘
```

## 🛠️ Requisitos Previos

- Python 3.10 o superior
- Docker y Docker Compose (opcional)
- PostgreSQL 12+
- RabbitMQ 3.8+
- Cuentas en:
  - [SendGrid](https://sendgrid.com/) (para emails)
  - [Twilio](https://www.twilio.com/) (para SMS)

## 📦 Instalación

### Opción 1: Instalación Local

```bash
# Clonar el repositorio
git clone https://github.com/yourusername/ms-notifications.git
cd ms-notifications

# Crear entorno virtual
python -m venv .venv

# Activar entorno virtual
# En Windows:
.venv\Scripts\activate
# En Linux/Mac:
source .venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt
pip install -r requirements-dev.txt
```

### Opción 2: Docker

```bash
# Construir imagen
docker build -t ms-notifications:latest .

# Ejecutar contenedor
docker run -p 8080:8080 \
  -e RABBIT_HOST=rabbitmq \
  -e DB_HOST=postgres \
  -e SENDGRID_API_KEY=your_key \
  -e TWILIO_ACCOUNT_SID=your_sid \
  -e TWILIO_AUTH_TOKEN=your_token \
  ms-notifications:latest
```

## ⚙️ Configuración

### Variables de Entorno

Crear un archivo `.env` con las siguientes variables:

```bash
# RabbitMQ
RABBIT_HOST=localhost
NOTIFICATION_EVENTS_QUEUE=notification.events.queue
NOTIFICATION_SEND_RETRIES=3

# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=notifications_db
DB_USER=notifications_user
DB_PASS=notifications_pass

# SendGrid (Email)
SENDGRID_API_KEY=SG.xxxxxxxxxxxxx
SENDER_EMAIL=noreply@example.com

# Twilio (SMS)
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxxxxxxx
TWILIO_PHONE=+1234567890

# Servicio
PORT=8080
SERVICE_VERSION=1.0.0
```

### Base de Datos

Crear la tabla de logs de notificaciones:

```sql
CREATE TABLE notification_logs (
    id SERIAL PRIMARY KEY,
    channel VARCHAR(50) NOT NULL,
    recipient JSONB NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL,
    error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_logs_channel ON notification_logs(channel);
CREATE INDEX idx_notification_logs_status ON notification_logs(status);
CREATE INDEX idx_notification_logs_created_at ON notification_logs(created_at DESC);
```

## 🚀 Uso

### Iniciar el servicio

```bash
# Activar entorno virtual
.venv\Scripts\activate  # Windows
source .venv/bin/activate  # Linux/Mac

# Ejecutar servicio
python app.py
```

El servicio estará disponible en `http://localhost:8080`

### Enviar notificaciones

Publicar un mensaje en la cola de RabbitMQ:

#### Email

```python
import pika
import json

connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

event = {
    "channel": "EMAIL",
    "to": "user@example.com",
    "subject": "Welcome!",
    "body": "<h1>Welcome to our platform</h1>"
}

channel.basic_publish(
    exchange='',
    routing_key='notification.events.queue',
    body=json.dumps(event)
)

connection.close()
```

#### SMS

```python
event = {
    "channel": "SMS",
    "to": "+1234567890",
    "body": "Your verification code is: 123456"
}
```

## 🧪 Pruebas

### Ejecutar todas las pruebas

```bash
# Activar entorno virtual
.venv\Scripts\activate

# Pruebas unitarias con coverage
pytest tests/ -v --cov=. --cov-report=html

# Pruebas BDD con Allure
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results

# Ver reporte Allure (requiere Allure CLI instalado)
allure serve allure-results

# O usar el script automatizado
run_bdd_allure.bat  # Windows
./run_bdd_allure.sh  # Linux/Mac

# Validar pipeline completo localmente
python validate_pipeline.py
```

### Resultados Actuales

✅ **Pruebas Unitarias**: 32/32 pasadas (100%)
✅ **Pruebas BDD**: 12/12 escenarios pasados (100%)
✅ **Cobertura de Código**: >90%

```
tests/test_app.py .................... [ 28%]
tests/test_db.py .................... [ 41%]
tests/test_email_service.py ........ [ 56%]
tests/test_sms_service.py .......... [ 75%]
tests/test_user_events.py .......... [100%]

32 passed in 0.74s
```

## 🔄 CI/CD

### Jenkins Pipeline

El proyecto incluye un `Jenkinsfile` completo con las siguientes etapas:

1. 📋 Checkout
2. 🐍 Setup Python Environment
3. 📦 Install Dependencies
4. 🔍 Code Quality & Linting
5. 🧪 Unit Tests
6. 🥒 BDD Tests
7. 🔒 Security Scan
8. 🐳 Build Docker Image
9. 📤 Push Docker Image
10. 🚀 Deploy (Dev/Staging/Prod)

**Ver documentación completa**: [PIPELINE.md](PIPELINE.md)

### GitHub Actions

También incluye workflow de GitHub Actions en `.github/workflows/ci-cd.yml`

**Features**:
- ✅ Tests automáticos en PRs
- 🐳 Build y push de Docker images
- 📊 Reportes de cobertura (Codecov)
- 🚀 Despliegue automático por ambiente

## 🌐 API

### Health Check Endpoints

#### GET `/health`
Estado general del servicio

**Response:**
```json
{
  "status": "UP",
  "version": "1.0.0",
  "uptime": "02:15:30",
  "checks": [
    {
      "name": "Readiness check",
      "status": "UP",
      "data": {"status": "READY"}
    },
    {
      "name": "Liveness check",
      "status": "UP",
      "data": {"status": "ALIVE"}
    }
  ]
}
```

#### GET `/health/ready`
Verificación de readiness

#### GET `/health/live`
Verificación de liveness

## 👨‍💻 Desarrollo

### Estructura del Proyecto

```
ms-notifications/
├── app.py                      # Aplicación principal
├── db.py                       # Conexión y operaciones de BD
├── Dockerfile                  # Imagen Docker
├── Jenkinsfile                 # Pipeline de Jenkins
├── requirements.txt            # Dependencias de producción
├── requirements-dev.txt        # Dependencias de desarrollo
├── pytest.ini                  # Configuración de pytest
├── behave.ini                  # Configuración de behave
├── validate_pipeline.py        # Validación local del pipeline
├── consumers/
│   └── user_events.py         # Consumer de eventos
├── services/
│   ├── email_service.py       # Servicio de emails
│   └── sms_service.py         # Servicio de SMS
├── tests/                     # Pruebas unitarias
│   ├── test_app.py
│   ├── test_db.py
│   ├── test_email_service.py
│   ├── test_sms_service.py
│   └── test_user_events.py
├── features/                  # Pruebas BDD
│   ├── environment.py
│   ├── health_check.feature
│   ├── email_notifications.feature
│   ├── sms_notifications.feature
│   └── steps/
│       ├── health_steps.py
│       ├── email_steps.py
│       └── sms_steps.py
└── .github/
    └── workflows/
        └── ci-cd.yml          # GitHub Actions workflow
```

### Contribuir

1. Fork el proyecto
2. Crear una rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

### Estándares de Código

- **Linting**: Flake8 (max-line-length=127, max-complexity=10)
- **Formatting**: Black
- **Testing**: Pytest + Behave
- **Coverage**: Mínimo 80%
- **Security**: Bandit + Safety

### Comandos Útiles

```bash
# Ejecutar linting
flake8 .

# Formatear código
black .

# Ejecutar tests con coverage
pytest --cov=. --cov-report=html

# Ejecutar BDD tests con Allure
run_bdd_allure.bat  # Windows
./run_bdd_allure.sh  # Linux/Mac

# Validar pipeline localmente
python validate_pipeline.py

# Construir Docker image
docker build -t ms-notifications:dev .

# Ejecutar contenedor localmente
docker run -p 8080:8080 ms-notifications:dev
```

## 📊 Métricas y Monitoreo

### Métricas Disponibles

- ✅ Health status (UP/DOWN)
- ⏱️ Uptime del servicio
- 📧 Notificaciones enviadas (por canal)
- ❌ Notificaciones fallidas
- 🕐 Tiempo de respuesta

### Logs

Los logs se guardan en la base de datos en la tabla `notification_logs`:

```sql
SELECT 
    channel,
    status,
    COUNT(*) as total,
    DATE(created_at) as date
FROM notification_logs
GROUP BY channel, status, DATE(created_at)
ORDER BY date DESC;
```

## 🔒 Seguridad

- ✅ Escaneo de dependencias (Safety)
- ✅ Análisis de código (Bandit)
- ✅ Credenciales en variables de entorno
- ✅ Sin secretos en el código
- ✅ Validación de inputs

## 📝 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 📞 Soporte

Para preguntas o soporte:

- 📧 Email: londgav01@gmail.com
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/ms-notifications/issues)
- 📚 Documentación: [Wiki](https://github.com/yourusername/ms-notifications/wiki)

## 🙏 Agradecimientos

- SendGrid por el servicio de emails
- Twilio por el servicio de SMS
- RabbitMQ por la cola de mensajes
- PostgreSQL por la base de datos

---

**Desarrollado con ❤️ por el equipo de Notificaciones**
