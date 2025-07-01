#!/bin/bash
set -e
echo ">>> Dọn dẹp các container cũ (nếu có)..."
docker-compose down -v --remove-orphans
echo ">>> Bước 1: Build các project Spring Boot bằng Maven..."
mvn -f discovery-server/pom.xml clean package -DskipTests
mvn -f api-gateway/pom.xml clean package -DskipTests
mvn -f services/auth-service/pom.xml clean package -DskipTests
mvn -f services/product-service/pom.xml clean package -DskipTests
mvn -f services/notification-service/pom.xml clean package -DskipTests
echo ">>> Bước 2: Build image và khởi chạy container với Docker Compose..."
docker-compose up --build -d
echo ""
echo ">>> Hệ thống đang khởi động... Vui lòng chờ khoảng 30-60 giây."
echo ">>> Bạn có thể theo dõi log bằng lệnh: docker-compose logs -f"
echo ""
echo "✅ HOÀN TẤT! Truy cập các địa chỉ sau:"
echo "----------------------------------------------------"
echo "-> React Client:         http://localhost:5173"
echo "-> Eureka Dashboard:     http://localhost:8761"
echo "----------------------------------------------------"
echo ">>> KIỂM TRA PHÂN QUYỀN:"
echo " 1. Đăng nhập với username 'user'."
echo " 2. Nhấn 'Create Product (Admin Only)' -> THẤT BẠI (Lỗi 403 Forbidden)."
echo " 3. Đăng xuất, đăng nhập với username 'admin'."
echo " 4. Nhấn 'Create Product (Admin Only)' -> THÀNH CÔNG."
echo "----------------------------------------------------"
