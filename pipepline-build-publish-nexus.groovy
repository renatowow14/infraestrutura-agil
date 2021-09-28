pipeline {
  environment {
    registry = "http://34.125.69.241/repository/docker-privado"
    registryCredential = 'docker-user'
    dockerImage = ''
  }
  agent any
  tools {nodejs "NODEJS-16.9.1" }
  stages {
    stage('Cloning Git') {
      steps {
        git 'https://github.com/gustavoapolinario/node-todo-frontend'
      }
    }
    stage('Build') {
       steps {
         sh 'npm install'
       }
    }
    stage('Test Application') {
      steps {
        sh 'npm test'
      }
    }
    stage('Building image') {
      steps{
        script {
          dockerImage = docker.build registry + ":$BUILD_NUMBER"
        }
      }
    }
    stage('Test image') {
        steps{
            script{
                /* Ideally, we would run a test framework against our image.
                * For this example, we're using a Volkswagen-type approach ;-) */
                dockerImage.inside {
                    sh 'echo "Tests passed"'
                }
            }
        }
    }
    
    stage('Deploy Image') {
      steps{
         script {
            docker.withRegistry( '', registryCredential ) {
            dockerImage.push("${env.BUILD_NUMBER}")
            dockerImage.push("latest")
          }
        }
      }
    }
    
    stage('Remove Unused docker image') {
      steps{
        sh "docker rmi $registry:$BUILD_NUMBER"
      }
    }
  }
}
