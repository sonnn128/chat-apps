# ⚡ Quick Start Guide - Chat Apps

Hướng dẫn nhanh để chạy và triển khai ứng dụng chat-apps.

---

## 🚀 Chạy nhanh với Docker (Recommended)

### Development Mode
```bash
# Clone repository
git clone https://github.com/sonnn128/chat-apps.git
cd chat-apps

# Chạy tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f

# Truy cập ứng dụng
open http://localhost:5173
```

### Production Mode
```bash
# Cấu hình environment
cp .env.prod.example .env.prod
nano .env.prod  # Chỉnh sửa các giá trị

# Deploy production
docker-compose -f docker-compose.prod.yml up -d

# Kiểm tra health
./scripts/health-check.sh
```

---

## 🔧 Các lệnh thường dùng

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d user-service

# Stop all services
docker-compose down

# Restart service
docker-compose restart user-service

# View logs
docker-compose logs -f service-name

# Rebuild and start
docker-compose up -d --build service-name

# Check status
docker-compose ps

# Clean up everything (⚠️ removes volumes)
docker-compose down -v
```

### Health Checks

```bash
# Check all services
./scripts/health-check.sh

# Check specific service
curl http://localhost:9005/actuator/health

# Check Eureka dashboard
open http://localhost:8761

# Check API Gateway
curl http://localhost:8888/actuator/health
```

### Database Operations

```bash
# Backup databases
./scripts/backup-databases.sh

# Restore from backup
./scripts/restore-databases.sh backups/20250111_120000.tar.gz

# Access PostgreSQL
docker-compose exec postgres psql -U postgres_user -d postgres_db

# Access Cassandra
docker-compose exec cassandra cqlsh

# Access MySQL (Keycloak)
docker-compose exec keycloak-mysql mysql -u keycloak -p
```

### Kafka Operations

```bash
# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:9092

# Create topic
docker-compose exec kafka kafka-topics --create \
  --topic my-topic \
  --bootstrap-server kafka:9092 \
  --partitions 3 \
  --replication-factor 1

# Describe topic
docker-compose exec kafka kafka-topics --describe \
  --topic chat-messages \
  --bootstrap-server kafka:9092

# Consume messages
docker-compose exec kafka kafka-console-consumer \
  --topic chat-messages \
  --bootstrap-server kafka:9092 \
  --from-beginning
```

---

## 🎯 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| UI | http://localhost:5173 | Register new user |
| Keycloak Admin | http://localhost:8080 | admin / admin |
| Eureka Dashboard | http://localhost:8761 | No auth |
| API Gateway | http://localhost:8888 | Via JWT token |
| AKHQ (Kafka UI) | http://localhost:9999 | No auth |
| PostgreSQL | localhost:5432 | postgres_user / postgres_password |
| Cassandra | localhost:9042 | cassandra / cassandra |

---

## 🐛 Troubleshooting nhanh

### Service không start

```bash
# Xem logs chi tiết
docker-compose logs service-name

# Restart service
docker-compose restart service-name

# Rebuild image
docker-compose up -d --build service-name
```

### Database connection error

```bash
# Kiểm tra database đã chạy chưa
docker-compose ps | grep postgres

# Test connection
docker-compose exec postgres pg_isready -U postgres_user

# Restart database
docker-compose restart postgres
```

### Keycloak connection refused

```bash
# Đợi Keycloak khởi động (có thể mất 1-2 phút)
docker-compose logs -f keycloak

# Kiểm tra Keycloak health
curl http://localhost:8080

# Restart Keycloak
docker-compose restart keycloak
```

### Cassandra not ready

```bash
# Đợi Cassandra khởi động (có thể mất 2-3 phút)
docker-compose logs -f cassandra

# Kiểm tra keyspace
docker-compose exec cassandra cqlsh -e "DESCRIBE KEYSPACES"

# Restart chat-service sau khi Cassandra ready
docker-compose restart chat-service
```

### Port already in use

```bash
# Tìm process đang dùng port
lsof -i :8080  # hoặc port nào đang bị conflict

