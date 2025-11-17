# ⚡ Guía Rápida de Setup CI/CD

## 🎯 Para empezar en 5 minutos

### Opción 1: Jenkins Pipeline

#### Paso 1: Configurar Credenciales en Jenkins

1. Ir a **Jenkins → Manage Jenkins → Credentials**
2. Agregar las siguientes credenciales:

```
ID: docker-registry-url
Type: Secret text
Value: [URL de tu Docker Registry]

ID: sendgrid-api-key
Type: Secret text
Value: [Tu SendGrid API Key]

ID: twilio-account-sid
Type: Secret text
Value: [Tu Twilio Account SID]

ID: twilio-auth-token
Type: Secret text
Value: [Tu Twilio Auth Token]

ID: twilio-phone-number
Type: Secret text
Value: [Tu número Twilio]
```

#### Paso 2: Crear Pipeline Job

1. **Nueva Tarea** → `ms-notifications-pipeline`
2. Tipo: **Pipeline**
3. En **Pipeline**:
   - Definition: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `[tu-repo-url]`
   - Script Path: `Jenkinsfile`
4. **Guardar**

#### Paso 3: Ejecutar Build

1. Click en **"Build Now"**
2. Ver el progreso en **"Console Output"**
3. ✅ Build exitoso!

---

### Opción 2: GitHub Actions

#### Paso 1: Configurar Secrets en GitHub

1. Ir a **Settings → Secrets and variables → Actions**
2. Click en **"New repository secret"**
3. Agregar:

```
SENDGRID_API_KEY = [tu-key]
TWILIO_ACCOUNT_SID = [tu-sid]
TWILIO_AUTH_TOKEN = [tu-token]
TWILIO_PHONE_NUMBER = [tu-número]
```

#### Paso 2: Activar GitHub Actions

1. El workflow ya está en `.github/workflows/ci-cd.yml`
2. Push a `main` o `develop`
3. Ir a **Actions** tab en GitHub
4. ✅ Ver el workflow ejecutándose!

---

### Opción 3: Validación Local (Antes de Commit)

```bash
# Activar entorno virtual
.venv\Scripts\activate  # Windows
source .venv/bin/activate  # Linux/Mac

# Ejecutar validación completa
python validate_pipeline.py
```

**Esto ejecutará**:
- ✅ Verificación de Python
- ✅ Linting (Flake8)
- ✅ Tests unitarios
- ✅ Tests BDD
- ✅ Security checks
- ✅ Docker build (opcional)

---

## 🧪 Comandos Rápidos

### Tests

```bash
# Solo tests unitarios
pytest tests/ -v

# Solo tests BDD con Allure
run_bdd_allure.bat  # Windows
./run_bdd_allure.sh  # Linux/Mac

# BDD sin Allure
behave features/

# Con coverage
pytest tests/ --cov=. --cov-report=html
```

### Linting

```bash
# Verificar código
flake8 .

# Auto-formatear
black .
```

### Docker

```bash
# Build
docker build -t ms-notifications:dev .

# Run
docker run -p 8080:8080 ms-notifications:dev

# Test health
curl http://localhost:8080/health
```

---

## 📊 Verificar que Todo Funciona

### 1. Tests Locales

```bash
pytest tests/ -v
```

**Esperado**: ✅ `32 passed in 0.74s`

### 2. Tests BDD

```bash
behave features/
```

**Esperado**: ✅ `12 scenarios passed`

### 3. Docker

```bash
docker build -t test .
```

**Esperado**: ✅ Build exitoso

---

## 🚨 Troubleshooting Común

### Error: "ModuleNotFoundError"

```bash
# Solución: Instalar dependencias
pip install -r requirements.txt -r requirements-dev.txt
```

### Error: "pytest: command not found"

```bash
# Solución: Activar entorno virtual
.venv\Scripts\activate  # Windows
```

### Error: Docker build fails

```bash
# Solución: Verificar Dockerfile existe
ls -la Dockerfile

# Rebuild sin cache
docker build --no-cache -t ms-notifications .
```

---

## 📈 Verificar Pipeline en Jenkins

1. **Dashboard** → Seleccionar job
2. Ver **Build History**
3. Click en build específico
4. Ver:
   - ✅ **Console Output** - Logs completos
   - ✅ **Test Result** - Resultados de tests
   - ✅ **Coverage Report** - Cobertura de código

---

## 🎯 Checklist de Setup

### Pre-requisitos
- [ ] Python 3.10+ instalado
- [ ] Git instalado
- [ ] Docker instalado (opcional)
- [ ] Jenkins/GitHub acceso

### Configuración Local
- [ ] Clonar repositorio
- [ ] Crear `.venv`
- [ ] Instalar dependencias
- [ ] Ejecutar `python validate_pipeline.py`

### CI/CD
- [ ] Configurar credenciales
- [ ] Crear pipeline job
- [ ] Ejecutar primer build
- [ ] Verificar reportes

### Validación
- [ ] Tests pasan (32/32)
- [ ] BDD pasa (12/12)
- [ ] Docker build exitoso
- [ ] Pipeline verde ✅

---

## 📞 Ayuda Rápida

**Ver documentación completa**: `README.md`
**Detalles del pipeline**: `PIPELINE.md`
**Resumen del proyecto**: `RESUMEN.md`

**Ejecutar validación**: `python validate_pipeline.py`

---

## ✅ Listo!

Si todo lo anterior funciona, tu CI/CD está **100% configurado** y listo para usar! 🎉
