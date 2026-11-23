pipeline {
    agent any
    triggers {
        githubPush()
    }
    tools {
        maven 'maven-3'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
        }
        stage('Use credential') {
            steps {
                withCredentials([string(credentialsId: 'deploy-token', variable: 'DEPLOY_TOKEN')]) {
                    sh 'echo "Token is $DEPLOY_TOKEN"'
                }
            }
        }
    }
    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        failure {
            mail to: 'skhdev88@gmail.com',
            subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: "Build failed. Details: ${env.BUILD_URL}"
        }
    }
}