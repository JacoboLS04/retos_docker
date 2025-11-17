# 📊 Guía de Integración Allure para Reportes BDD

## 🎯 Descripción

Los tests BDD (Behave) ahora generan reportes en formato Allure, proporcionando visualizaciones interactivas y detalladas de los resultados de las pruebas.

## ✨ Características de Allure

- 📊 **Reportes interactivos** con gráficos y estadísticas
- 📸 **Screenshots** y attachments
- 📝 **Descripción detallada** de cada paso
- 📈 **Tendencias** de ejecución
- 🔍 **Filtros avanzados** por features, tags, status
- ⏱️ **Tiempos de ejecución** detallados
- 📋 **Historial** de ejecuciones

## 🚀 Instalación

### Python Package (Ya incluido)

```bash
pip install allure-behave
```

### Allure CLI (Para generar reportes localmente)

#### Windows (usando Scoop)

```powershell
# Instalar Scoop
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# Instalar Allure
scoop install allure
```

#### Mac

```bash
brew install allure
```

#### Linux

```bash
# Descargar y extraer
wget https://github.com/allure-framework/allure2/releases/download/2.24.1/allure-2.24.1.tgz
tar -zxvf allure-2.24.1.tgz
sudo mv allure-2.24.1 /opt/allure
sudo ln -s /opt/allure/bin/allure /usr/bin/allure
```

## 📝 Uso Local

### Opción 1: Script Automatizado (Recomendado)

**Windows:**
```bash
run_bdd_allure.bat
```

**Linux/Mac:**
```bash
chmod +x run_bdd_allure.sh
./run_bdd_allure.sh
```

Este script:
1. ✅ Limpia reportes anteriores
2. ✅ Ejecuta tests BDD con Allure
3. ✅ Genera el reporte HTML
4. ✅ Abre el reporte en el navegador

### Opción 2: Comandos Manuales

```bash
# Activar entorno virtual
.venv\Scripts\activate  # Windows
source .venv/bin/activate  # Linux/Mac

# Ejecutar tests con Allure
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results

# Generar reporte
allure generate allure-results --clean -o allure-report

# Abrir reporte
allure open allure-report
```

### Opción 3: Servir reporte directamente

```bash
# Servir sin generar (usa resultados existentes)
allure serve allure-results
```

## 🏗️ Integración en Jenkins

### Prerequisitos

1. **Instalar el plugin de Allure** en Jenkins:
   - Manage Jenkins → Plugins → Available
   - Buscar "Allure"
   - Instalar "Allure" plugin

2. **Configurar Allure en Jenkins**:
   - Manage Jenkins → Global Tool Configuration
   - Allure Commandline → Add Allure Commandline
   - Name: `allure`
   - Install automatically: ✅
   - Version: Latest

### Configuración en Jenkinsfile

Ya está configurado en el `Jenkinsfile`:

```groovy
stage('BDD Tests') {
    steps {
        bat '''
            behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results
        '''
    }
    post {
        always {
            allure([
                includeProperties: false,
                jdk: '',
                properties: [],
                reportBuildPolicy: 'ALWAYS',
                results: [[path: 'allure-results']]
            ])
        }
    }
}
```

### Ver Reportes en Jenkins

Después de ejecutar el pipeline:

1. Ir al build específico
2. Click en **"Allure Report"** en el menú lateral
3. Explorar el reporte interactivo

## 📊 Estructura de Reportes Allure

```
allure-results/          # Datos raw (JSON)
├── *-result.json       # Resultados de tests
├── *-container.json    # Contenedores (features)
└── *-attachment.*      # Attachments (screenshots, logs)

allure-report/          # Reporte HTML generado
├── index.html         # Página principal
├── data/             # Datos del reporte
└── assets/           # CSS, JS, imágenes
```

## 🎨 Personalización de Reportes

### Agregar información adicional en tests

Puedes enriquecer los reportes usando decoradores de Allure en los steps:

```python
# features/steps/email_steps.py
import allure

@given('the email service is available')
def step_impl(context):
    with allure.step("Configurar mock de SendGrid"):
        # Setup mock
        pass
    
    allure.attach("Configuración", "SendGrid API Key configurado")
```

### Agregar Screenshots

```python
@then('the email should be sent successfully')
def step_impl(context):
    # Validaciones
    
    # Adjuntar información
    allure.attach(
        json.dumps(context.email_data, indent=2),
        name="Email Data",
        attachment_type=allure.attachment_type.JSON
    )
```

### Categorías y Severidad

```python
from allure_commons.types import Severity

@allure.severity(Severity.CRITICAL)
@given('the email service is available')
def step_impl(context):
    pass
```

## 📈 Métricas Disponibles en Allure

### Overview
- Total scenarios/steps
- Pass/Fail/Skip rates
- Tiempo de ejecución total
- Gráficos de tendencias

### Behaviors
- Organización por Features
- Stories y épicas
- Behaviors tree

### Packages
- Organización por módulos
- Tests por paquete

### Graphs
- Status chart
- Severity chart
- Duration chart
- Retry trend

### Timeline
- Visualización temporal de ejecuciones
- Tests en paralelo
- Duración de cada test

## 🔧 Configuración Avanzada

### behave.ini

```ini
[behave]
format = allure_behave.formatter:AllureFormatter
outfiles = allure-results

# Configuración de Allure
[allure]
clean_allure_dir = true
```

### environment.py

```python
import allure

def before_all(context):
    # Información del ambiente
    allure.environment(
        Environment='Test',
        Python_Version='3.12',
        Service_Version='1.0.0'
    )
```

## 📝 Ejemplo de Reporte

Después de ejecutar los tests, el reporte Allure mostrará:

```
Overview
├── 12 scenarios passed (100%)
├── 45 steps passed (100%)
├── Duration: 0.042s
└── Pass Rate: 100%

Features
├── Email Notification Service (4 scenarios)
├── Health Check Endpoints (4 scenarios)
└── SMS Notification Service (4 scenarios)

Timeline
├── All tests executed sequentially
└── Average duration: 0.003s per scenario
```

## 🎯 Mejores Prácticas

1. **Limpiar resultados anteriores** antes de cada ejecución
2. **Usar tags** para organizar scenarios
3. **Agregar attachments** para debugging
4. **Documentar steps** con descripciones claras
5. **Revisar tendencias** para detectar degradación

## 🐛 Troubleshooting

### Error: "No se encuentra allure command"

```bash
# Verificar instalación
allure --version

# Reinstalar si es necesario
scoop install allure  # Windows
brew install allure   # Mac
```

### Error: "allure-results vacío"

```bash
# Verificar que behave use el formatter
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results
```

### Reporte no se genera

```bash
# Limpiar y regenerar
rm -rf allure-report
allure generate allure-results --clean -o allure-report
```

## 📚 Referencias

- [Allure Documentation](https://docs.qameta.io/allure/)
- [Allure-Behave Integration](https://github.com/allure-framework/allure-python)
- [Behave Documentation](https://behave.readthedocs.io/)

## 🎉 Resultados Actuales

Con la integración de Allure, ahora tienes:

- ✅ Reportes visuales interactivos
- ✅ Gráficos de tendencias
- ✅ Historial de ejecuciones
- ✅ Filtros y búsqueda avanzada
- ✅ Export a PDF/HTML
- ✅ Integración con Jenkins
- ✅ Timeline de ejecución

**¡Los reportes BDD ahora son mucho más profesionales y fáciles de analizar!** 🚀
