pipeline {
    agent any
    
    environment {
        // Python environment
        PYTHON_VERSION = '3.12'
        VENV_DIR = '.venv'
        
        // Docker configuration
        DOCKER_IMAGE = 'ms-notifications'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_REGISTRY = credentials('docker-registry-url') // Configure in Jenkins
        
        // Service configuration
        SERVICE_VERSION = "${env.BUILD_NUMBER}"
        
        // RabbitMQ configuration (for integration tests)
        RABBIT_HOST = 'localhost'
        NOTIFICATION_EVENTS_QUEUE = 'notification.events.queue'
        
        // Database configuration (for integration tests)
        DB_HOST = 'localhost'
        DB_PORT = '5432'
        DB_NAME = 'notifications_test_db'
        DB_USER = 'test_user'
        DB_PASS = 'test_password'
        
        // External services (mock credentials for testing)
        SENDGRID_API_KEY = credentials('sendgrid-api-key')
        SENDER_EMAIL = 'londgav01@gmail.com'
        TWILIO_ACCOUNT_SID = credentials('twilio-account-sid')
        TWILIO_AUTH_TOKEN = credentials('twilio-auth-token')
        TWILIO_PHONE = credentials('twilio-phone-number')
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
                checkout([
                    $class: 'GitSCM',
                    branches: scm.branches,
                    extensions: [
                        [$class: 'CloneOption', depth: 1, noTags: false, shallow: true],
                        [$class: 'CheckoutOption', timeout: 10]
                    ],
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
                script {
                    env.GIT_COMMIT_SHORT = bat(
                        script: "@git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Setup Python Environment') {
            steps {
                echo 'Setting up Python virtual environment...'
                bat '''
                    python --version
                    if exist %VENV_DIR% rmdir /s /q %VENV_DIR%
                    python -m venv %VENV_DIR%
                    call %VENV_DIR%\\Scripts\\activate.bat
                    python -m pip install --upgrade pip
                '''
            }
        }
        
        stage('Install Dependencies') {
            steps {
                echo 'Installing project dependencies...'
                bat '''
                    call %VENV_DIR%\\Scripts\\activate.bat
                    pip install -r requirements.txt
                    pip install -r requirements-dev.txt
                    pip install allure-behave
                '''
            }
        }
        
        stage('Code Quality & Linting') {
            parallel {
                stage('Lint with Flake8') {
                    steps {
                        echo 'Running Flake8 linting...'
                        bat '''
                            call %VENV_DIR%\\Scripts\\activate.bat
                            pip install flake8
                            flake8 . --count --select=E9,F63,F7,F82 --show-source --statistics || exit 0
                            flake8 . --count --exit-zero --max-complexity=10 --max-line-length=127 --statistics
                        '''
                    }
                }
                
                stage('Code Formatting Check') {
                    steps {
                        echo 'Checking code formatting with Black...'
                        bat '''
                            call %VENV_DIR%\\Scripts\\activate.bat
                            pip install black
                            black --check . || exit 0
                        '''
                    }
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo 'Running unit tests with pytest...'
                bat '''
                    call %VENV_DIR%\\Scripts\\activate.bat
                    pytest tests/ -v --tb=short --junit-xml=reports/junit-unit-tests.xml --cov=. --cov-report=xml:reports/coverage.xml --cov-report=html:reports/coverage-html --cov-report=term
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
                bat '''
                    call %VENV_DIR%\\Scripts\\activate.bat
                    if exist allure-results rmdir /s /q allure-results
                    behave features/ -f allure_behave.formatter:AllureFormatter -o allure-results --junit --junit-directory reports/
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
                        bat '''
                            call %VENV_DIR%\\Scripts\\activate.bat
                            pip install safety
                            safety check --json || exit 0
                        '''
                    }
                }
                
                stage('Bandit Security Scan') {
                    steps {
                        echo 'Running Bandit security scanner...'
                        bat '''
                            call %VENV_DIR%\\Scripts\\activate.bat
                            pip install bandit
                            bandit -r . -f json -o reports/bandit-report.json || exit 0
                        '''
                    }
                }
            }
        }
        
        stage('🐳 Build Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                script {
                    bat """
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
                    bat """
                        docker run --rm ${DOCKER_IMAGE}:${DOCKER_TAG} python -c "import app; print('Container test passed')"
                    """
                }
            }
        }
        
        stage('📤 Push Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                echo '📤 Pushing Docker image to registry...'
                script {
                    bat """
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
                branch 'develop'
            }
            steps {
                echo '🚀 Deploying to Development environment...'
                script {
                    // Add your deployment logic here
                    // Example: kubectl apply, docker-compose, etc.
                    bat """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to development
                    """
                }
            }
        }
        
        stage('🚀 Deploy to Staging') {
            when {
                branch 'main'
            }
            steps {
                echo '🚀 Deploying to Staging environment...'
                input message: 'Deploy to Staging?', ok: 'Deploy'
                script {
                    bat """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to staging
                    """
                }
            }
        }
        
        stage('🚀 Deploy to Production') {
            when {
                buildingTag()
            }
            steps {
                echo '🚀 Deploying to Production environment...'
                input message: 'Deploy to Production?', ok: 'Deploy', submitter: 'admin'
                script {
                    bat """
                        echo Deploying ${DOCKER_IMAGE}:${DOCKER_TAG} to production
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Cleaning up workspace...'
            cleanWs()
        }
        
        success {
            echo '✅ Pipeline completed successfully!'
            // Add notification logic (Slack, Email, etc.)
        }
        
        failure {
            echo '❌ Pipeline failed!'
            // Add notification logic for failures
        }
        
        unstable {
            echo '⚠️ Pipeline is unstable!'
        }
    }
}
