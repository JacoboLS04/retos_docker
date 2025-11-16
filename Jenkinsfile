// Declarative Jenkins pipeline to test the api-gateway microservice
pipeline {
  agent {
    // Uses Docker agent with Node.js runtime so the pipeline is portable
    docker {
      image 'node:20'
      args '-u root:root'
    }
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
        echo 'Installing npm dependencies (dev dependencies included)'
        sh 'npm ci'
      }
    }

    stage('Unit Tests') {
      steps {
        echo 'Running Jest unit tests with JUnit reporter'
        // Use jest-junit reporter to produce JUnit xml
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
        echo 'Running Cucumber BDD tests (Gherkin scenarios in Spanish)'
        sh 'npm run test:bdd -- --format json:reports/cucumber-report.json --format html:reports/cucumber-report.html'
      }
      post {
        always {
          // Publish Cucumber reports if available
          script {
            if (fileExists('reports/cucumber-report.json')) {
              echo 'Cucumber JSON report generated'
              // Optionally archive the HTML report
              archiveArtifacts artifacts: 'reports/cucumber-report.html', allowEmptyArchive: true
            }
          }
        }
      }
    }

    stage('Start Service') {
      steps {
        echo 'Starting api-gateway in background for health check'
        // Start in background and redirect logs
        sh 'nohup npm start > /tmp/gateway.log 2>&1 & sleep 2'
      }
    }

    stage('Health Check') {
      steps {
        echo 'Waiting for /health to respond (retries)'
        sh '''
        set -e
        for i in 1 2 3 4 5; do
          if curl -sSf http://localhost:${PORT}/health -m 2; then
            echo 'Health OK'
            exit 0
          fi
          echo 'Waiting for service...'
          sleep 2
        done
        echo 'Health check failed'
        cat /tmp/gateway.log || true
        exit 1
        '''
      }
    }
  }

  post {
    always {
      script {
        try {
          echo 'Pipeline finished — printing gateway log (tail)'
          sh 'if [ -f /tmp/gateway.log ]; then tail -n 200 /tmp/gateway.log; fi'
        } catch (Exception e) {
          echo "Could not read gateway log: ${e.message}"
        }
      }
    }
    cleanup {
      script {
        try {
          echo 'Cleanup: kill node processes if any (best-effort)'
          sh "pkill -f 'node src/index.js' || true"
        } catch (Exception e) {
          echo "Cleanup warning: ${e.message}"
        }
      }
    }
  }
}
