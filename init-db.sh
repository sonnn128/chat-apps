#!/bin/bash
set -e # Dừng script ngay nếu có lỗi

# Chạy lệnh psql với user mặc định (postgres) hoặc user đã được tạo
# Kết nối vào database mặc định (được tạo bởi POSTGRES_DB) để thực thi các lệnh SQL
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Tạo các database còn lại
    CREATE DATABASE db_userservice;
    CREATE DATABASE db_c;
    CREATE DATABASE db_d;

    -- Cấp toàn bộ quyền trên các database mới cho user 'dbuser'
    GRANT ALL PRIVILEGES ON DATABASE db_b TO dbuser;
    GRANT ALL PRIVILEGES ON DATABASE db_c TO dbuser;
    GRANT ALL PRIVILEGES ON DATABASE db_d TO dbuser;
EOSQL