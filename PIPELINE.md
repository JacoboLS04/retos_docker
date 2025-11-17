# 🚀 Pipeline CI/CD - MS Notifications

## 📋 Descripción General

Este documento describe el pipeline de CI/CD configurado para el microservicio de notificaciones. El pipeline está diseñado para ejecutarse en Jenkins y automatiza todo el proceso desde el código hasta el despliegue.

## 🏗️ Arquitectura del Pipeline

El pipeline consta de las siguientes etapas principales:

### 1. 📋 Checkout
- Clona el repositorio desde Git
- Obtiene el hash corto del commit para etiquetado

### 2. 🐍 Setup Python Environment
- Configura un entorno virtual de Python
- Actualiza pip a la última versión

### 3. 📦 Install Dependencies
- Instala dependencias de producción (`requirements.txt`)
- Instala dependencias de desarrollo (`requirements-dev.txt`)

### 4. 🔍 Code Quality & Linting (Paralelo)
- **Flake8**: Análisis estático de código para detectar errores
- **Black**: Verificación de formato del código Python

### 5. 🧪 Unit Tests
- Ejecuta todas las pruebas unitarias con `pytest`
- Genera reportes de cobertura de código (XML y HTML)
- Publica resultados en formato JUnit
- Genera reportes de cobertura visuales

### 6. 🥒 BDD Tests
- Ejecuta pruebas de comportamiento con `Behave`
- Valida los escenarios de usuario definidos
- Genera reportes JUnit para seguimiento

### 7. 🔒 Security Scan (Paralelo)
- **Safety**: Escaneo de vulnerabilidades en dependencias
- **Bandit**: Análisis de seguridad del código Python

### 8. 🐳 Build Docker Image
- Construye la imagen Docker del microservicio
- Etiqueta con número de build, commit hash y latest

### 9. 🧪 Container Tests
- Valida que el contenedor se ejecuta correctamente
- Verifica la importación de módulos principales

### 10. 📤 Push Docker Image
- Sube la imagen al registro Docker (solo en ramas principales)
- Ejecuta solo en las ramas: main, master, develop

### 11. 🚀 Deploy Stages
- **Development**: Despliegue automático en rama `develop`
- **Staging**: Despliegue manual en rama `main`
- **Production**: Despliegue manual solo en tags (requiere aprobación de admin)

---

## ⚙️ Configuración en Jenkins

### Prerrequisitos

1. **Jenkins instalado** con los siguientes plugins:
   - Pipeline
   - Docker Pipeline
   - JUnit Plugin
   - HTML Publisher Plugin
   - Git Plugin

2. **Credenciales configuradas** en Jenkins:
   - `docker-registry-url`: URL del registro Docker
   - `sendgrid-api-key`: API key de SendGrid
   - `twilio-account-sid`: Account SID de Twilio
   - `twilio-auth-token`: Auth Token de Twilio
   - `twilio-phone-number`: Número de teléfono de Twilio

### Pasos de Configuración

#### 1. Crear un nuevo Pipeline Job

```
1. En Jenkins, ir a "Nueva Tarea"
2. Nombre: "ms-notifications-pipeline"
3. Tipo: "Pipeline"
4. Click en "OK"
```

#### 2. Configurar el Pipeline

En la sección **Pipeline**:

- **Definition**: Pipeline script from SCM
- **SCM**: Git
- **Repository URL**: [Tu URL del repositorio]
- **Credentials**: [Tus credenciales de Git]
- **Branch Specifier**: */main (o tu rama principal)
- **Script Path**: Jenkinsfile

#### 3. Configurar Webhooks (Opcional)

Para builds automáticos en cada push:

1. En tu repositorio Git, configurar webhook
2. URL: `http://[jenkins-url]/github-webhook/`
3. Events: Push events

#### 4. Configurar Build Triggers

En la configuración del job:

- ✅ GitHub hook trigger for GITScm polling
- ✅ Poll SCM (opcional): `H/5 * * * *` (cada 5 minutos)

---

## 🧪 Resultados de las Pruebas

### Pruebas Unitarias (Pytest)

**Estado Actual**: ✅ **32/32 PASADAS** (100%)

```
tests/test_app.py::test_health_endpoint_up PASSED
tests/test_app.py::test_health_ready_endpoint PASSED
tests/test_app.py::test_health_live_endpoint PASSED
tests/test_db.py::test_save_notification_log_success PASSED
tests/test_email_service.py::test_send_email_success PASSED
tests/test_sms_service.py::test_send_sms_success PASSED
tests/test_user_events.py::test_handle_email_event PASSED
... y 25 pruebas más
```

### Pruebas BDD (Behave)

**Estado Actual**: ✅ **12/12 ESCENARIOS PASADOS** (100%)

