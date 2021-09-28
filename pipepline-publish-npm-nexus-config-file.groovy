pipeline {
    agent any
    tools{nodejs "NODEJS-16.9.1"}

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/brunourb/npm-app1.git'
            }
        }

        stage('Publish to Nexus') {
            steps {
                 configFileProvider([configFile(fileId: 'b42c6fbc-37f8-4568-967c-81e44eb0e478', targetLocation: '.npmrc')]) {
                    sh "ls -la"
                    sh 'npm publish -verbose'
                }
            }
        }
    }
}
