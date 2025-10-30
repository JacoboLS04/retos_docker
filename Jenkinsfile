pipeline {
    agent any

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
                    // Publicar los resultados de las pruebas (ejecutar dentro de node)
                    script {
                        node {
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
        }
    }

    post {
        always {
            // Limpiar el workspace después de la ejecución (asegurar node context)
            script {
                node {
                    cleanWs()
                }
            }
        }
        success {
            echo 'Pipeline ejecutado exitosamente!'
        }
        failure {
            echo 'El pipeline ha fallado'
        }
    }
}