**Features:**
- ✅ Email Notification Service (4 escenarios)
- ✅ Health Check Endpoints (4 escenarios)
- ✅ SMS Notification Service (4 escenarios)

**Steps:** 45/45 pasos ejecutados exitosamente

---

## 📊 Reportes Generados

El pipeline genera los siguientes reportes:

### 1. Coverage Report (Cobertura de Código)
- **Ubicación**: `reports/coverage-html/index.html`
- **Formato**: HTML interactivo
- **Acceso**: Publicado en Jenkins como "Coverage Report"

### 2. JUnit Test Reports
- **Unit Tests**: `reports/junit-unit-tests.xml`
- **BDD Tests**: `reports/TESTS-*.xml`
- **Visualización**: Gráficos de tendencias en Jenkins

### 3. Security Reports
- **Bandit**: `reports/bandit-report.json`
- **Safety**: Salida en consola

---

## 🐳 Docker Image Tags

Cada build genera múltiples tags para la imagen Docker:

| Tag | Descripción | Ejemplo |
|-----|-------------|---------|
| `BUILD_NUMBER` | Número de build de Jenkins | `ms-notifications:42` |
| `GIT_COMMIT_SHORT` | Hash corto del commit | `ms-notifications:a1b2c3d` |
| `latest` | Última versión construida | `ms-notifications:latest` |

---

## 🔐 Variables de Entorno

### Variables del Pipeline

```groovy
PYTHON_VERSION = '3.12'
DOCKER_IMAGE = 'ms-notifications'
SERVICE_VERSION = "${env.BUILD_NUMBER}"
```

### Variables para Tests

```groovy
RABBIT_HOST = 'localhost'
DB_HOST = 'localhost'
DB_NAME = 'notifications_test_db'
```

### Credenciales Sensibles (configurar en Jenkins)

- `SENDGRID_API_KEY`
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_PHONE`
- `DOCKER_REGISTRY`

---

## 🚀 Estrategia de Deployment

### Flujo de Branches

```
feature/* → develop → main → tags
   ↓          ↓        ↓       ↓
  Test      Dev     Staging  Prod
```

### Ambientes

| Ambiente | Rama | Tipo de Deploy | Aprobación |
|----------|------|----------------|------------|
| **Development** | `develop` | Automático | No requerida |
| **Staging** | `main` | Manual | Usuario con permisos |
| **Production** | `v*.*.*` (tags) | Manual | Administrador |

---

## 🛠️ Comandos Útiles

### Ejecutar pruebas localmente

```bash
# Activar entorno virtual
.venv\Scripts\activate

# Ejecutar pruebas unitarias
pytest tests/ -v

# Ejecutar pruebas BDD
behave features/

# Generar reporte de cobertura
pytest tests/ --cov=. --cov-report=html

# Ejecutar linting
flake8 .
black --check .
```

### Construir imagen Docker localmente

```bash
docker build -t ms-notifications:local .
docker run -p 8080:8080 ms-notifications:local
```

---

## 📈 Métricas del Pipeline

### Tiempo Estimado de Ejecución

| Etapa | Tiempo Aproximado |
|-------|-------------------|
| Checkout | ~10s |
| Setup Python | ~30s |
| Install Dependencies | ~45s |
| Code Quality | ~20s |
| Unit Tests | ~5s |
| BDD Tests | ~3s |
| Security Scan | ~30s |
| Docker Build | ~2min |
| Container Tests | ~10s |
| **Total** | **~4-5 minutos** |

---

## 🔧 Troubleshooting

### Problema: Pruebas fallan en Jenkins pero pasan localmente

**Solución**:
1. Verificar versiones de Python coincidan
2. Revisar variables de entorno
3. Asegurar que todas las dependencias estén instaladas

### Problema: Docker build falla

**Solución**:
1. Verificar Dockerfile existe
2. Revisar logs de build: `docker build --no-cache`
3. Verificar permisos en Jenkins agent

### Problema: No se pueden subir imágenes al registro

**Solución**:
1. Verificar credenciales de Docker Registry en Jenkins
2. Verificar conectividad de red
3. Probar login manual: `docker login [registry-url]`

---

## 📞 Contacto y Soporte

Para problemas con el pipeline, contactar a:
- **DevOps Team**: devops@example.com
- **Slack**: #ms-notifications-ci

---

## 📝 Changelog del Pipeline

### Version 1.0.0 (2025-11-17)
- ✨ Pipeline inicial con todas las etapas
- ✅ Integración de pruebas unitarias y BDD
- 🐳 Build y push de Docker images
- 🔒 Escaneo de seguridad integrado
- 📊 Reportes de cobertura y calidad

---

## 📚 Referencias

- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [Pytest Documentation](https://docs.pytest.org/)
- [Behave Documentation](https://behave.readthedocs.io/)
- [Docker Documentation](https://docs.docker.com/)
