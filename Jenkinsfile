pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '15'))
    disableConcurrentBuilds()
  }

  parameters {
    string(name: 'SONAR_PROJECT_KEY', defaultValue: 'retos_docker', description: 'Clave del proyecto en SonarQube')
  string(name: 'SONAR_PROJECT_NAME', defaultValue: 'Retos_Docker', description: 'Nombre del proyecto en SonarQube')
    booleanParam(name: 'RUN_AUTOMATION_TESTS', defaultValue: false, description: 'Clonar y ejecutar proyecto de automatización de pruebas')
    string(name: 'AUTOMATION_REPO_URL', defaultValue: 'https://github.com/JacoboLS04/retos_docker.git', description: 'URL del repositorio de automatización (opcional si está en este repo)')
    string(name: 'AUTOMATION_REPO_BRANCH', defaultValue: 'taller-automatizacion', description: 'Rama del repo de automatización')
  }

  environment {
    // Requiere tener configurado en Jenkins: Manage Jenkins > Global Tool Configuration
    SCANNER_HOME = tool name: 'SonarQubeScanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Unit Tests') {
      steps {
        sh '''
          set -e
          chmod +x ./gradlew || true
          # Ensure Unix line endings on gradlew (in case checked out with CRLF)
          if command -v dos2unix >/dev/null 2>&1; then
            dos2unix ./gradlew || true
          fi
          ./gradlew --no-daemon clean test || true
        '''
      }
      post {
        always {
          // Publica resultados JUnit para historial en Jenkins
          // Amplía los patrones para capturar más ubicaciones posibles
          junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml,**/target/surefire-reports/*.xml'

          // Publica el reporte HTML que utiliza index.html (Gradle) si existe
          script {
            def reportDir = 'build/reports/tests/test'
            if (fileExists(reportDir + '/index.html')) {
              publishHTML(target: [
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: reportDir,
                reportFiles: 'index.html',
                reportName: 'Unit Test Report'
              ])
            } else {
              echo "No existe el directorio de reportes HTML: ${reportDir}. Listando workspace para diagnóstico..."
              sh 'ls -la'
              sh 'ls -la build || true'
              sh 'ls -la build/reports || true'
              sh 'ls -la build/test-results || true'
            }

            archiveArtifacts artifacts: 'build/reports/tests/**,build/test-results/**', allowEmptyArchive: true
            // Publicar Allure results si existen
            script {
              def allureDir = 'build/allure-results'
              if (fileExists(allureDir)) {
                echo "Allure results encontrados en ${allureDir}. Intentando publicarlos (requiere plugin Allure)."
                try {
                  allure includeProperties: false, jdk: '', results: [[path: allureDir]]
                } catch (Throwable e) {
                  echo "No se pudo publicar Allure (¿falta plugin Allure?): ${e.message}"
                }
              } else {
                echo "No se encontraron Allure results en ${allureDir}."
              }
            }
          }
        }
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('SonarQube') { // Requiere configurar el servidor en Jenkins con este nombre
          sh '''
            ${SCANNER_HOME}/bin/sonar-scanner \
              -Dsonar.projectKey="${SONAR_PROJECT_KEY}" \
              -Dsonar.projectName="${SONAR_PROJECT_NAME}" \
              -Dsonar.sources=src/main/java \
              -Dsonar.tests=src/test/java \
              -Dsonar.java.binaries=build/classes/java/main \
              -Dsonar.java.test.binaries=build/classes/java/test \
              -Dsonar.junit.reportPaths=build/test-results
          '''
        }
      }
    }

    stage('Quality Gate') {
      steps {
        script {
          // Requiere webhook SonarQube -> Jenkins o que el plugin pueda consultar el análisis
          timeout(time: 3, unit: 'MINUTES') {
            def qg = waitForQualityGate abortPipeline: true
            echo "Quality Gate: ${qg.status}"
          }
        }
      }
    }

    stage('Automation Tests (optional)') {
      when { expression { return params.RUN_AUTOMATION_TESTS as boolean } }
      steps {
        script {
          sh '''
            set -e
            WORKDIR=$(pwd)

            if [ -n "${AUTOMATION_REPO_URL}" ]; then
              echo "Clonando repo de automatización: ${AUTOMATION_REPO_URL} (${AUTOMATION_REPO_BRANCH})"
              rm -rf automation-tests
              git clone --depth 1 --branch "${AUTOMATION_REPO_BRANCH}" "${AUTOMATION_REPO_URL}" automation-tests
              cd automation-tests
            elif [ -d "pruebas-taller-automatizacion" ]; then
              echo "Usando carpeta de automatización local: pruebas-taller-automatizacion"
              cd pruebas-taller-automatizacion
            else
              echo "No hay repo/carpeta de automatización. Saliendo de la etapa."
              exit 0
            fi

            # Detección simple del tipo de proyecto de automatización
            if [ -f "gradlew" ]; then
              chmod +x gradlew || true
              ./gradlew --no-daemon clean test || true
            elif [ -f "mvnw" ]; then
              chmod +x mvnw || true
              ./mvnw -B -DskipTests=false test || true
            elif [ -f "pom.xml" ]; then
              mvn -B -DskipTests=false test || true
            elif [ -f "package.json" ]; then
              echo "Proyecto Node detectado. Se omite ejecución porque el contenedor no tiene Node/npm. Configure un agente con Node o ejecute en Docker."
            elif ls *.bru >/dev/null 2>&1 || ls **/*.bru >/dev/null 2>&1; then
              echo "Colecciones Bruno detectadas. Intentando ejecutar con 'bru' si está disponible..."
              if command -v bru >/dev/null 2>&1; then
                echo "Ejecutando Bruno CLI"
                bru run . || true
                # Si se genera un reporte HTML, intente moverlo a una ubicación conocida
                if [ -d "reports" ] && [ -f "reports/index.html" ]; then
                  mkdir -p build/reports/bruno
                  cp -r reports/* build/reports/bruno/
                fi
              else
                echo "'bru' no está instalado en el agente. Instale Node + Bruno CLI (@usebruno/cli) o configure un agente con estas herramientas."
              fi
            else
              echo "No se detectó herramienta de automatización soportada (Gradle/Maven/Node/Bruno)."
            fi

            cd "$WORKDIR"
          '''
        }
      }
      post {
        always {
          script {
            // Publica cualquier reporte HTML (index.html) común en proyectos Gradle/Maven
            def candidates = [
              [dir: 'automation-tests/build/reports/tests/test', name: 'Automation Unit Test Report (Gradle)'],
              [dir: 'pruebas-taller-automatizacion/build/reports/tests/test', name: 'Automation Unit Test Report (Gradle - local)'],
              [dir: 'automation-tests/target/surefire-reports', name: 'Automation Surefire (Maven)'],
              [dir: 'automation-tests/target/site', name: 'Automation Site (Maven)'],
              [dir: 'pruebas-taller-automatizacion/target/surefire-reports', name: 'Automation Surefire (Maven - local)'],
              [dir: 'pruebas-taller-automatizacion/target/site', name: 'Automation Site (Maven - local)']
            ]
            for (c in candidates) {
              try {
                if (fileExists(c.dir + '/index.html')) {
                  publishHTML(target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: c.dir,
                    reportFiles: 'index.html',
                    reportName: c.name
                  ])
                } else {
                  echo "No existe ${c.dir}/index.html, salto publicación: ${c.name}"
                }
              } catch (Throwable e) {
                echo "No se pudo publicar reporte HTML en ${c.dir}: ${e.message}"
              }
            }

            // Archivar resultados por si se requieren
            archiveArtifacts artifacts: 'automation-tests/**/reports/**,pruebas-taller-automatizacion/**/reports/**', allowEmptyArchive: true
            junit allowEmptyResults: true, testResults: 'automation-tests/**/test-results/**/*.xml,pruebas-taller-automatizacion/**/test-results/**/*.xml,automation-tests/target/surefire-reports/*.xml,pruebas-taller-automatizacion/target/surefire-reports/*.xml'
          }
        }
      }
    }
  }

  post {
    success { echo 'Pipeline finalizado correctamente.' }
    unstable { echo 'Pipeline inestable (revise pruebas/calidad).' }
    failure { echo 'Pipeline falló.' }
    always {
      echo 'Publicación de artefactos y reportes completada.'
    }
  }
}
