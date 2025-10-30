pipeline {
    agent {
        label 'built-in'  // Usar el nodo built-in de Jenkins
    }

    options {
        skipDefaultCheckout(true)  // Skip automatic checkout
        disableConcurrentBuilds()  // No permitir builds concurrentes
    }

    environment {
        REPO_URL = 'https://github.com/JacoboLS04/retos_docker.git'
        BRANCH = 'taller-observabilidad'
        GO111MODULE = 'on'
        GOROOT = tool 'go-1.21'  // Nombre de la instalación de Go configurada en Jenkins
        PATH = "${env.GOROOT}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                // Limpiar workspace antes de clonar
                cleanWs()
                // Clonar específicamente el repositorio y rama
                git branch: env.BRANCH,
                    url: env.REPO_URL,
                    credentialsId: 'github-credentials'
            }
        }

        stage('Setup') {
            steps {
                script {
                    // Verificar versión de Go y configuración
                    sh '''
                        go version
                        go env
                    '''
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    script {
                        // Descargar dependencias y ejecutar pruebas unitarias
                        sh '''
                            go mod tidy
                            go test -v -coverprofile=coverage.out ./... -run "^Test[^Integration]"
                            go tool cover -html=coverage.out -o unit-coverage.html
                        '''
                    }
                }
            }
        }

        stage('Integration Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    script {
                        // Ejecutar las pruebas de integración
                        sh '''
                            go mod tidy
                            go test -v -tags=integration -coverprofile=integration-coverage.out ./... -run "^TestIntegration"
                            go tool cover -html=integration-coverage.out -o integration-coverage.html
                        '''
                    }
                }
            }
        }

        stage('Test Results') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    // Publicar los resultados de las pruebas
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
            cleanWs()  // Limpiar el workspace después de la ejecución
        }
        success {
            echo 'Pipeline ejecutado exitosamente!'
        }
        failure {
            echo 'El pipeline ha fallado'
        }
    }
}