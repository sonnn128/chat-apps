#!/bin/bash

# Script này nên được đặt trên server deploy hoặc copy qua trong quá trình deploy
# Usage: ./deploy.sh <dockerhub_username>

DOCKERHUB_USERNAME=$1

if [ -z "$DOCKERHUB_USERNAME" ]; then
  echo "Usage: ./deploy.sh <dockerhub_username>"
  exit 1
fi

export DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME

echo "Deploying for user ${DOCKERHUB_USERNAME}..."

# Pull images mới nhất
docker-compose -f docker-compose.prod.yml pull

# Recreate containers
docker-compose -f docker-compose.prod.yml up -d

# Clean up unused images
docker image prune -f

echo "Deployment completed!"
