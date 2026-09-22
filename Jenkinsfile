pipeline {
    agent any
    tools {
        maven 'Maven3'
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
    }
}