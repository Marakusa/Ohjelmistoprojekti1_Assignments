pipeline {
    agent any
    tools {
        maven 'Maven3'
    }
    environment {
        PATH = "C:\\Users\\skype\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKERHUB_REPO = 'marakusa/temperature-converter'
        DOCKER_IMAGE_TAG = 'latest'
    }
    stages {
        stage('check') {
            steps {
                git 'https://github.com/Marakusa/Ohjelmistoprojekti1_Assignments.git'
            }
        }
        stage('build') {
            steps {
                bat 'mvn clean install'
            }
        }
        stage('test') {
            steps {
                bat 'mvn test'
            }
        }
        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
        stage('jacoco') {
            steps {
                jacoco()
            }
        }
        stage('Build Docker Image'){
             steps{
                 script{
                     docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                 }
             }
        }
        stage('Push Docker Image to Docker Hub'){
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        def customImage = docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                        customImage.push()
                    }
                }
            }
        }
    }
}