# Kill process
kill -9 <PID>

# Hoặc thay đổi port trong docker-compose.yml
```

### Out of memory

```bash
# Tăng memory cho Docker Desktop
# Settings > Resources > Memory: 8GB+

# Hoặc giảm số services chạy cùng lúc
docker-compose up -d postgres cassandra keycloak
docker-compose up -d discovery-server api-gateway
docker-compose up -d user-service chat-service
```

---

## 📋 Development Workflow

### 1. Chạy infrastructure services
```bash
docker-compose up -d postgres cassandra kafka zookeeper keycloak-mysql keycloak
```

### 2. Chạy configuration services
```bash
docker-compose up -d discovery-server api-gateway
```

### 3. Chạy business services
```bash
docker-compose up -d user-service media-service chat-service channel-service friendship-service notification-service
```

### 4. Chạy UI
```bash
docker-compose up -d ui
# Hoặc chạy local cho development:
cd ui
npm install
npm run dev
```

### 5. Make changes và rebuild
```bash
# Edit code...

# Rebuild specific service
docker-compose up -d --build user-service

# View logs
docker-compose logs -f user-service
```

---

## 🧪 Testing

### Manual Testing

```bash
# 1. Đăng ký user mới
curl -X POST http://localhost:8888/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#",
    "firstName": "Test",
    "lastName": "User"
  }'

# 2. Login
curl -X POST http://localhost:8888/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'

# 3. Get user profile (với token từ login)
curl http://localhost:8888/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Automated Testing

```bash
# Run unit tests
cd user-service
mvn test

# Run integration tests
mvn verify

# Run all tests for all services
for service in */pom.xml; do
  cd $(dirname $service)
  mvn test
  cd ..
done
```

---

## 🔄 CI/CD với Jenkins

### Setup Jenkins (One-time)

```bash
# Start Jenkins
docker run -d \
  --name jenkins \
  -p 8090:8080 \
  -v ~/jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts

# Get admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Access Jenkins
open http://localhost:8090
```

### Trigger Build

```bash
# Manual trigger từ Jenkins UI
# http://localhost:8090/job/chat-apps-pipeline/build

# Auto trigger qua Git push
git add .
git commit -m "feat: new feature"
git push origin main
# Jenkins sẽ tự động build
```

---

## 📊 Monitoring

### View Metrics

```bash
# Prometheus metrics
curl http://localhost:9005/actuator/prometheus

# Health check
curl http://localhost:9005/actuator/health

# Service info
curl http://localhost:9005/actuator/info
```

### View Logs

```bash
# Real-time logs
docker-compose logs -f service-name

# Last 100 lines
docker-compose logs --tail=100 service-name

# All logs
docker-compose logs service-name

# Follow multiple services
docker-compose logs -f user-service chat-service
```

---

## 🚨 Emergency Commands

### Stop everything
```bash
docker-compose down
```

### Stop and remove volumes (⚠️ loses data)
```bash
docker-compose down -v
```

### Restart specific service quickly
```bash
docker-compose restart service-name
```

### Force recreate service
```bash
docker-compose up -d --force-recreate service-name
```

### View resource usage
```bash
docker stats
```

### Clean up Docker
```bash
# Remove unused images
docker image prune -a

# Remove unused volumes
docker volume prune

# Remove everything unused
docker system prune -a --volumes
```

---

## 📚 More Information

- **Full Deployment Guide**: [DEPLOYMENT.md](./DEPLOYMENT.md)
- **Jenkins Setup**: [JENKINS_SETUP.md](./JENKINS_SETUP.md)
- **CV Guide**: [CV_GUIDE.md](./CV_GUIDE.md)
- **Architecture**: [feature.md](./feature.md)

---

## ⚡ TL;DR - Quickest Start

```bash
# 1. Clone
git clone https://github.com/sonnn128/chat-apps.git && cd chat-apps

# 2. Run
docker-compose up -d

# 3. Wait 2-3 minutes then access
open http://localhost:5173

# Done! 🎉
```

---

**Happy coding! 🚀**
