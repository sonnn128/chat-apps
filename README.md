# HOW TO RUN THE APP?  

## 1. For production

### Production Deployment Guide
For comprehensive production deployment instructions, including:
- Complete deployment strategy and mindset
- Step-by-step infrastructure setup
- CI/CD pipeline with Jenkins
- Monitoring and logging setup
- Security best practices
- Troubleshooting guide

**👉 See [DEPLOYMENT.md](./DEPLOYMENT.md) for full details**

### Quick Production Deployment

```bash
# 1. Clone repository
git clone https://github.com/sonnn128/chat-apps.git
cd chat-apps

# 2. Configure environment variables
cp .env.prod.example .env.prod
# Edit .env.prod with your production values

# 3. Deploy with production configuration
docker-compose -f docker-compose.prod.yml up -d

# 4. Check service health
./scripts/health-check.sh

# 5. Access the application
# UI: http://your-domain:5173
# Keycloak: http://your-domain:8080
# Eureka Dashboard: http://your-domain:8761
```

## 2. For docker (Development)
```
cd <project_folder>
docker compose up -d
```
Then visit: [http://localhost:5173](http://localhost:5173) to access this app

## 3. For development
### 3.1 Run database / MQ with docker
```
docker compose -f compose.dev.yml up -d
```
### 3.2 Check containers
```
C:\Users\son>docker ps
IMAGE                             PORTS                                         NAMES
tchiotludo/akhq:latest            0.0.0.0:9999->8080/tcp, [::]:9999->8080/tcp   akhq
confluentinc/cp-kafka:7.3.0       0.0.0.0:9092->9092/tcp, [::]:9092->9092/tcp   kafka
confluentinc/cp-zookeeper:7.3.0   2181/tcp, 2888/tcp, 3888/tcp                  zookeeper
cassandra:latest                  0.0.0.0:9042->9042/tcp, [::]:9042->9042/tcp   chat-apps-cassandra-1
postgres:16                       0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   chat-apps-postgres-1

```
### 3.3 Run configuration service
```
cd <project folder/discovery-server>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/discovery-server-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/api-gateway>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/api-gateway-0.0.1-SNAPSHOT.jar
```
### 3.4 Run services
```
cd <project folder/auth-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/auth-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/user-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/user-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/chat-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/chat-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/channel-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/channel-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/notification-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/notification-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/friendshipservice-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/friendshipservice-service-0.0.1-SNAPSHOT.jar
```
```
cd <project folder/media-service>
mvn install -DskipTests=true
java -jar -Xmx2048m -Xms256m /target/media-service-0.0.1-SNAPSHOT.jar
```


### 3.4 Run ui
```
cd <project folder/ui>
npm install 
npm run dev
```
Then visit: [http://localhost:5173](http://localhost:5173) to access this app
---

## 🚀 Triển khai Production & CI/CD

### 📖 Tài liệu triển khai

Dự án bao gồm tài liệu đầy đủ để triển khai lên production và thiết lập CI/CD:

1. **[DEPLOYMENT.md](./DEPLOYMENT.md)** - Hướng dẫn triển khai production
   - Mindset và chiến lược triển khai microservices
   - Thứ tự triển khai từng bước (Infrastructure → Config → Services → UI)
   - Cấu hình môi trường production
   - Monitoring và logging (ELK Stack, Prometheus)
   - Security best practices
   - Troubleshooting chi tiết
   - Template mô tả dự án cho CV

2. **[JENKINS_SETUP.md](./JENKINS_SETUP.md)** - Hướng dẫn cài đặt Jenkins CI/CD
   - Cài đặt Jenkins từ đầu
   - Cấu hình plugins và credentials
   - Tạo pipeline tự động
   - Cấu hình webhook GitHub
   - Troubleshooting Jenkins

3. **[Jenkinsfile](./Jenkinsfile)** - Pipeline CI/CD hoàn chỉnh
   - Build tất cả services song song
   - Chạy tests tự động
   - Build và push Docker images
   - Deploy tự động theo branch (dev/staging/prod)
   - Health checks sau deployment

### 🛠️ Scripts hỗ trợ

Trong thư mục `scripts/`:

- **health-check.sh** - Kiểm tra health của tất cả services
- **backup-databases.sh** - Backup tự động PostgreSQL, Cassandra, MySQL
- **restore-databases.sh** - Restore databases từ backup

```bash
# Kiểm tra health
./scripts/health-check.sh

# Backup databases
./scripts/backup-databases.sh

# Restore databases
./scripts/restore-databases.sh backups/20250111_120000.tar.gz
```

### 🐳 Docker Compose cho Production

```bash
# Deploy với cấu hình production
docker-compose -f docker-compose.prod.yml up -d

# Kiểm tra logs
docker-compose -f docker-compose.prod.yml logs -f

# Stop services
docker-compose -f docker-compose.prod.yml down
```

---

## Git commit invention
```
feat: chat 
feat(ui): create Emoji message
feat(api): create api GET /users

fix(ui):
fix(api):

test(ui):
test(api):


```

```
docker exec -it keycloak /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/export --realm chat-apps
```
```
Restart container and apply code or config
docker compose up --build api-gateway
```