// Declarative Jenkins pipeline to test the api-gateway microservice
pipeline {
  agent any

  environment {
    CI = 'true'
    PORT = '3001'
    JEST_JUNIT_OUTPUT = 'junit.xml'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build and Test') {
      agent {
        docker {
          image 'node:20'
          args '-u root:root'
          reuseNode true
        }
      }
      stages {
        stage('Install') {
          steps {
            echo '=== Installing dependencies ==='
            sh 'node --version && npm --version'
            sh 'npm ci'
          }
        }

        stage('Unit Tests') {
          steps {
            echo '=== Running Jest unit tests ==='
            sh 'npx jest --ci --reporters=default --reporters=jest-junit'
          }
          post {
            always {
              junit allowEmptyResults: true, testResults: 'junit.xml'
            }
          }
        }

        stage('BDD Tests') {
          steps {
            echo '=== Running Cucumber BDD tests ==='
            sh 'mkdir -p reports'
            sh 'npm run test:bdd -- --format json:reports/cucumber-report.json --format html:reports/cucumber-report.html'
          }
          post {
            always {
              script {
                if (fileExists('reports/cucumber-report.html')) {
                  archiveArtifacts artifacts: 'reports/cucumber-report.html', allowEmptyArchive: true
                }
              }
            }
          }
        }

        stage('Start Service') {
          steps {
            echo '=== Starting api-gateway for health check ==='
            sh 'nohup npm start > /tmp/gateway.log 2>&1 & sleep 3'
          }
        }

        stage('Health Check') {
          steps {
            echo '=== Checking /health endpoint ==='
            sh '''
              for i in 1 2 3 4 5; do
                if curl -sSf http://localhost:${PORT}/health -m 2; then
                  echo "Health check passed"
                  exit 0
                fi
                echo "Retry $i/5..."
                sleep 2
              done
              echo "Health check failed"
              tail -n 100 /tmp/gateway.log || true
              exit 1
            '''
          }
        }

        stage('Cleanup') {
          steps {
            echo '=== Cleanup ==='
            sh 'tail -n 50 /tmp/gateway.log || true'
            sh "pkill -f 'node src/index.js' || true"
          }
        }
      }
    }
  }

  post {
    always {
      echo 'Pipeline complete'
    }
  }
}
