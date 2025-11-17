#!/bin/bash
# Script para ejecutar tests BDD con Allure en Linux/Mac

echo "============================================================"
echo "  🥒 Ejecutando Tests BDD con Allure"
echo "============================================================"
echo ""

# Activar entorno virtual
source .venv/bin/activate

# Limpiar reportes anteriores
rm -rf allure-results
rm -rf allure-report
rm -f reports/TESTS-*.xml

echo "🧹 Reportes anteriores limpiados"
echo ""

# Ejecutar tests con Allure formatter
echo "🧪 Ejecutando tests BDD..."
behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results --junit --junit-directory reports/

if [ $? -ne 0 ]; then
    echo "❌ Tests fallaron"
    exit 1
fi

echo "✅ Tests BDD completados"
echo ""

# Verificar si allure está instalado
if ! command -v allure &> /dev/null; then
    echo "⚠️  Allure CLI no está instalado"
    echo "📝 Los resultados están en: allure-results/"
    echo ""
    echo "Para instalar Allure CLI:"
    echo "  Mac: brew install allure"
    echo "  Linux: ver https://docs.qameta.io/allure/"
    echo ""
    echo "Luego ejecutar: allure serve allure-results"
    exit 0
fi

# Generar y abrir reporte
echo "📊 Generando reporte Allure..."
allure generate allure-results --clean -o allure-report

echo ""
echo "✅ Reporte generado en: allure-report/"
echo ""
echo "🌐 Abriendo reporte en navegador..."
allure open allure-report
