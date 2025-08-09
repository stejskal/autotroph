#!/bin/bash

# Script to build and test Docker image locally

set -e

echo "Building Docker image..."
docker build -t autotroph-service:latest .

echo "Docker image built successfully!"
echo "Image size:"
docker images autotroph-service:latest

echo ""
echo "To run the container locally:"
echo "docker run -p 8080:8080 \\"
echo "  -e NEO4J_URI=bolt://host.docker.internal:7687 \\"
echo "  -e NEO4J_USERNAME=neo4j \\"
echo "  -e NEO4J_PASSWORD=password \\"
echo "  -e NEO4J_DATABASE=neo4j \\"
echo "  autotroph-service:latest"

echo ""
echo "Or use docker-compose to run everything:"
echo "docker-compose up -d"
