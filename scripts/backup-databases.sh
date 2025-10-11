#!/bin/bash

# Database Backup Script for Chat-Apps
# This script creates backups of PostgreSQL, Cassandra, and MySQL databases

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
RETENTION_DAYS=7

echo "================================================"
echo "Starting Database Backup"
echo "================================================"
echo ""

# Create backup directory
mkdir -p "$BACKUP_DIR"

# Backup PostgreSQL
echo -e "${YELLOW}Backing up PostgreSQL...${NC}"
docker-compose exec -T postgres pg_dump -U postgres_user postgres_db > "$BACKUP_DIR/postgres_backup.sql"
echo -e "${GREEN}✓ PostgreSQL backup completed${NC}"

# Backup Cassandra
echo -e "${YELLOW}Backing up Cassandra...${NC}"
# Take snapshot
docker-compose exec -T cassandra nodetool snapshot chatapps
# Copy snapshot files
docker cp $(docker-compose ps -q cassandra):/var/lib/cassandra/data "$BACKUP_DIR/cassandra_data"
echo -e "${GREEN}✓ Cassandra backup completed${NC}"

# Backup Keycloak MySQL
echo -e "${YELLOW}Backing up Keycloak MySQL...${NC}"
docker-compose exec -T keycloak-mysql mysqldump -u keycloak -ppassword keycloak > "$BACKUP_DIR/keycloak_mysql_backup.sql"
echo -e "${GREEN}✓ Keycloak MySQL backup completed${NC}"

# Compress backups
echo -e "${YELLOW}Compressing backups...${NC}"
tar -czf "$BACKUP_DIR.tar.gz" -C ./backups "$(basename $BACKUP_DIR)"
rm -rf "$BACKUP_DIR"
echo -e "${GREEN}✓ Backup compressed: $BACKUP_DIR.tar.gz${NC}"

# Clean old backups
echo -e "${YELLOW}Cleaning old backups (older than $RETENTION_DAYS days)...${NC}"
find ./backups -name "*.tar.gz" -type f -mtime +$RETENTION_DAYS -delete
echo -e "${GREEN}✓ Old backups cleaned${NC}"

echo ""
echo "================================================"
echo "Backup completed successfully!"
echo "Backup location: $BACKUP_DIR.tar.gz"
echo "================================================"
