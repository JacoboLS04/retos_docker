// Declarative Jenkins pipeline to test the api-gateway microservice
pipeline {
  agent any

  tools {
    nodejs 'NodeJS'
  }

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

    stage('Install') {
      steps {
        echo '=== Installing dependencies ==='
        sh 'node --version && npm --version'
        sh 'npm install --production=false'
      }
    }

    stage('Unit Tests') {
      steps {
        echo '=== Running Jest unit tests ==='
        sh 'touch .localstorage'
        // Provide a localstorage file to satisfy Node 25 experimental webstorage, override NODE_OPTIONS entirely
        sh 'NODE_OPTIONS="--localstorage-file=.localstorage" NODE_ENV=test node -r ./jest-environment.js ./node_modules/jest/bin/jest.js --config=jest.config.js --runInBand --reporters=default --reporters=jest-junit --no-coverage'
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
        sh 'NODE_OPTIONS="--localstorage-file=.localstorage" NODE_ENV=test ./node_modules/.bin/cucumber-js --format json:reports/cucumber-report.json --format html:reports/cucumber-report.html'
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

  post {
    always {
      echo 'Pipeline complete'
    }
  }
}
