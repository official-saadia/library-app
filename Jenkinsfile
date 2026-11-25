pipeline {
    agent none
    triggers { githubPush() }
    stages {
        stage('Build and test') {
            agent any
            tools { maven 'maven-3' }
            stages {
                stage('Checkout') {
                    steps { checkout scm }
                }
                stage('Build') {
                    steps { sh 'mvn -B clean package -DskipTests' }
                }
                stage('Test') {
                    steps { sh 'mvn -B test' }
                }
                stage('Use credential') {
                    steps {
                        withCredentials([string(credentialsId: 'deploy-token', variable: 'DEPLOY_TOKEN')]) {
                            sh 'echo "Token is $DEPLOY_TOKEN"'
                        }
                    }
                }
                stage('Stash') {
                    steps {
                        stash name: 'app', includes: 'target/*.jar, smoke-test.sh'
                    }
                }
            }
            post {
                always { junit 'target/surefire-reports/*.xml' }
                success { archiveArtifacts artifacts: 'target/*.jar', fingerprint: true }
                failure {
                    mail to: 'skhdev88@gmail.com',
                         subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Build failed. Details: ${env.BUILD_URL}"
                }
            }
        }
        stage('Deploy to client') {
            agent { label 'client' }
            steps {
                unstash 'app'
                sh '''
                    pkill -f 'java -jar target' || true
                    JENKINS_NODE_COOKIE=dontKillMe nohup setsid java -jar target/*.jar > app.log 2>&1 &
                '''
            }
        }
        stage('Smoke test') {
            agent { label 'client' }
            steps {
                sh 'bash smoke-test.sh http://localhost:8080'
            }
        }
    }
}