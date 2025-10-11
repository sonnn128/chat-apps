# 🚀 HƯỚNG DẪN TRIỂN KHAI DỰ ÁN CHAT-APPS LÊN PRODUCTION

## 📋 MỤC LỤC
1. [Mindset Triển Khai](#mindset-triển-khai)
2. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
3. [Thứ Tự Triển Khai](#thứ-tự-triển-khai)
4. [Cấu Hình Environment](#cấu-hình-environment)
5. [CI/CD với Jenkins](#cicd-với-jenkins)
6. [Monitoring và Logging](#monitoring-và-logging)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## 🎯 MINDSET TRIỂN KHAI

### Nguyên tắc vàng khi triển khai Microservices
1. **Infrastructure First**: Triển khai hạ tầng cơ sở trước (databases, message queues, service discovery)
2. **Configuration Services**: Triển khai các service quản lý cấu hình và service discovery
3. **Core Services**: Triển khai các service nghiệp vụ theo thứ tự phụ thuộc
4. **API Gateway**: Triển khai gateway sau khi các service đã sẵn sàng
5. **Frontend**: Triển khai UI cuối cùng khi backend đã hoàn thiện

### Chiến lược triển khai từng bước

```
Phase 1: Infrastructure Layer (Nền tảng)
    ├── Databases (PostgreSQL, Cassandra, MySQL-Keycloak)
    ├── Message Queue (Kafka + Zookeeper)
    └── Identity Management (Keycloak)

Phase 2: Configuration Layer (Cấu hình)
    ├── Discovery Server (Eureka)
    └── API Gateway (Spring Cloud Gateway)

Phase 3: Business Services Layer (Nghiệp vụ)
    ├── User Service
    ├── Auth Service
    ├── Media Service
    ├── Chat Service
    ├── Channel Service
    ├── Friendship Service
    └── Notification Service

Phase 4: Presentation Layer (Giao diện)
    └── UI (React/Vite)
```

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Sơ đồ kiến trúc
```
                        [Users/Clients]
                              |
                              v
                    ┌─────────────────┐
                    │   UI (React)    │ :5173
                    └────────┬────────┘
                              |
                    ┌─────────▼────────┐
                    │   API Gateway    │ :8888
                    └────────┬─────────┘
                              |
            ┌─────────────────┼─────────────────┐
            │                 │                 │
    ┌───────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
    │ User Service │  │Chat Service │  │Channel Srv  │
    │   :9005      │  │   :9007     │  │   :9008     │
    └───────┬──────┘  └──────┬──────┘  └──────┬──────┘
            │                 │                 │
    ┌───────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
    │  PostgreSQL  │  │  Cassandra  │  │   Kafka     │
    │   :5432      │  │   :9042     │  │   :9092     │
    └──────────────┘  └─────────────┘  └─────────────┘
            │
    ┌───────▼──────┐
    │  Keycloak    │
    │   :8080      │
    └──────────────┘
```

### Services và Dependencies
| Service | Port | Dependencies | Database |
|---------|------|--------------|----------|
| Discovery Server | 8761 | None | None |
| API Gateway | 8888 | Discovery Server | None |
| Keycloak | 8080 | MySQL | MySQL |
| User Service | 9005 | Discovery, PostgreSQL, Keycloak | PostgreSQL |
| Media Service | 9006 | Discovery, Cloudinary | None |
| Chat Service | 9007 | Discovery, Cassandra, Kafka | Cassandra |
| Channel Service | 9008 | Discovery, PostgreSQL, Kafka | PostgreSQL |
| Friendship Service | 9009 | Discovery, PostgreSQL, Kafka | PostgreSQL |
| Notification Service | 8889 | Discovery, Kafka | None |
| UI | 5173 | All Services | None |

---

## 📝 THỨ TỰ TRIỂN KHAI CHI TIẾT

### BƯỚC 1: Chuẩn bị môi trường

```bash
# 1.1. Cài đặt Docker và Docker Compose
sudo apt-get update
sudo apt-get install docker.io docker-compose -y
sudo systemctl start docker
sudo systemctl enable docker

# 1.2. Clone repository
git clone https://github.com/sonnn128/chat-apps.git
cd chat-apps

# 1.3. Tạo thư mục data cho các database
mkdir -p data/postgres data/cassandra data/mysql_keycloak_data
```

### BƯỚC 2: Triển khai Infrastructure (Databases & Message Queue)

```bash
# 2.1. Start PostgreSQL
docker-compose up -d postgres
docker-compose ps | grep postgres  # Kiểm tra health check
docker-compose logs postgres       # Xem logs nếu có lỗi

# 2.2. Start Cassandra
docker-compose up -d cassandra
# Đợi Cassandra khởi động hoàn toàn (có thể mất 2-3 phút)
docker-compose logs -f cassandra
# Chờ đến khi thấy "Startup complete"

# 2.3. Start Kafka ecosystem
docker-compose up -d zookeeper
sleep 10  # Đợi Zookeeper khởi động
docker-compose up -d kafka
sleep 15  # Đợi Kafka khởi động
docker-compose logs kafka | grep "started (kafka.server.KafkaServer)"

# 2.4. Verify infrastructure
docker-compose ps
# Tất cả services phải ở trạng thái healthy hoặc running
```

### BƯỚC 3: Triển khai Keycloak (Identity Management)

```bash
# 3.1. Start Keycloak MySQL database
docker-compose up -d keycloak-mysql
docker-compose logs -f keycloak-mysql
# Chờ đến khi thấy "ready for connections"

# 3.2. Start Keycloak với realm import
docker-compose up -d keycloak
docker-compose logs -f keycloak
# Chờ đến khi thấy "Keycloak started"

# 3.3. Truy cập Keycloak Admin Console
# URL: http://localhost:8080
# Username: admin
# Password: admin

# 3.4. Verify Keycloak realm import
# Kiểm tra realm "chat-apps" đã được import thành công
# Kiểm tra clients, users, roles đã được tạo
```

### BƯỚC 4: Triển khai Configuration Services

```bash
# 4.1. Build và start Discovery Server (Eureka)
docker-compose up -d discovery-server
docker-compose logs -f discovery-server
# Chờ đến khi thấy "Started EurekaServerApplication"

# 4.2. Verify Discovery Server
curl http://localhost:8761/actuator/health
# Response: {"status":"UP"}

# 4.3. Truy cập Eureka Dashboard
# URL: http://localhost:8761
# Kiểm tra không có service nào đăng ký (chưa)

# 4.4. Build và start API Gateway
docker-compose up -d api-gateway
docker-compose logs -f api-gateway
# Chờ đến khi thấy "Started ApiGatewayApplication"

# 4.5. Verify API Gateway đã đăng ký với Eureka
# Kiểm tra Eureka Dashboard, phải thấy "API-GATEWAY"
```

### BƯỚC 5: Triển khai Business Services (Core Services)

**⚠️ Lưu ý**: Triển khai từng service một, verify health check trước khi chuyển sang service tiếp theo

```bash
# 5.1. User Service (service quan trọng nhất - nhiều service phụ thuộc)
docker-compose up -d user-service
docker-compose logs -f user-service
# Chờ đến khi thấy "Started UserServiceApplication"
curl http://localhost:9005/actuator/health

# 5.2. Media Service
docker-compose up -d media-service
docker-compose logs -f media-service
curl http://localhost:9006/actuator/health

# 5.3. Chat Service (phụ thuộc Cassandra và Kafka)
docker-compose up -d chat-service
docker-compose logs -f chat-service
curl http://localhost:9007/actuator/health

# 5.4. Channel Service (phụ thuộc User Service)
docker-compose up -d channel-service
docker-compose logs -f channel-service
curl http://localhost:9008/actuator/health

# 5.5. Friendship Service
docker-compose up -d friendship-service
docker-compose logs -f friendship-service
curl http://localhost:9009/actuator/health

# 5.6. Notification Service (WebSocket service)
docker-compose up -d notification-service
docker-compose logs -f notification-service
curl http://localhost:8889/actuator/health

# 5.7. Verify tất cả services đã đăng ký với Eureka
# Truy cập http://localhost:8761 và kiểm tra
```

### BƯỚC 6: Triển khai UI (Frontend)

```bash
# 6.1. Build và start UI
docker-compose up -d ui
docker-compose logs -f ui

# 6.2. Truy cập ứng dụng
# URL: http://localhost:5173
# Kiểm tra đăng nhập, chat, upload file, etc.
```

### BƯỚC 7: Verification và Testing

```bash
# 7.1. Kiểm tra tất cả containers
docker-compose ps

# 7.2. Kiểm tra logs của tất cả services
docker-compose logs --tail=100

# 7.3. Test API qua API Gateway
curl http://localhost:8888/actuator/health

# 7.4. Test các endpoint nghiệp vụ
# Đăng ký user mới
# Đăng nhập
# Gửi tin nhắn
# Upload file
# Tạo channel
# Kết bạn
```

---

## ⚙️ CẤU HÌNH ENVIRONMENT

### Environment Variables cho Production

Tạo file `.env.prod`:

```bash
# Database Configuration
POSTGRES_DB=chatapps_prod
POSTGRES_USER=chatapps_user
POSTGRES_PASSWORD=<strong-password>
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Cassandra Configuration
CASSANDRA_HOST=cassandra
CASSANDRA_PORT=9042
CASSANDRA_KEYSPACE=chatapps_prod

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
KAFKA_AUTO_OFFSET_RESET=earliest

# Keycloak Configuration
KEYCLOAK_AUTH_SERVER_URL=http://keycloak:8080
KEYCLOAK_REALM=chat-apps
KEYCLOAK_RESOURCE=chat-apps-client
KEYCLOAK_CREDENTIALS_SECRET=<keycloak-client-secret>

# MySQL for Keycloak
MYSQL_ROOT_PASSWORD=<strong-password>
MYSQL_DATABASE=keycloak_prod
MYSQL_USER=keycloak_user
MYSQL_PASSWORD=<strong-password>

# Cloudinary (Media Service)
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-api-key>
CLOUDINARY_API_SECRET=<your-api-secret>

# Service Discovery
EUREKA_SERVER_URL=http://discovery-server:8761/eureka

# Application Configuration
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx2048m -Xms512m

# UI Configuration
VITE_REACT_APP_BASE_URL=http://api-gateway:8888/api/v1
VITE_REACT_APP_BASE_WS_URL=http://notification-service:8889/ws
```

### Security Configuration

**1. Thay đổi passwords mặc định:**
- Keycloak admin password
- Database passwords
- Keycloak client secrets

**2. Cấu hình SSL/TLS:**
```bash
# Generate SSL certificates
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout /etc/ssl/private/nginx-selfsigned.key \
  -out /etc/ssl/certs/nginx-selfsigned.crt
```

**3. Configure Nginx reverse proxy:**
```nginx
server {
    listen 80;
    server_name chat-apps.yourdomain.com;
    
    location / {
        proxy_pass http://ui:5173;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /api/ {
        proxy_pass http://api-gateway:8888/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    location /ws {
        proxy_pass http://notification-service:8889;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 🔄 CI/CD VỚI JENKINS

### Cài đặt Jenkins

```bash
# 1. Cài đặt Jenkins với Docker
docker run -d -p 8090:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins \
  jenkins/jenkins:lts

# 2. Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# 3. Truy cập Jenkins
# URL: http://localhost:8090
```

### Jenkins Plugins cần thiết

1. **Docker Pipeline**: Build và push Docker images
2. **Pipeline**: Tạo CI/CD pipeline
3. **Git**: Clone repository
4. **Credentials**: Quản lý secrets
5. **Blue Ocean**: UI đẹp cho pipeline
6. **SSH Agent**: Deploy lên remote servers

### Configure Jenkins

**1. Add Docker Hub credentials:**
- Dashboard → Manage Jenkins → Manage Credentials
- Add Credentials → Username with password
- ID: `dockerhub-credentials`

**2. Add GitHub credentials:**
- Add Credentials → Username with password hoặc SSH key
- ID: `github-credentials`

**3. Create Pipeline Job:**
- New Item → Pipeline
- Pipeline script from SCM
- SCM: Git
- Repository URL: https://github.com/sonnn128/chat-apps.git
- Branch: */main
- Script Path: Jenkinsfile

### Jenkins Pipeline Strategy

```
Stage 1: Checkout Code
    └── Clone repository từ GitHub

Stage 2: Build Services
    ├── Build Discovery Server
    ├── Build API Gateway
    ├── Build User Service
    ├── Build Media Service
    ├── Build Chat Service
    ├── Build Channel Service
    ├── Build Friendship Service
    ├── Build Notification Service
    └── Build UI

Stage 3: Run Tests
    ├── Unit Tests
    ├── Integration Tests
    └── Code Quality Check (SonarQube - optional)

Stage 4: Build Docker Images
    ├── Build & Tag Images
    └── Push to Docker Registry

Stage 5: Deploy
    ├── Deploy to Development (auto)
    ├── Deploy to Staging (auto)
    └── Deploy to Production (manual approval)

Stage 6: Post-Deployment
    ├── Health Checks
    ├── Integration Tests
    └── Notifications (Slack/Email)
```

---

## 📊 MONITORING VÀ LOGGING

### Logging Stack (ELK Stack)

```yaml
# docker-compose.monitoring.yml
services:
  elasticsearch:
    image: elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data

  logstash:
    image: logstash:8.11.0
    volumes:
      - ./logstash/config:/usr/share/logstash/pipeline
    ports:
      - "5000:5000"
    depends_on:
      - elasticsearch

  kibana:
    image: kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_URL: http://elasticsearch:9200
    depends_on:
      - elasticsearch
```

### Monitoring Stack (Prometheus + Grafana)

```yaml
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    depends_on:
      - prometheus
```

### Configure Spring Boot Actuator

Thêm vào `application.yml` của mỗi service:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    health:
      show-details: always
```

---

## 🎯 BEST PRACTICES

### 1. Quản lý Configuration

- **Sử dụng Spring Cloud Config Server** cho cấu hình tập trung
- **Externalize configurations**: Không hardcode trong code
- **Environment-specific configs**: Dev, Staging, Production
- **Secret management**: Sử dụng HashiCorp Vault hoặc AWS Secrets Manager

### 2. Database Management

- **Database migrations**: Sử dụng Flyway hoặc Liquibase
- **Backup strategy**: 
  - Daily backups
  - Weekly full backups
  - Transaction logs backup
- **Database clustering**: High availability cho production

### 3. Service Resilience

- **Circuit Breaker**: Resilience4j cho fault tolerance
- **Rate Limiting**: Bảo vệ services khỏi overload
- **Health Checks**: Implement proper health endpoints
- **Graceful Shutdown**: Handle shutdown signals properly

### 4. Security

- **API Security**:
  - JWT tokens với short expiration
  - Refresh token rotation
  - Rate limiting per user/IP
- **Network Security**:
  - Private network cho services
  - Expose only necessary ports
  - Use firewall rules
- **Data Security**:
  - Encrypt sensitive data at rest
  - Use HTTPS/TLS for communication
  - Regular security audits

### 5. Performance Optimization

- **Caching**: Redis cho session, frequently accessed data
- **Database Indexing**: Proper indexes cho query performance
- **Connection Pooling**: Configure proper pool sizes
- **Async Processing**: Kafka cho long-running tasks

---

## 🐛 TROUBLESHOOTING

### Common Issues và Solutions

#### Issue 1: Service không kết nối được với Discovery Server

**Triệu chứng:**
```
com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server
```

**Solution:**
```bash
# 1. Kiểm tra Discovery Server đã chạy chưa
curl http://localhost:8761/actuator/health

# 2. Kiểm tra network connectivity
docker-compose exec user-service ping discovery-server

# 3. Kiểm tra configuration
docker-compose logs user-service | grep "eureka"

# 4. Restart service
docker-compose restart user-service
```

#### Issue 2: Keycloak connection refused

**Triệu chứng:**
```
Connection refused: keycloak:8080
```

**Solution:**
```bash
# 1. Kiểm tra Keycloak đã sẵn sàng chưa
docker-compose logs keycloak | grep "started"

# 2. Test connection từ service
docker-compose exec user-service curl http://keycloak:8080

# 3. Kiểm tra Keycloak realm configuration
# Truy cập http://localhost:8080 và verify realm "chat-apps"

# 4. Verify client configuration trong Keycloak
```

#### Issue 3: Database connection timeout

**Triệu chứng:**
```
org.springframework.jdbc.CannotGetJdbcConnectionException
```

**Solution:**
```bash
# 1. Kiểm tra database health
docker-compose ps | grep postgres
docker-compose logs postgres

# 2. Test connection
docker-compose exec postgres psql -U postgres_user -d postgres_db

# 3. Kiểm tra connection string trong service
docker-compose logs user-service | grep "datasource"

# 4. Increase connection timeout
# Thêm vào environment variables:
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=60000
```

#### Issue 4: Cassandra not ready

**Triệu chứng:**
```
com.datastax.oss.driver.api.core.AllNodesFailedException: All 1 node(s) tried for the query failed
```

**Solution:**
```bash
# 1. Đợi Cassandra khởi động hoàn toàn (có thể mất vài phút)
docker-compose logs -f cassandra

# 2. Verify Cassandra health
docker-compose exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

# 3. Check if keyspace created
docker-compose exec cassandra cqlsh -e "DESCRIBE KEYSPACE chatapps"

# 4. Restart chat-service sau khi Cassandra sẵn sàng
docker-compose restart chat-service
```

#### Issue 5: Kafka topic not found

**Triệu chứng:**
```
org.apache.kafka.common.errors.UnknownTopicOrPartitionException
```

**Solution:**
```bash
# 1. List existing topics
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:9092

# 2. Create topic manually if needed
docker-compose exec kafka kafka-topics --create \
  --topic chat-messages \
  --bootstrap-server kafka:9092 \
  --partitions 3 \
  --replication-factor 1

# 3. Verify topic created
docker-compose exec kafka kafka-topics --describe \
  --topic chat-messages \
  --bootstrap-server kafka:9092
```

#### Issue 6: Out of Memory

**Triệu chứng:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# 1. Increase JVM memory in docker-compose.yml
environment:
  JAVA_OPTS: "-Xmx4096m -Xms1024m"

# 2. Monitor memory usage
docker stats

# 3. Check for memory leaks
docker-compose logs service-name | grep "OutOfMemory"

# 4. Restart service with more memory
docker-compose up -d --force-recreate service-name
```

---

## 📚 TÀI LIỆU THAM KHẢO CHO CV

### Kiến thức và Kỹ năng đã áp dụng

**1. Microservices Architecture:**
- Service Discovery với Eureka
- API Gateway với Spring Cloud Gateway
- Inter-service communication với REST và Kafka
- Distributed tracing và monitoring

**2. Containerization & Orchestration:**
- Docker và Docker Compose
- Multi-stage builds
- Container networking và volumes
- Health checks và dependencies

**3. CI/CD Pipeline:**
- Jenkins pipeline as code
- Automated testing và deployment
- Docker registry integration
- Multi-environment deployment (Dev/Staging/Prod)

**4. Database Management:**
- PostgreSQL (Relational database)
- Cassandra (NoSQL - wide column store)
- MySQL (Keycloak persistence)
- Database migration strategies

**5. Message Queue:**
- Apache Kafka cho event-driven architecture
- Producer/Consumer patterns
- Topic management và partitioning

**6. Identity & Access Management:**
- Keycloak integration
- OAuth 2.0 và OpenID Connect
- JWT token management
- Role-based access control (RBAC)

**7. Monitoring & Logging:**
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Prometheus và Grafana
- Spring Boot Actuator
- Distributed logging với correlation IDs

**8. Security:**
- SSL/TLS configuration
- Secret management
- Network security với Docker networks
- API security với JWT

### Mô tả dự án cho CV

```
PROJECT: Real-time Chat Application - Microservices Architecture

Description:
Developed and deployed a production-ready real-time chat application using 
microservices architecture with 8+ independent services, serving thousands 
of concurrent users.

Technologies:
- Backend: Spring Boot, Spring Cloud (Gateway, Eureka), WebSocket
- Frontend: React, Vite
- Databases: PostgreSQL, Cassandra, MySQL
- Message Queue: Apache Kafka
- Identity Management: Keycloak (OAuth 2.0, OpenID Connect)
- DevOps: Docker, Docker Compose, Jenkins CI/CD
- Monitoring: ELK Stack, Prometheus, Grafana

Key Achievements:
✓ Designed and implemented microservices architecture with service discovery
✓ Built CI/CD pipeline with Jenkins for automated testing and deployment
✓ Implemented real-time messaging with WebSocket and Kafka
✓ Integrated Keycloak for enterprise-grade authentication and authorization
✓ Containerized all services with Docker and orchestrated with Docker Compose
✓ Set up monitoring and logging infrastructure with ELK Stack and Prometheus
✓ Achieved 99.9% uptime with proper health checks and fault tolerance

Responsibilities:
• Designed microservices architecture and defined service boundaries
• Implemented CI/CD pipeline using Jenkins for automated deployment
• Configured Keycloak for SSO and user management
• Set up monitoring infrastructure with Prometheus and Grafana
• Implemented distributed logging with ELK Stack
• Optimized database queries and caching strategies
• Wrote comprehensive deployment documentation and runbooks
```

---

## 🎓 CHECKLIST ĐỂ HOÀN THIỆN TRIỂN KHAI

### Pre-Production Checklist

- [ ] **Infrastructure**
  - [ ] All databases configured và backed up
  - [ ] Kafka topics created với proper partitioning
  - [ ] Keycloak realm configured với production settings
  - [ ] SSL certificates installed và configured

- [ ] **Application**
  - [ ] All services build successfully
  - [ ] Environment variables configured cho production
  - [ ] Secrets externalized và secured
  - [ ] Health checks implemented for all services

- [ ] **Security**
  - [ ] Default passwords changed
  - [ ] Firewall rules configured
  - [ ] API rate limiting enabled
  - [ ] Security headers configured

- [ ] **Monitoring**
  - [ ] Prometheus configured và collecting metrics
  - [ ] Grafana dashboards created
  - [ ] ELK Stack configured for log aggregation
  - [ ] Alerting rules configured

- [ ] **CI/CD**
  - [ ] Jenkins pipeline tested và working
  - [ ] Automated tests passing
  - [ ] Deployment automation verified
  - [ ] Rollback procedure tested

- [ ] **Documentation**
  - [ ] Deployment guide completed
  - [ ] Architecture documentation updated
  - [ ] Runbook for common issues created
  - [ ] API documentation published

### Post-Deployment Checklist

- [ ] All services registered with Eureka
- [ ] Health checks passing for all services
- [ ] Can login through Keycloak
- [ ] Can send and receive messages
- [ ] File upload working
- [ ] WebSocket connections stable
- [ ] Monitoring dashboards showing data
- [ ] Logs being collected properly

---

## 🚀 DEPLOYMENT COMMANDS QUICK REFERENCE

```bash
# Full deployment
docker-compose up -d

# Deploy specific service
docker-compose up -d --build service-name

# View logs
docker-compose logs -f service-name

# Check health
docker-compose ps
curl http://localhost:PORT/actuator/health

# Restart service
docker-compose restart service-name

# Stop all services
docker-compose down

# Clean up (⚠️ removes volumes)
docker-compose down -v

# Update service
docker-compose pull service-name
docker-compose up -d --build service-name

# Backup databases
docker-compose exec postgres pg_dump -U postgres_user postgres_db > backup.sql
```

---

**Chúc bạn triển khai thành công và đi thực tập thuận lợi! 🎉**

---

*Document version: 1.0*  
*Last updated: 2025-10-11*  
*Maintained by: Chat-Apps Team*
