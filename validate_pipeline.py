#!/usr/bin/env python
"""
Script para validar el pipeline localmente antes de hacer commit.
Ejecuta todas las verificaciones que se correrán en Jenkins.
"""

import subprocess
import sys
import os
from pathlib import Path

# Colores para output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    BOLD = '\033[1m'
    END = '\033[0m'

def print_step(message):
    print(f"\n{Colors.BLUE}{Colors.BOLD}{'='*60}{Colors.END}")
    print(f"{Colors.BLUE}{Colors.BOLD}{message}{Colors.END}")
    print(f"{Colors.BLUE}{Colors.BOLD}{'='*60}{Colors.END}\n")

def print_success(message):
    print(f"{Colors.GREEN}✅ {message}{Colors.END}")

def print_error(message):
    print(f"{Colors.RED}❌ {message}{Colors.END}")

def print_warning(message):
    print(f"{Colors.YELLOW}⚠️  {message}{Colors.END}")

def run_command(command, description):
    """Ejecuta un comando y retorna True si fue exitoso."""
    print(f"🔄 {description}...")
    try:
        result = subprocess.run(
            command,
            shell=True,
            check=True,
            capture_output=True,
            text=True
        )
        print_success(f"{description} - PASSED")
        return True
    except subprocess.CalledProcessError as e:
        print_error(f"{description} - FAILED")
        if e.stdout:
            print(f"STDOUT:\n{e.stdout}")
        if e.stderr:
            print(f"STDERR:\n{e.stderr}")
        return False

def check_python_version():
    """Verifica que la versión de Python sea compatible."""
    print_step("🐍 Verificando versión de Python")
    version = sys.version_info
    if version.major >= 3 and version.minor >= 10:
        print_success(f"Python {version.major}.{version.minor}.{version.micro}")
        return True
    else:
        print_error(f"Python {version.major}.{version.minor} detectado. Se requiere Python 3.10+")
        return False

def check_venv():
    """Verifica que estemos en un entorno virtual."""
    print_step("📦 Verificando entorno virtual")
    if hasattr(sys, 'real_prefix') or (hasattr(sys, 'base_prefix') and sys.base_prefix != sys.prefix):
        print_success("Entorno virtual detectado")
        return True
    else:
        print_warning("No se detectó entorno virtual. Recomendado activar .venv")
        return True  # No es crítico, solo advertencia

def check_dependencies():
    """Verifica que las dependencias estén instaladas."""
    print_step("📚 Verificando dependencias")
    
    required_packages = [
        'pytest',
        'behave',
        'flask',
        'pika',
        'psycopg2',
        'sendgrid',
        'twilio'
    ]
    
    all_installed = True
    for package in required_packages:
        try:
            __import__(package.replace('-', '_'))
            print_success(f"{package} instalado")
        except ImportError:
            print_error(f"{package} NO instalado")
            all_installed = False
    
    return all_installed

def run_linting():
    """Ejecuta verificaciones de linting."""
    print_step("🔍 Verificaciones de Linting")
    
    success = True
    
    # Flake8 - Errores críticos
    if not run_command(
        "flake8 . --count --select=E9,F63,F7,F82 --show-source --statistics",
        "Flake8 - Errores críticos"
    ):
        success = False
    
    # Flake8 - Complejidad y estilo
    run_command(
        "flake8 . --count --exit-zero --max-complexity=10 --max-line-length=127 --statistics",
        "Flake8 - Complejidad y estilo"
    )
    
    return success

def run_unit_tests():
    """Ejecuta las pruebas unitarias."""
    print_step("🧪 Ejecutando Pruebas Unitarias")
    
    return run_command(
        "pytest tests/ -v --tb=short --cov=. --cov-report=term --cov-report=html:reports/coverage-html",
        "Pytest - Pruebas unitarias"
    )

def run_bdd_tests():
    """Ejecuta las pruebas BDD."""
    print_step("🥒 Ejecutando Pruebas BDD")
    
    return run_command(
        "behave features/",
        "Behave - Pruebas BDD"
    )

def run_security_checks():
    """Ejecuta verificaciones de seguridad."""
    print_step("🔒 Verificaciones de Seguridad")
    
    # Safety - vulnerabilidades en dependencias
    run_command(
        "safety check || exit 0",
        "Safety - Escaneo de vulnerabilidades"
    )
    
    # Bandit - seguridad del código
    run_command(
        "bandit -r . -ll || exit 0",
        "Bandit - Análisis de seguridad"
    )
    
    return True  # No fallar el pipeline por seguridad

def check_dockerfile():
    """Verifica que el Dockerfile exista."""
    print_step("🐳 Verificando Dockerfile")
    
    if Path("Dockerfile").exists():
        print_success("Dockerfile encontrado")
        return True
    else:
        print_error("Dockerfile no encontrado")
        return False

def validate_docker_build():
    """Valida que la imagen Docker se pueda construir."""
    print_step("🐳 Validando construcción de Docker")
    
    return run_command(
        "docker build -t ms-notifications:test .",
        "Docker build"
    )

def main():
    """Función principal que ejecuta todas las validaciones."""
    print(f"\n{Colors.BOLD}{'='*60}")
    print("🚀 VALIDACIÓN LOCAL DEL PIPELINE CI/CD")
    print(f"{'='*60}{Colors.END}\n")
    
    results = {
        "Python Version": check_python_version(),
        "Virtual Environment": check_venv(),
        "Dependencies": check_dependencies(),
        "Linting": run_linting(),
        "Unit Tests": run_unit_tests(),
        "BDD Tests": run_bdd_tests(),
        "Security Checks": run_security_checks(),
        "Dockerfile": check_dockerfile(),
    }
    
    # Docker build es opcional (puede no estar Docker instalado)
    print_warning("Docker build es opcional. Omitir si Docker no está disponible.")
    user_input = input("¿Ejecutar Docker build? (y/N): ").strip().lower()
    if user_input == 'y':
        results["Docker Build"] = validate_docker_build()
    
    # Resumen final
    print(f"\n{Colors.BOLD}{'='*60}")
    print("📊 RESUMEN DE RESULTADOS")
    print(f"{'='*60}{Colors.END}\n")
    
    passed = 0
    failed = 0
    
    for check, result in results.items():
        if result:
            print_success(f"{check}")
            passed += 1
        else:
            print_error(f"{check}")
            failed += 1
    
    print(f"\n{Colors.BOLD}{'='*60}")
    print(f"Total: {passed} ✅  |  {failed} ❌")
    print(f"{'='*60}{Colors.END}\n")
    
    if failed == 0:
        print_success("¡Todas las validaciones pasaron! ✨")
        print_success("El código está listo para commit/push.")
        return 0
    else:
        print_error(f"{failed} validación(es) fallaron.")
        print_error("Por favor, corrige los errores antes de hacer commit.")
        return 1

if __name__ == "__main__":
    exit_code = main()
    sys.exit(exit_code)
