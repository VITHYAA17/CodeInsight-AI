pipeline {
    agent any

    environment {
        PROJECT_ID          = 'codeinsight-ai-production'
        GCP_REGION          = 'us-central1'
        GKE_CLUSTER         = 'codeinsight-gke-cluster-prod'
        REGISTRY_REPO       = "us-central1-docker.pkg.dev/${PROJECT_ID}/codeinsight-docker-prod"
        GCP_CREDENTIALS_ID  = 'gcp-service-account-key'
        IMAGE_TAG           = "${BUILD_NUMBER}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
                echo "Building commit: ${GIT_COMMIT}"
            }
        }

        stage('Backend Build & Test') {
            steps {
                dir('backend') {
                    echo "Compiling and running unit tests with Maven..."
                    sh './mvnw clean test'
                }
            }
        }

        stage('Frontend Build & Test') {
            steps {
                dir('frontend') {
                    echo "Installing dependencies and running frontend production build..."
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo "Building Docker container images for Backend & Frontend..."
                sh """
                    docker build -t ${REGISTRY_REPO}/backend:${IMAGE_TAG} -t ${REGISTRY_REPO}/backend:latest ./backend
                    docker build -t ${REGISTRY_REPO}/frontend:${IMAGE_TAG} -t ${REGISTRY_REPO}/frontend:latest ./frontend
                """
            }
        }

        stage('Push to GCP Artifact Registry') {
            steps {
                echo "Authenticating to Google Artifact Registry and pushing images..."
                withCredentials([file(credentialsId: "${GCP_CREDENTIALS_ID}", variable: 'GCP_KEY_FILE')]) {
                    sh """
                        gcloud auth activate-service-account --key-file=\${GCP_KEY_FILE}
                        gcloud auth configure-docker ${GCP_REGION}-docker.pkg.dev --quiet
                        docker push ${REGISTRY_REPO}/backend:${IMAGE_TAG}
                        docker push ${REGISTRY_REPO}/backend:latest
                        docker push ${REGISTRY_REPO}/frontend:${IMAGE_TAG}
                        docker push ${REGISTRY_REPO}/frontend:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes (GKE)') {
            steps {
                echo "Deploying updated containers to GKE cluster..."
                withCredentials([file(credentialsId: "${GCP_CREDENTIALS_ID}", variable: 'GCP_KEY_FILE')]) {
                    sh """
                        gcloud auth activate-service-account --key-file=\${GCP_KEY_FILE}
                        gcloud container clusters get-credentials ${GKE_CLUSTER} --region ${GCP_REGION} --project ${PROJECT_ID}
                        kubectl apply -k k8s/
                        kubectl set image deployment/codeinsight-backend backend=${REGISTRY_REPO}/backend:${IMAGE_TAG} -n codeinsight
                        kubectl set image deployment/codeinsight-frontend frontend=${REGISTRY_REPO}/frontend:${IMAGE_TAG} -n codeinsight
                        kubectl rollout status deployment/codeinsight-backend -n codeinsight --timeout=120s
                        kubectl rollout status deployment/codeinsight-frontend -n codeinsight --timeout=120s
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline executed successfully! CodeInsight AI deployed to GKE."
        }
        failure {
            echo "Pipeline failed. Check Jenkins console log for details."
        }
        always {
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
