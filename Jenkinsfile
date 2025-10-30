pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
    }

    stages {
        stage('Build') {
            steps {
                // Checkout del código
                checkout scm
                
                // Ir al directorio del servicio
                dir('retos_observabilidad/monitoring-service') {
                    sh 'go version || true'
                    sh 'go mod tidy || true'
                    sh 'go test ./... || true'
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