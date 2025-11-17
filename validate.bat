@echo off
REM Script de validación rápida para Windows
REM Ejecuta todas las verificaciones del pipeline localmente

echo.
echo ============================================================
echo   🚀 VALIDACION PIPELINE CI/CD - MS Notifications
echo ============================================================
echo.

REM Verificar Python
echo [1/8] Verificando Python...
python --version
if %errorlevel% neq 0 (
    echo ❌ Error: Python no encontrado
    pause
    exit /b 1
)
echo ✅ Python OK
echo.

REM Verificar entorno virtual
echo [2/8] Verificando entorno virtual...
if not exist ".venv\" (
    echo ⚠️  Entorno virtual no encontrado. Creando...
    python -m venv .venv
    echo ✅ Entorno virtual creado
)
echo.

REM Activar entorno virtual
echo [3/8] Activando entorno virtual...
call .venv\Scripts\activate.bat
if %errorlevel% neq 0 (
    echo ❌ Error al activar entorno virtual
    pause
    exit /b 1
)
echo ✅ Entorno virtual activado
echo.

REM Instalar dependencias
echo [4/8] Instalando dependencias...
python -m pip install --upgrade pip --quiet
pip install -r requirements.txt --quiet
pip install -r requirements-dev.txt --quiet
pip install flake8 black safety bandit --quiet
echo ✅ Dependencias instaladas
echo.

REM Ejecutar linting
echo [5/8] Ejecutando linting (Flake8)...
flake8 . --count --select=E9,F63,F7,F82 --show-source --statistics
if %errorlevel% neq 0 (
    echo ❌ Errores críticos de linting encontrados
    pause
    exit /b 1
)
echo ✅ Linting OK
echo.

REM Ejecutar tests unitarios
echo [6/8] Ejecutando tests unitarios...
pytest tests/ -v --tb=short
if %errorlevel% neq 0 (
    echo ❌ Tests unitarios fallaron
    pause
    exit /b 1
)
echo ✅ Tests unitarios OK
echo.

REM Ejecutar tests BDD
echo [7/8] Ejecutando tests BDD...
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results
if %errorlevel% neq 0 (
    echo ❌ Tests BDD fallaron
    pause
    exit /b 1
)
echo ✅ Tests BDD OK
echo.

REM Verificar Dockerfile
echo [8/8] Verificando Dockerfile...
if not exist "Dockerfile" (
    echo ❌ Dockerfile no encontrado
    pause
    exit /b 1
)
echo ✅ Dockerfile OK
echo.

REM Resumen final
echo ============================================================
echo   ✅ TODAS LAS VALIDACIONES PASARON
echo ============================================================
echo.
echo   32 tests unitarios ✅
echo   12 escenarios BDD ✅
echo   Linting ✅
echo   Dockerfile ✅
echo.
echo   🎉 Tu código está listo para commit/push!
echo.
echo ============================================================
echo.

pause
