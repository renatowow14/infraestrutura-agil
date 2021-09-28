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
                sh 'echo "registry=${REGISTRY_URL}\n_auth=${NPM_TOKEN}\nemail=${REGISTRY_USER_EMAIL}\nalways-auth=true\n" > .npmrc'
                sh 'npm publish -verbose'
            }
        }
    }
}
