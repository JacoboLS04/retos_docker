pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
    }

    tools {
        go 'go-1.21'
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
                        
                        # No usar || echo aquí para que falle si no hay pruebas
                        TMPDIR="$PWD/test_output" go test -v -coverprofile=coverage.out .
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
                        
                        # No usar || echo aquí para que falle si no hay pruebas
                        TMPDIR="$PWD/test_output" go test -v -tags=integration -coverprofile=integration-coverage.out .
                        if [ -f integration-coverage.out ]; then
                            go tool cover -html=integration-coverage.out -o integration-coverage.html
                        fi
                    '''
                }
            }
        }

        stage('Coverage Report') {
            steps {
                dir('monitoring-service') {
                    script {
                        def hasReports = sh(script: '''
                            if [ -f unit-coverage.html ] || [ -f integration-coverage.html ]; then
                                echo "true"
                            else
                                echo "false"
                            fi
                        ''', returnStdout: true).trim()

                        if (hasReports == 'true') {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: '.',
                                reportFiles: 'unit-coverage.html,integration-coverage.html',
                                reportName: 'Test Coverage Reports'
                            ])
                        } else {
                            error 'No coverage reports were generated. Tests may have failed or no Go files were found.'
                        }
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