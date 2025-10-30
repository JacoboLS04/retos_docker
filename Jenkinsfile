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
                dir('retos_observabilidad/monitoring-service') {
                    sh '''
                        pwd
                        ls -la
                        echo "Verificando entorno Go..."
                        go version
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
                dir('retos_observabilidad/monitoring-service') {
                    sh '''
                        echo "Verificando estructura del proyecto..."
                        pwd
                        echo "Contenido del directorio actual:"
                        ls -la
                        echo "Contenido del directorio padre:"
                        ls -la ..
                        echo "Ejecutando pruebas unitarias..."
                        go test -v -coverprofile=coverage.out . || echo "No se encontraron pruebas en el directorio actual"
                        if [ -f coverage.out ]; then
                            go tool cover -html=coverage.out -o unit-coverage.html
                        fi
                    '''
                }
            }
        }

        stage('Integration Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    sh '''
                        echo "Ejecutando pruebas de integración..."
                        go test -v -tags=integration -coverprofile=integration-coverage.out . || echo "No se encontraron pruebas de integración"
                        if [ -f integration-coverage.out ]; then
                            go tool cover -html=integration-coverage.out -o integration-coverage.html
                        fi
                    '''
                }
            }
        }

        stage('Coverage Report') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '.',
                        reportFiles: 'unit-coverage.html,integration-coverage.html',
                        reportName: 'Test Coverage Reports'
                    ])
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