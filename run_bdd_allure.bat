@echo off
REM Script para ejecutar tests BDD con Allure en Windows

echo ============================================================
echo   🥒 Ejecutando Tests BDD con Allure
echo ============================================================
echo.

REM Activar entorno virtual
call .venv\Scripts\activate.bat

REM Limpiar reportes anteriores
if exist allure-results rmdir /s /q allure-results
if exist allure-report rmdir /s /q allure-report
if exist reports\TESTS-*.xml del /q reports\TESTS-*.xml

echo 🧹 Reportes anteriores limpiados
echo.

REM Ejecutar tests con Allure formatter
echo 🧪 Ejecutando tests BDD...
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results --junit --junit-directory reports/

if %errorlevel% neq 0 (
    echo ❌ Tests fallaron
    pause
    exit /b 1
)

echo ✅ Tests BDD completados
echo.

REM Verificar si allure está instalado
where allure >nul 2>nul
if %errorlevel% neq 0 (
    echo ⚠️  Allure CLI no está instalado
    echo 📝 Los resultados están en: allure-results/
    echo.
    echo Para instalar Allure CLI:
    echo   1. Instalar Scoop: https://scoop.sh/
    echo   2. Ejecutar: scoop install allure
    echo.
    echo Luego ejecutar: allure serve allure-results
    pause
    exit /b 0
)

REM Generar y abrir reporte
echo 📊 Generando reporte Allure...
allure generate allure-results --clean -o allure-report

echo.
echo ✅ Reporte generado en: allure-report/
echo.
echo 🌐 Abriendo reporte en navegador...
allure open allure-report

pause
