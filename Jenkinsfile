pipeline {
    agent any
    
    environment {
        PYTHON_VERSION = '3.12'
        VENV_DIR = '.venv'
        DOCKER_IMAGE = 'ms-notifications'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = 'localhost:5000'
        SERVICE_VERSION = "${env.BUILD_NUMBER}"
        RABBIT_HOST = 'localhost'
        NOTIFICATION_EVENTS_QUEUE = 'notification.events.queue'
        DB_HOST = 'localhost'
        DB_PORT = '5432'
        DB_NAME = 'notifications_test_db'
        DB_USER = 'test_user'
        DB_PASS = 'test_password'
        SENDGRID_API_KEY = 'test-api-key'
        SENDER_EMAIL = 'londgav01@gmail.com'
        TWILIO_ACCOUNT_SID = 'test-sid'
        TWILIO_AUTH_TOKEN = 'test-token'
        TWILIO_PHONE = '+1234567890'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Cloning repository...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    echo "Git commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }
        
        stage('Setup Python Environment') {
            steps {
                echo 'Setting up Python virtual environment...'
                sh '''
                    python3 --version
                    pip3 --version
                '''
            }
        }
        
        stage('Install Dependencies') {
            steps {
                echo 'Installing project dependencies...'
                sh '''
                    pip3 install --break-system-packages -r requirements.txt
                    pip3 install --break-system-packages -r requirements-dev.txt
                    pip3 install --break-system-packages allure-behave
                '''
            }
        }
        
        stage('Code Quality & Linting') {
            parallel {
                stage('Lint with Flake8') {
                    steps {
                        echo 'Running Flake8 linting...'
                        sh '''
                            pip3 install --break-system-packages flake8
                            flake8 . --count --select=E9,F63,F7,F82 --show-source --statistics || true
                            flake8 . --count --exit-zero --max-complexity=10 --max-line-length=127 --statistics
                        '''
                    }
                }
                
                stage('Code Formatting Check') {
                    steps {
                        echo 'Checking code formatting with Black...'
                        sh '''
                            pip3 install --break-system-packages black
                            black --check . || true
                        '''
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests with pytest...'
                sh '''
                    mkdir -p reports
                    python3 -m pytest tests/ -v --tb=short --junit-xml=reports/junit-unit-tests.xml --cov=. --cov-report=xml:reports/coverage.xml --cov-report=html:reports/coverage-html --cov-report=term
                '''
            }
            post {
                always {
                    junit 'reports/junit-unit-tests.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'reports/coverage-html',
                        reportFiles: 'index.html',
                        reportName: 'Coverage Report'
                    ])
                }
            }
        }
        
        stage('BDD Tests') {
            steps {
                echo 'Running BDD tests with Behave and Allure...'
                sh '''
                    rm -rf allure-results
                    mkdir -p reports
                    python3 -m behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results --junit --junit-directory reports/
                '''
            }
            post {
                always {
                    junit 'reports/TESTS-*.xml'
                    script {
                        allure([
                            includeProperties: false,
                            jdk: '',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'allure-results']]
                        ])
                    }
                }
            }
        }
        
        stage('Security Scan') {
            parallel {
                stage('Dependency Check') {
                    steps {
                        echo '🔍 Scanning dependencies for vulnerabilities...'
                        sh '''
                            pip3 install --break-system-packages safety
                            safety check --json || true
                        '''
                    }
                }
                
                stage('Bandit Security Scan') {
                    steps {
                        echo 'Running Bandit security scanner...'
                        sh '''
                            pip3 install --break-system-packages bandit
                            mkdir -p reports
                            bandit -r . -f json -o reports/bandit-report.json || true
                        '''
                    }
                }
            }
        }
        
        stage('🐳 Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} -t ${DOCKER_IMAGE}:latest .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:${GIT_COMMIT_SHORT}
                    """
                }
            }
        }
        
        stage('🧪 Container Tests') {
            steps {
                echo '🧪 Testing Docker container...'
                script {
                    sh """
                        docker run --rm ${DOCKER_IMAGE}:${DOCKER_TAG} python3 -c "import app; print('Container test passed')"
                    """
                }
            }
        }
        
        stage('📤 Push Docker Image') {
            when {
                expression { return false } // Disabled by default
            }
            steps {
                echo '📤 Pushing Docker image to registry...'
                script {
                    sh """
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                        docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('🚀 Deploy to Development') {
            when {
                expression { return false } // Disabled by default
            }
            steps {
                echo '🚀 Deploying to Development environment...'
                script {
                    sh """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to development
                    """
                }
            }
        }
        
        stage('🚀 Deploy to Staging') {
            when {
                expression { return false } // Disabled by default
            }
            steps {
                echo '🚀 Deploying to Staging environment...'
                input message: 'Deploy to Staging?', ok: 'Deploy'
                script {
                    sh """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to staging
                    """
                }
            }
        }
        
        stage('🚀 Deploy to Production') {
            when {
                expression { return false } // Disabled by default
            }
            steps {
                echo '🚀 Deploying to Production environment...'
                input message: 'Deploy to Production?', ok: 'Deploy', submitter: 'admin'
                script {
                    sh """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to production
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline completed successfully!'
        }
        
        failure {
            echo '❌ Pipeline failed!'
        }
        
        unstable {
            echo '⚠️ Pipeline is unstable!'
        }
    }
}
