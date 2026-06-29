pipeline {
    agent any

    environment {
        REGISTRY_CREDENTIALS_ID = 'docker-hub-credentials'
        IMAGE_NAME = 'balagrishnan/product-service-java'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    tools {
        jdk 'JDK17'
        maven 'maven-3.9'
    }

    stages {
        // STEP 1: Dynamically pull the code from Git into Jenkins workspace
        stage('Checkout Source Code') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        // STEP 2: Build the executable .jar file
        stage('Build Backend (Java)') {
            steps {
                echo 'Building Java Backend application...'
                bat 'mvn clean package -DskipTests=true'
            }
        }

        // STEP 3: Build the Docker image locally
        stage('Build Docker Image') {
            steps {
                echo "Building product-service-java Docker Image..."
                bat "docker build --no-cache -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                bat "docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest"
            }
        }

        // STEP 4: Login and backup your build to Docker Hub
        stage('Login & Push to Registry') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${REGISTRY_CREDENTIALS_ID}",
                                                 usernameVariable: 'HUB_USER',
                                                 passwordVariable: 'HUB_TOKEN')]) {
                    bat 'docker logout || ver > nul'
                    bat 'docker login -u %HUB_USER% -p %HUB_TOKEN%'
                    bat "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                    bat "docker push ${IMAGE_NAME}:latest"
                }
            }
        }

        // STEP 5: Deploy the container to your local system completely automated
        stage('Deploy with Docker') {
            steps {
                echo 'Cleaning up any existing containers safely...'
                // Force kill/remove old instance if it exists on Windows
                bat 'docker rm -f product-service-java || ver > nul'

                echo 'Running the new Docker container attached to product-network...'
                // Launches the fresh build on the internal virtual bridge network
                bat """
                    docker run -d ^
                    --name product-service-java ^
                    --network product-network ^
                    -p 8081:8081 ^
                    --restart always ^
                    ${IMAGE_NAME}:latest
                """
                echo 'Docker container successfully deployed on port 8081!'
            }
        }
    }

    post {
        always {
            bat "docker logout || ver > nul"
        }
        success {
            echo 'Backend built, pushed, and deployed successfully!'
        }
        failure {
            echo 'The pipeline build encountered an error. Check logs above.'
        }
    }
}