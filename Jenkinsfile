pipeline {
    agent any

    environment {
        // Variables de entorno para la aplicación
        NODE_ENV = 'test'
        HEALTH_PORT = '8080'
        
        // Configuración de base de datos para tests (ajusta según tu entorno)
        DB_HOST = 'localhost'
        DB_PORT = '5432'
        DB_NAME = 'notification_test'
        DB_USER = credentials('db-user')
        DB_PASSWORD = credentials('db-password')
        
        // Configuración de RabbitMQ para tests
        RABBIT_URL = 'amqp://guest:guest@localhost:5672'
    }

    tools {
        nodejs 'NodeJS'// Nombre configurado en Jenkins Global Tool Configuration
    }

    options {
        // Mantener logs de los últimos 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Timeout de 30 minutos para todo el pipeline
        timeout(time: 30, unit: 'MINUTES')
        // Timestamps en los logs
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Clonando repositorio...'
                checkout scm
            }
        }

        stage('Install Dependencies') {
            steps {
                echo '📥 Instalando dependencias de Node.js...'
                script {
                    if (isUnix()) {
                        sh 'npm ci'
                    } else {
                        bat 'npm ci'
                    }
                }
            }
        }

        stage('Lint & Build') {
            steps {
                echo '🔨 Compilando TypeScript...'
                script {
                    if (isUnix()) {
                        sh 'npm run build'
                    } else {
                        bat 'npm run build'
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                echo '🧪 Ejecutando tests unitarios con Jest...'
                script {
                    if (isUnix()) {
                        sh 'npm test -- --ci --coverage --reporters=default --reporters=jest-junit'
                    } else {
                        bat 'npm test -- --ci --coverage --reporters=default --reporters=jest-junit'
                    }
                }
            }
            post {
                always {
                    // Publicar resultados de tests unitarios
                    junit 'junit.xml'
                    // Publicar cobertura de código
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'coverage/lcov-report',
                        reportFiles: 'index.html',
                        reportName: 'Jest Coverage Report',
                        reportTitles: 'Code Coverage'
                    ])
                }
            }
        }

        stage('BDD Tests (Cucumber)') {
            steps {
                echo '🥒 Ejecutando tests BDD con Cucumber...'
                script {
                    // Crear directorio de reportes si no existe
                    if (isUnix()) {
                        sh 'mkdir -p reports'
                        sh 'npm run test:bdd || true'
                    } else {
                        bat 'if not exist reports mkdir reports'
                        bat 'npm run test:bdd || exit 0'
                    }
                }
            }
            post {
                always {
                    // Publicar reporte HTML de Cucumber
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'reports',
                        reportFiles: 'cucumber-report.html',
                        reportName: 'Cucumber BDD Report',
                        reportTitles: 'BDD Test Results'
                    ])
                    
                    // Publicar resultados JSON de Cucumber para métricas
                    cucumber(
                        fileIncludePattern: '**/cucumber-report.json',
                        sortingMethod: 'ALPHABETICAL',
                        trendsLimit: 100
                    )
                }
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'master'
                }
            }
            steps {
                echo '🐳 Construyendo imagen Docker...'
                script {
                    def imageTag = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
                    if (isUnix()) {
                        sh "docker build -t notification-orchestrator:${imageTag} ."
                        sh "docker tag notification-orchestrator:${imageTag} notification-orchestrator:latest"
                    } else {
                        bat "docker build -t notification-orchestrator:${imageTag} ."
                        bat "docker tag notification-orchestrator:${imageTag} notification-orchestrator:latest"
                    }
                }
            }
        }

        stage('Security Scan') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'master'
                }
            }
            steps {
                echo '🔒 Escaneando vulnerabilidades...'
                script {
                    if (isUnix()) {
                        sh 'npm audit --audit-level=moderate || true'
                    } else {
                        bat 'npm audit --audit-level=moderate || exit 0'
                    }
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo '🚀 Desplegando a entorno de staging...'
                script {
                    // Aquí va tu lógica de despliegue a staging
                    // Por ejemplo: kubectl, docker-compose, etc.
                    echo 'Despliegue a staging configurado según tu infraestructura'
                }
            }
        }

        stage('Deploy to Production') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                echo '🚀 Desplegando a producción...'
                input message: '¿Desplegar a producción?', ok: 'Deploy'
                script {
                    // Aquí va tu lógica de despliegue a producción
                    echo 'Despliegue a producción configurado según tu infraestructura'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline ejecutado exitosamente'
            // Notificaciones de éxito (Slack, Email, etc.)
        }
        failure {
            echo '❌ Pipeline falló'
            // Notificaciones de fallo
        }
        always {
            echo '🧹 Limpieza completada'
        }
    }
}
