node {
    checkout scm

    docker.withServer('tcp://34.125.87.11', 'maquina-deploy') {
        docker.image('mysql:5').withRun('-p 3306:3306') {
           sh "ls -la"
        }
    }
}
