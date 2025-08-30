#!/bin/bash
set -e # Dừng script ngay nếu có lỗi

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE db_userservice;
    CREATE DATABASE db_channelservice;
    CREATE DATABASE db_d;

    GRANT ALL PRIVILEGES ON DATABASE db_userservice TO dbuser;
    GRANT ALL PRIVILEGES ON DATABASE db_channelservice TO dbuser;
    GRANT ALL PRIVILEGES ON DATABASE db_d TO dbuser;
EOSQL
