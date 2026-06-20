pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'maven-3.9'
    }

    stages {
        // STEP 1: Dynamically pull the code from Git into Jenkins workspace
        stage('Checkout Source Code') {
            steps {
                cleanWs() // Optional: Cleans the workspace directory before building
                checkout scm // Instructs Jenkins to pull code from the Git repo tied to this job
            }
        }

        stage('Build Backend (Java)') {
            steps {
                echo 'Building Java Backend application...'
                // Changing skipTests to true allows Maven to package the app without checking the database
                bat 'mvn clean package -DskipTests=true' 
            }
        }
        
        stage('Deploy with Docker') {
            steps {
                script {
                    echo 'Cleaning up any existing containers safely...'
                        try {
                            bat 'docker stop my-backend-app || ver > nul'
                        } catch (Exception e) {
                            echo 'Container is already stopped.'
                        }
                        
                        try {
                            bat 'docker rm my-backend-app || ver > nul'
                        } catch (Exception e) {
                            echo 'Container is already removed.'
                        }
                        
                        try {
                            echo 'Building and running the new Docker container...'
                            bat 'docker build --no-cache -t backend-image .'
                            
                            // CHANGED PORT: Mapping local Windows port 8081 to container port 8080
                            bat 'docker run -d -p 8081:8080 --name my-backend-app backend-image'
                            
                            echo 'Docker container successfully deployed on port 8081!'
                        } catch (Exception e) {
                            echo 'Failed to build or run the Docker container.'
                            throw e
                        }
                    }
            }
        }
    }

    post {
        success {
            echo 'Backend built and deployed successfully!'
        }
        failure {
            echo 'The pipeline build encountered an error. Check logs above.'
        }
    }
}