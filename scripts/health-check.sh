#!/bin/bash

# Health Check Script for Chat-Apps Services
# This script checks the health of all services in the chat-apps application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
RETRY_COUNT=5
RETRY_DELAY=10

# Services to check
declare -A SERVICES=(
    ["Discovery Server"]="http://localhost:8761/actuator/health"
    ["API Gateway"]="http://localhost:8888/actuator/health"
    ["User Service"]="http://localhost:9005/actuator/health"
    ["Media Service"]="http://localhost:9006/actuator/health"
    ["Chat Service"]="http://localhost:9007/actuator/health"
    ["Channel Service"]="http://localhost:9008/actuator/health"
    ["Friendship Service"]="http://localhost:9009/actuator/health"
    ["Notification Service"]="http://localhost:8889/actuator/health"
    ["Keycloak"]="http://localhost:8080"
    ["UI"]="http://localhost:5173"
)

echo "================================================"
echo "Starting Health Check for Chat-Apps Services"
echo "================================================"
echo ""

failed_services=()
success_count=0
total_count=${#SERVICES[@]}

# Function to check service health
check_service() {
    local service_name=$1
    local service_url=$2
    local retry=0
    
    echo -n "Checking $service_name... "
    
    while [ $retry -lt $RETRY_COUNT ]; do
        if curl -f -s -o /dev/null -w "%{http_code}" "$service_url" | grep -q "200"; then
            echo -e "${GREEN}✓ HEALTHY${NC}"
            return 0
        fi
        
        retry=$((retry + 1))
        if [ $retry -lt $RETRY_COUNT ]; then
            sleep $RETRY_DELAY
        fi
    done
    
    echo -e "${RED}✗ UNHEALTHY${NC} (tried $RETRY_COUNT times)"
    return 1
}

# Check each service
for service_name in "${!SERVICES[@]}"; do
    service_url="${SERVICES[$service_name]}"
    
    if check_service "$service_name" "$service_url"; then
        success_count=$((success_count + 1))
    else
        failed_services+=("$service_name")
    fi
done

echo ""
echo "================================================"
echo "Health Check Summary"
echo "================================================"
echo "Total Services: $total_count"
echo -e "${GREEN}Healthy: $success_count${NC}"
echo -e "${RED}Unhealthy: ${#failed_services[@]}${NC}"

if [ ${#failed_services[@]} -gt 0 ]; then
    echo ""
    echo -e "${RED}Failed Services:${NC}"
    for service in "${failed_services[@]}"; do
        echo "  - $service"
    done
    echo ""
    echo "Please check the logs for failed services:"
    echo "  docker-compose logs <service-name>"
    exit 1
else
    echo ""
    echo -e "${GREEN}All services are healthy! ✓${NC}"
    exit 0
fi
