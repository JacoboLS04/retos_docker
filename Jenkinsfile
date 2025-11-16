// Declarative Jenkins pipeline to test the api-gateway microservice
pipeline {
  agent any

  tools {
    nodejs 'NodeJS'
  }

  environment {
    CI = 'true'
    PORT = '3001'
    // Store JUnit XML under reports/junit for clarity
    JEST_JUNIT_OUTPUT = 'reports/junit/junit.xml'
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
        sh 'mkdir -p reports/junit'
        sh 'touch .localstorage'
        // Run Jest with coverage and junit reporter
        sh 'NODE_OPTIONS="--localstorage-file=.localstorage" NODE_ENV=test node -r ./jest-environment.js ./node_modules/jest/bin/jest.js --config=jest.config.js --runInBand --coverage --reporters=default --reporters=jest-junit'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'reports/junit/junit.xml'
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

    stage('Publish Reports') {
      steps {
        echo '=== Publishing HTML reports ==='
        script {
          // Archive coverage HTML if exists
          if (fileExists('coverage/lcov-report/index.html')) {
            archiveArtifacts artifacts: 'coverage/lcov-report/**', allowEmptyArchive: true
            try {
              publishHTML(target: [
                reportDir: 'coverage/lcov-report',
                reportFiles: 'index.html',
                reportName: 'Coverage Report',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
              ])
            } catch (ignored) { echo 'HTML Publisher not available for coverage.' }
          } else { echo 'Coverage report not found.' }

          if (fileExists('reports/cucumber-report.html')) {
            try {
              publishHTML(target: [
                reportDir: 'reports',
                reportFiles: 'cucumber-report.html',
                reportName: 'Cucumber Report',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
              ])
            } catch (ignored) { echo 'HTML Publisher not available for cucumber.' }
          } else { echo 'Cucumber HTML report not found.' }
        }
      }
    }

    stage('Generate Allure Results') {
      steps {
        echo '=== Preparing Allure results ==='
        script {
          sh 'mkdir -p allure-results'
          
          // Copy Cucumber JSON for Allure
          if (fileExists('reports/cucumber-report.json')) {
            sh 'cp reports/cucumber-report.json allure-results/'
            echo 'Cucumber JSON copied to allure-results/'
          } else {
            echo 'Warning: Cucumber JSON not found'
          }
          
          // Copy JUnit XML for Allure (optional, for unit tests)
          if (fileExists('reports/junit/junit.xml')) {
            sh 'cp reports/junit/junit.xml allure-results/'
            echo 'JUnit XML copied to allure-results/'
          } else {
            echo 'Warning: JUnit XML not found'
          }
        }
      }
    }
  }

  post {
    always {
      echo 'Pipeline complete'
      // Allure plugin will pick up results from allure-results/ directory
      allure includeProperties: false, 
             jdk: '', 
             results: [[path: 'allure-results']]
    }
  }
}
