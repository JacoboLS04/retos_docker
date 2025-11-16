pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
    }

    tools {
        go 'go-1.21'
        maven 'Maven 3.9'
        jdk 'JDK 17'
    }

    environment {
        GO111MODULE = 'on'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup') {
            steps {
                script {
                    echo "Checking workspace structure..."
                    sh 'pwd && ls -la'
                }
                dir('monitoring-service') {
                    sh '''
                        echo "Verificando estructura del directorio:"
                        pwd
                        ls -la
                        echo "Verificando entorno Go..."
                        go version
                        
                        # Verifica si existen los archivos Go
                        if [ ! -f "main.go" ] || [ ! -f "main_test.go" ] || [ ! -f "integration_test.go" ]; then
                            echo "ERROR: No se encontraron los archivos Go necesarios"
                            exit 1
                        fi
                        
                        # Crear directorio temporal para los tests
                        mkdir -p test_output
                        
                        echo "Instalando herramientas para reportes JUnit (go-junit-report)..."
                        go install github.com/jstemmer/go-junit-report/v2@latest
                        echo "go-junit-report instalado en: $HOME/go/bin/go-junit-report"
                        
                        echo "Inicializando módulo Go..."
                        go mod init monitoring-service || true
                        echo "Descargando dependencias..."
                        go mod tidy
                        echo "Listando módulos..."
                        go list -m all
                    '''
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('monitoring-service') {
                    sh '''
                        echo "Ejecutando pruebas unitarias..."
                        # Configurar el directorio de salida para los tests
                        export TARGETS_FILE="$PWD/test_output/targets.json"
                        
                        # Ejecutar pruebas y generar reporte JUnit
                        set -o pipefail
                        TMPDIR="$PWD/test_output" go test -v -coverprofile=coverage.out . | tee unit-test.out
                        "$HOME/go/bin/go-junit-report" -set-exit-code < unit-test.out > unit-junit.xml
                        if [ -f coverage.out ]; then
                            go tool cover -html=coverage.out -o unit-coverage.html
                        fi
                    '''
                }
            }
        }

        stage('Integration Tests') {
            steps {
                dir('monitoring-service') {
                    sh '''
                        echo "Ejecutando pruebas de integración..."
                        # Configurar el directorio de salida para los tests
                        export TARGETS_FILE="$PWD/test_output/targets.json"
                        
                        # Ejecutar pruebas y generar reporte JUnit
                        set -o pipefail
                        TMPDIR="$PWD/test_output" go test -v -tags=integration -coverprofile=integration-coverage.out . | tee integration-test.out
                        "$HOME/go/bin/go-junit-report" -set-exit-code < integration-test.out > integration-junit.xml
                        if [ -f integration-coverage.out ]; then
                            go tool cover -html=integration-coverage.out -o integration-coverage.html
                        fi
                    '''
                }
            }
        }

        stage('Java Build & Test') {
            steps {
                script {
                    echo "Compilando proyecto Java Spring Boot..."
                    sh 'mvn clean compile'
                    
                    echo "Ejecutando tests de Cucumber con generación de reportes Allure..."
                    sh 'mvn test'
                }
            }
        }

        stage('Publish Reports') {
            steps {
                script {
                    // Publicar JUnit para Go tests
                    dir('monitoring-service') {
                        def haveJUnit = fileExists('unit-junit.xml') || fileExists('integration-junit.xml')
                        if (haveJUnit) {
                            junit allowEmptyResults: true, testResults: 'unit-junit.xml, integration-junit.xml'
                        } else {
                            echo 'No se encontraron reportes JUnit de Go.'
                        }

                        // Publicar cobertura HTML de Go
                        def hasCoverage = fileExists('unit-coverage.html') || fileExists('integration-coverage.html')
                        if (hasCoverage) {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: '.',
                                reportFiles: 'unit-coverage.html,integration-coverage.html',
                                reportName: 'Go Test Coverage Reports'
                            ])
                        } else {
                            echo 'No se generaron reportes de cobertura HTML de Go.'
                        }
                    }

                    // Publicar reportes de Cucumber HTML
                    def cucumberHtmlExists = fileExists('target/cucumber-reports/cucumber-report.html')
                    if (cucumberHtmlExists) {
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'target/cucumber-reports',
                            reportFiles: 'cucumber-report.html',
                            reportName: 'Cucumber HTML Report'
                        ])
                    }

                    // Publicar reportes Allure (Java/Cucumber)
                    def allureResultsExist = fileExists('target/allure-results')
                    if (allureResultsExist) {
                        allure([
                            includeProperties: false,
                            jdk: '',
                            results: [[path: 'target/allure-results']]
                        ])
                    } else {
                        echo 'No se encontraron resultados Allure para Java/Cucumber.'
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Limpiando workspace...'
            deleteDir()
        }
    }
}