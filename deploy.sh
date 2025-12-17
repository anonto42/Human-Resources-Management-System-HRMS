#!/bin/bash

# Pull latest image
docker pull $DOCKER_USERNAME/your-app:latest

# Stop and remove old container
docker-compose down

# Start new container
docker-compose up -d

# Clean up old images
docker image prune -f