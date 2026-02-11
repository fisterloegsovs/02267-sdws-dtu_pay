def GITLAB_CONNECTION = 'Gitlab'

pipeline {
    agent any
    options { skipDefaultCheckout() }

    stages {
        stage('Git') {
            steps {
                script {
                    gitlabCommitStatus(name: "Git", connection: gitLabConnection(GITLAB_CONNECTION)) {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: params.branch]],
                            userRemoteConfigs: scm.userRemoteConfigs
                        ])
                    }
                }
            }
        }
        stage('Build code') {
            steps {
                gitlabCommitStatus(name: "Build code", connection: gitLabConnection(GITLAB_CONNECTION)) {
                    sh './compile.sh'
                }
            }
        }
        stage('Build docker') {
            steps {
                gitlabCommitStatus(name: "Docker build", connection: gitLabConnection(GITLAB_CONNECTION)) {
                    sh './reset.sh'
                    sh './build-images.sh'
                }
            }
        }
        stage('Deploy docker') {
            steps {
                gitlabCommitStatus(name: "Docker deploy", connection: gitLabConnection(GITLAB_CONNECTION)) {
                    sh './deploy.sh'
                }
            }
        }
        stage('Run tests') {
            steps {
                gitlabCommitStatus(name: "End-to-end tests", connection: gitLabConnection(GITLAB_CONNECTION)) {
                    sh './run-tests.sh'
                }
            }
        }
    }
    post {
        always {
            junit '**/target/surefire-reports/*.xml'
        }
    }
}
