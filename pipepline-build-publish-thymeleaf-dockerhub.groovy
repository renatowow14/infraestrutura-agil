    node {
        
        def newApp
        def registryhomol = 'qghouse.duckdns.org/library/plataform-base'
        def registryCredential = 'harbor'
        def dockerImage = ''
        def NODE_VERSION='v16.4.2'
        def autoCancelled = false
        def SERVER_HOMOL='192.168.25.3:4243'
        
        stage('Checkout') {
            git branch: 'develop',
                url: 'https://github.com/renatowow14/plataform-base-teste.git'
        }
        stage('Validate') {
            sh 'git pull origin develop'
        }
    //     stage('Build') {
    //                     //INSTALL NVM BINARY AND INSTALL NODE VERSION AND USE NODE VERSION
    //                     nvm(nvmInstallURL: 'https://raw.githubusercontent.com/creationix/nvm/master/install.sh', 
    //                     nvmIoJsOrgMirror: 'https://iojs.org/dist',
    //                     nvmNodeJsOrgMirror: 'https://nodejs.org/dist', 
    //                     version: NODE_VERSION) {
                        
    //                     //BUILD APPLICATION 
    //                     echo "Build main site distribution"
    //                     sh "rm -rfv package-lock.json"
    //                     sh "cd  src/client && rm -rfv node_modules/ >> /dev/null && rm -rfv package-lock.json && npm install"
    //                     sh "cd  src/server && rm -rfv node_modules/ >> /dev/null && rm -rfv package-lock.json && npm install"

    //                     //VERIFY IF BUILD IS COMPLETE AND NOTIFY IN DISCORD ABOUT OF THE RESULT
    //                     def status = sh(returnStatus: true, script: "cd  src/client && ng build")
    //                     if (status != 0) {
    //                         echo "FAILED BUILD!"
    //                         currentBuild.result = 'FAILED'
    //                         def discordImageSuccess = 'https://www.jenkins.io/images/logos/formal/256.png'
    //                         def discordImageError = 'https://www.jenkins.io/images/logos/fire/256.png'

    //                         def discordDesc =
    //                                 "Result: ${currentBuild.currentResult}\n" +
    //                                         "Project: Nome projeto\n" +
    //                                         "Commit: Quem fez commit\n" +
    //                                         "Author: Autor do commit\n" +
    //                                         "Message: EROO NA BUILD!\n" +
    //                                         "Duration: ${currentBuild.durationString}"

    //                                         //Variaveis de ambiente do Jenkins - NOME DO JOB E NÚMERO DO JOB
    //                                         def discordFooter = "${env.JOB_BASE_NAME} (#${BUILD_NUMBER})"
    //                                         def discordTitle = "${env.JOB_BASE_NAME} (build #${BUILD_NUMBER})"
    //                                         def urlWebhook = "https://discord.com/api/webhooks/799479979448336445/onr86IjH0zBoxzRZfsVnbQJIKBuygmKNAQkkJ1WTqQ0pB4abcOdjvUmRZHHi1Z_luM_E"

    //                         discordSend description: discordDesc,
    //                                 footer: discordFooter,
    //                                 link: env.JOB_URL,
    //                                 result: currentBuild.currentResult,
    //                                 title: discordTitle,
    //                                 webhookURL: urlWebhook,
    //                                 successful: currentBuild.resultIsBetterOrEqualTo('SUCCESS'),
    //                                 thumbnail: 'SUCCESS'.equals(currentBuild.currentResult) ? discordImageSuccess : discordImageError
    //                         autoCancelled = true
    //                         error('Aborting the build.')
    // }                               
                
    //             }
    //     }

        stage('SonarQube Analysis') {
            env.sonarHome= tool name: 'SonarScanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
            withSonarQubeEnv('sonar.installation') { // from SonarQube servers > name
            sh "${sonarHome}/bin/sonar-runner -Dsonar.projectName=plataforma-base  -Dsonar.projectKey=d3d4454508d8c971f93f08c578b05d21fa6b63ba -Dsonar.host.url=https://qgvalidator.duckdns.org"

        }
        }
        stage('Building Image') {
            dockerImage = docker.build registryhomol + ":$BUILD_NUMBER"
        }
        stage('Push Image to Registry') {
            
            docker.withRegistry( 'https://qghouse.duckdns.org', registryCredential ) {
            dockerImage.push("${env.BUILD_NUMBER}")
            dockerImage.push("latest")
                        
                }   
                
            }
        stage('Removing image Locally') {
            sh "docker rmi $registryhomol:$BUILD_NUMBER"
            sh "docker rmi $registryhomol:latest"
        }

        stage('Pull imagem on DEV') {
                
                        def urlImage = "http://$SERVER_HOMOL/images/create?fromImage=$registryhomol:latest";
                        def response = httpRequest url:"${urlImage}", httpMode:'POST', acceptType: 'APPLICATION_JSON', validResponseCodes:"200"
                        println("Status: " + response.status)
                        def pretty_json = writeJSON( returnText: true, json: response.content)
                        println pretty_json  
            }

        stage('Deploy container on DEV') {
                
                        configFileProvider([configFile(fileId: '431b4b00-d94a-4837-87d4-0f019dd583f7', targetLocation: 'container.json')]) {

                            def url = "http://$SERVER_HOMOL/containers/plataform-base?force=true"
                            def response = sh(script: "curl -v -X DELETE $url", returnStdout: true).trim()
                            echo response

                            url = "http://$SERVER_HOMOL/containers/create?name=plataform-base"
                            response = sh(script: "curl -v -X POST -H 'Content-Type: application/json' -d @container.json -s $url", returnStdout: true).trim()
                            echo response
                        }
    
            }            
        stage('Start container on DEV') {

                        final String url = "http://$SERVER_HOMOL/containers/plataform-base/start"
                        final String response = sh(script: "curl -v -X POST -s $url", returnStdout: true).trim()
                        echo response                    
                    
                
            }                      
        stage('Send message to Discord') {
            
                        //SEND DISCORD NOTIFICATION
                        def discordImageSuccess = 'https://www.jenkins.io/images/logos/formal/256.png'
                        def discordImageError = 'https://www.jenkins.io/images/logos/fire/256.png'

                        def discordDesc =
                                "Result: ${currentBuild.currentResult}\n" +
                                        "Project: Nome projeto\n" +
                                        "Commit: Quem fez commit\n" +
                                        "Author: Autor do commit\n" +
                                        "Message: mensagem do changelog ou commit\n" +
                                        "Duration: ${currentBuild.durationString}"

                                        //Variaveis de ambiente do Jenkins - NOME DO JOB E NÚMERO DO JOB
                                        def discordFooter = "${env.JOB_BASE_NAME} (#${BUILD_NUMBER})"
                                        def discordTitle = "${env.JOB_BASE_NAME} (build #${BUILD_NUMBER})"
                                        def urlWebhook = "https://discord.com/api/webhooks/799479979448336445/onr86IjH0zBoxzRZfsVnbQJIKBuygmKNAQkkJ1WTqQ0pB4abcOdjvUmRZHHi1Z_luM_E"

                        discordSend description: discordDesc,
                                footer: discordFooter,
                                link: env.JOB_URL,
                                result: currentBuild.currentResult,
                                title: discordTitle,
                                webhookURL: urlWebhook,
                                successful: currentBuild.resultIsBetterOrEqualTo('SUCCESS'),
                                thumbnail: 'SUCCESS'.equals(currentBuild.currentResult) ? discordImageSuccess : discordImageError              
                    
            }         
        
        
        
        }
