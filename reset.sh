#!/bin/bash

# Get the ID of the rabbitmq container
RABBITMQ_ID=$(docker ps -aqf "name=rabbitmq")

# Stop all running containers except rabbitmq
echo "Stopping all running containers except rabbitmq..."
docker stop $(docker ps -q | grep -v $RABBITMQ_ID)

# Remove all containers except rabbitmq
echo "Removing all containers except rabbitmq..."
docker rm $(docker ps -a -q | grep -v $RABBITMQ_ID)

# Get the ID of the rabbitmq image
RABBITMQ_IMAGE_ID=$(docker images -q rabbitmq)

# Remove all images except rabbitmq
echo "Removing all images except rabbitmq..."
docker rmi $(docker images -q | grep -v $RABBITMQ_IMAGE_ID)

echo "All containers stopped and images removed, except for rabbitmq."