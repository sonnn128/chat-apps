#!/bin/bash

# Database Restore Script for Chat-Apps
# This script restores databases from backup files

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if backup file is provided
if [ $# -eq 0 ]; then
    echo -e "${RED}Error: No backup file specified${NC}"
    echo "Usage: $0 <backup-file.tar.gz>"
    echo ""
    echo "Available backups:"
    ls -lh ./backups/*.tar.gz 2>/dev/null || echo "  No backups found"
    exit 1
fi

BACKUP_FILE=$1

# Check if backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo -e "${RED}Error: Backup file not found: $BACKUP_FILE${NC}"
    exit 1
fi

echo "================================================"
echo "Starting Database Restore"
echo "Backup file: $BACKUP_FILE"
echo "================================================"
echo ""

# Warning
echo -e "${YELLOW}WARNING: This will overwrite existing databases!${NC}"
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Restore cancelled."
    exit 0
fi

# Extract backup
TEMP_DIR="./backups/temp_restore"
mkdir -p "$TEMP_DIR"
echo -e "${YELLOW}Extracting backup...${NC}"
tar -xzf "$BACKUP_FILE" -C "$TEMP_DIR"
BACKUP_DIR=$(find "$TEMP_DIR" -mindepth 1 -maxdepth 1 -type d | head -n 1)

# Restore PostgreSQL
if [ -f "$BACKUP_DIR/postgres_backup.sql" ]; then
    echo -e "${YELLOW}Restoring PostgreSQL...${NC}"
    docker-compose exec -T postgres psql -U postgres_user -d postgres_db < "$BACKUP_DIR/postgres_backup.sql"
    echo -e "${GREEN}✓ PostgreSQL restored${NC}"
else
    echo -e "${YELLOW}⚠ PostgreSQL backup not found in archive${NC}"
fi

# Restore Cassandra
if [ -d "$BACKUP_DIR/cassandra_data" ]; then
    echo -e "${YELLOW}Restoring Cassandra...${NC}"
    # Stop cassandra service
    docker-compose stop cassandra
    # Restore data
    docker cp "$BACKUP_DIR/cassandra_data/." $(docker-compose ps -q cassandra):/var/lib/cassandra/data
    # Start cassandra service
    docker-compose start cassandra
    echo -e "${GREEN}✓ Cassandra restored${NC}"
else
    echo -e "${YELLOW}⚠ Cassandra backup not found in archive${NC}"
fi

# Restore Keycloak MySQL
if [ -f "$BACKUP_DIR/keycloak_mysql_backup.sql" ]; then
    echo -e "${YELLOW}Restoring Keycloak MySQL...${NC}"
    docker-compose exec -T keycloak-mysql mysql -u keycloak -ppassword keycloak < "$BACKUP_DIR/keycloak_mysql_backup.sql"
    echo -e "${GREEN}✓ Keycloak MySQL restored${NC}"
else
    echo -e "${YELLOW}⚠ Keycloak MySQL backup not found in archive${NC}"
fi

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "================================================"
echo "Restore completed!"
echo "Please restart services: docker-compose restart"
echo "================================================"
