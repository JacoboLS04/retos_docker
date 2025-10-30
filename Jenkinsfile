pipeline {
    agent any

    environment {
        GO_VERSION = 'go1.21'
        REPO_URL = 'https://github.com/JacoboLS04/retos_docker.git'
        BRANCH = 'taller-observabilidad'
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

        stage('Setup Go') {
            steps {
                script {
                    // Asegurarse de que Go esté instalado
                    sh 'go version || true'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    script {
                        // Ejecutar las pruebas unitarias con cobertura
                        sh '''
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
                    // Publicar los resultados de las pruebas unitarias
                    publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '.',
                        reportFiles: 'unit-coverage.html',
                        reportName: 'Unit Test Coverage Report'
                    ])
                    
                    // Publicar los resultados de las pruebas de integración
                    publishHTML(target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: '.',
                        reportFiles: 'integration-coverage.html',
                        reportName: 'Integration Test Coverage Report'
                    ])
                }
            }
        }
    }

    post {
        always {
            // Limpiar el workspace después de la ejecución
            cleanWs()
        }
        success {
            echo 'Pipeline ejecutado exitosamente!'
        }
        failure {
            echo 'El pipeline ha fallado'
        }
    }
}