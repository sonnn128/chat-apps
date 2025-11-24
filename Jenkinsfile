pipeline {
    agent { 
        label 'dev-server'
    }
    
    parameters {
        booleanParam(name: 'FORCE_BUILD_ALL', defaultValue: false, description: 'Check this to force build all services regardless of changes')
    }

    environment {
        // Docker Hub Config
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-creds'
        DOCKERHUB_USERNAME = 'sonta28122004'
        
        // Deploy Config
        DEPLOY_DIR = '/home/jenkins/chatapps' // Hoặc thư mục home của user chạy agent
        
        VITE_REACT_APP_BASE_URL = 'http://34.158.40.253:8888'
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
                    // Lấy danh sách file thay đổi
                    def changedFiles = ""
                    try {
                        changedFiles = sh(script: "git diff --name-only ${env.GIT_PREVIOUS_COMMIT} ${env.GIT_COMMIT}", returnStdout: true).trim()
                    } catch (Exception e) {
                        echo "Could not determine changed files (First run?), defaulting to empty."
                    }
                    echo "Changed files:\n${changedFiles}"
                    
                    // Logic kiểm tra: Build nếu có thay đổi HOẶC Force Build được chọn
                    def force = params.FORCE_BUILD_ALL == true
                    
                    env.BUILD_ALL = force || changedFiles.contains('pom.xml') || changedFiles.contains('chatapps-base/') ? 'true' : 'false'
                    
                    env.BUILD_USER = env.BUILD_ALL == 'true' || changedFiles.contains('user-service/') ? 'true' : 'false'
                    env.BUILD_CHAT = env.BUILD_ALL == 'true' || changedFiles.contains('chat-service/') ? 'true' : 'false'
                    env.BUILD_RELATIONSHIP = env.BUILD_ALL == 'true' || changedFiles.contains('relationship-service/') ? 'true' : 'false'
                    env.BUILD_MEDIA = env.BUILD_ALL == 'true' || changedFiles.contains('media-service/') ? 'true' : 'false'
                    env.BUILD_GATEWAY = env.BUILD_ALL == 'true' || changedFiles.contains('api-gateway/') ? 'true' : 'false'
                    env.BUILD_DISCOVERY = env.BUILD_ALL == 'true' || changedFiles.contains('discovery-server/') ? 'true' : 'false'
                    
                    // Frontend tách biệt
                    env.BUILD_FRONTEND = force || changedFiles.contains('frontend/') ? 'true' : 'false'
                    
                    echo "Build Plan: User=${env.BUILD_USER}, Chat=${env.BUILD_CHAT}, Frontend=${env.BUILD_FRONTEND}..."
                }
            }
        }

        stage('Build & Push Backend') {
            parallel {
                stage('User Service') {
                    when { expression { return env.BUILD_USER == 'true' } }
                    steps {
                        script {
                            echo "Building user-service..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-user-service:${env.BUILD_NUMBER}", "-f user-service/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
                stage('Chat Service') {
                    when { expression { return env.BUILD_CHAT == 'true' } }
                    steps {
                        script {
                            echo "Building chat-service..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-chat-service:${env.BUILD_NUMBER}", "-f chat-service/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
                stage('Relationship Service') {
                    when { expression { return env.BUILD_RELATIONSHIP == 'true' } }
                    steps {
                        script {
                            echo "Building relationship-service..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-relationship-service:${env.BUILD_NUMBER}", "-f relationship-service/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
                stage('Media Service') {
                    when { expression { return env.BUILD_MEDIA == 'true' } }
                    steps {
                        script {
                            echo "Building media-service..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-media-service:${env.BUILD_NUMBER}", "-f media-service/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
                stage('API Gateway') {
                    when { expression { return env.BUILD_GATEWAY == 'true' } }
                    steps {
                        script {
                            echo "Building api-gateway..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-api-gateway:${env.BUILD_NUMBER}", "-f api-gateway/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
                stage('Discovery Server') {
                    when { expression { return env.BUILD_DISCOVERY == 'true' } }
                    steps {
                        script {
                            echo "Building discovery-server..."
                            docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                                def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-discovery-server:${env.BUILD_NUMBER}", "-f discovery-server/Dockerfile .")
                                image.push()
                                image.push('latest')
                            }
                        }
                    }
                }
            }
        }

        stage('Build & Push Frontend') {
            when { expression { return env.BUILD_FRONTEND == 'true' } }
            steps {
                script {
                    echo "DEBUG: VITE_REACT_APP_BASE_URL is '${env.VITE_REACT_APP_BASE_URL}'"
                    dir('frontend') {
                        // Docker Hub registry URL is empty string
                        docker.withRegistry('', "${DOCKERHUB_CREDENTIALS_ID}") {
                            def image = docker.build("${DOCKERHUB_USERNAME}/chatapps-frontend:${env.BUILD_NUMBER}", "--build-arg VITE_REACT_APP_BASE_URL=${env.VITE_REACT_APP_BASE_URL} .")
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
                    sh """
                        mkdir -p ${DEPLOY_DIR}
                        cp docker-compose.prod.yml ${DEPLOY_DIR}/
                        cd ${DEPLOY_DIR}
                        
                        # Login Docker Hub
                        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                        
                        # Pull images mới nhất
                        docker-compose -f docker-compose.prod.yml pull
                        
                        # Recreate containers
                        docker-compose -f docker-compose.prod.yml up -d
                        
                        # Clean up unused images
                        docker image prune -f
                    """
                }
            }
        }
    }
}


