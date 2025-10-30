pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/JacoboLS04/retos_docker.git'
        BRANCH = 'taller-observabilidad'
        GO111MODULE = 'on'
    }

    stages {
        stage('Setup and Build') {
            agent {
                node {
                    label 'built-in'  // Usar el nodo built-in de Jenkins
                }
            }
            tools {
                go 'go-1.21'  // Usar la instalación de Go configurada en Jenkins
            }
            steps {
                // Limpiar workspace antes de clonar
                cleanWs()
                // Clonar específicamente el repositorio y rama
                git branch: env.BRANCH,
                    url: env.REPO_URL,
                    credentialsId: 'github-credentials'
            }
        }

            stage('Verify Go') {
                steps {
                    // Verificar versión de Go y configuración
                    sh 'go version'
                    sh 'go env'
                }
            }

            stage('Unit Tests') {
                steps {
                    dir('retos_observabilidad/monitoring-service') {
                        // Descargar dependencias y ejecutar pruebas unitarias
                        sh 'go mod tidy'
                        sh 'go test -v -coverprofile=coverage.out ./... -run "^Test[^Integration]"'
                        sh 'go tool cover -html=coverage.out -o unit-coverage.html'
                    }
                }
            }

            stage('Integration Tests') {
                steps {
                    dir('retos_observabilidad/monitoring-service') {
                        // Ejecutar las pruebas de integración
                        sh 'go test -v -tags=integration -coverprofile=integration-coverage.out ./... -run "^TestIntegration"'
                        sh 'go tool cover -html=integration-coverage.out -o integration-coverage.html'
                    }
                }
            }

            stage('Publish Results') {
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