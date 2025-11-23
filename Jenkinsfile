pipeline {
    agent { 
        label 'dev-server'
    }
    environment {
        // Docker Hub Config
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-creds'
        DOCKERHUB_USERNAME = 'your_dockerhub_username' // Thay đổi thành username của bạn
        
        // SSH Config
        SSH_CREDENTIALS_ID = 'ssh-deploy-key'
        DEPLOY_SERVER_IP = 'deploy-server-ip' // Thay đổi thành IP thực tế
        DEPLOY_DIR = '/app/chatapps'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Determine Changes') {
            steps {
                script {
                    // Lấy danh sách file thay đổi giữa commit hiện tại và commit trước đó
                    // Nếu là lần build đầu tiên hoặc force build, có thể cần logic khác
                    def changedFiles = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_COMMIT} ${env.GIT_COMMIT}", returnStdout: true).trim()
                    echo "Changed files:\n${changedFiles}"
                    
                    // Hàm kiểm tra thay đổi
                    env.BUILD_ALL = changedFiles.contains('pom.xml') || changedFiles.contains('chatapps-base/') ? 'true' : 'false'
                    
                    env.BUILD_USER = env.BUILD_ALL == 'true' || changedFiles.contains('user-service/') ? 'true' : 'false'
                    env.BUILD_CHAT = env.BUILD_ALL == 'true' || changedFiles.contains('chat-service/') ? 'true' : 'false'
                    env.BUILD_RELATIONSHIP = env.BUILD_ALL == 'true' || changedFiles.contains('relationship-service/') ? 'true' : 'false'
                    env.BUILD_MEDIA = env.BUILD_ALL == 'true' || changedFiles.contains('media-service/') ? 'true' : 'false'
                    env.BUILD_GATEWAY = env.BUILD_ALL == 'true' || changedFiles.contains('api-gateway/') ? 'true' : 'false'
                    env.BUILD_DISCOVERY = env.BUILD_ALL == 'true' || changedFiles.contains('discovery-server/') ? 'true' : 'false'
                    env.BUILD_FRONTEND = changedFiles.contains('frontend/') ? 'true' : 'false'
                }
            }
        }

        stage('Build & Push Backend') {
            parallel {
                stage('User Service') {
                    when { expression { return env.BUILD_USER == 'true' } }
                    steps {
                        buildAndPushService('user-service', '9006')
                    }
                }
                stage('Chat Service') {
                    when { expression { return env.BUILD_CHAT == 'true' } }
                    steps {
                        buildAndPushService('chat-service', '9002')
                    }
                }
                stage('Relationship Service') {
                    when { expression { return env.BUILD_RELATIONSHIP == 'true' } }
                    steps {
                        buildAndPushService('relationship-service', '9001')
                    }
                }
                stage('Media Service') {
                    when { expression { return env.BUILD_MEDIA == 'true' } }
                    steps {
                        buildAndPushService('media-service', '9004')
                    }
                }
                stage('API Gateway') {
                    when { expression { return env.BUILD_GATEWAY == 'true' } }
                    steps {
                        buildAndPushService('api-gateway', '8888')
                    }
                }
                stage('Discovery Server') {
                    when { expression { return env.BUILD_DISCOVERY == 'true' } }
                    steps {
                        buildAndPushService('discovery-server', '8761')
                    }
                }
            }
        }

        stage('Build & Push Frontend') {
            when { expression { return env.BUILD_FRONTEND == 'true' } }
            steps {
                script {
                    dir('frontend') {
                        // Docker Hub registry URL is empty string
                        docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                            def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-frontend:${env.BUILD_NUMBER}")
                            image.push()
                            image.push('latest')
                        }
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no root@${DEPLOY_SERVER_IP} '
                                mkdir -p ${DEPLOY_DIR}
                                cd ${DEPLOY_DIR}
                                
                                # Login Docker Hub
                                echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                                
                                # Pull images mới nhất
                                docker-compose -f docker-compose.prod.yml pull
                                
                                # Recreate containers
                                docker-compose -f docker-compose.prod.yml up -d
                                
                                # Clean up unused images
                                docker image prune -f
                            '
                        """
                    }
                }
            }
        }
    }
}

def buildAndPushService(String serviceName, String port) {
    script {
        echo "Building ${serviceName}..."
        // Build Maven
        sh "mvn clean package -pl ${serviceName} -am -DskipTests"
        
        // Build Docker & Push
        docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
            def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-${serviceName}:${env.BUILD_NUMBER}", "./${serviceName}")
            image.push()
            image.push('latest')
        }
    }
}
