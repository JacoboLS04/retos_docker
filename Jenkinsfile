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
                    sh 'go version'
                    sh 'go mod tidy'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    sh '''
                        go test -v -coverprofile=coverage.out ./... -run "^Test[^Integration]"
                        go tool cover -html=coverage.out -o unit-coverage.html
                    '''
                }
            }
        }

        stage('Integration Tests') {
            steps {
                dir('retos_observabilidad/monitoring-service') {
                    sh '''
                        go test -v -tags=integration -coverprofile=integration-coverage.out ./... -run "^TestIntegration"
                        go tool cover -html=integration-coverage.out -o integration-coverage.html
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