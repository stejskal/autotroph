#!/bin/bash

# Script to run the Autotroph service

echo "Starting Autotroph Neo4j Service..."

# Check if Neo4j is running
if ! curl -s http://localhost:7474 > /dev/null; then
    echo "Warning: Neo4j doesn't seem to be running on localhost:7474"
    echo "Please start Neo4j first:"
    echo "  docker-compose up -d"
    echo "  or"
    echo "  docker run --name neo4j -p7474:7474 -p7687:7687 -d --env NEO4J_AUTH=neo4j/password neo4j:latest"
    echo ""
    echo "Continuing anyway..."
fi

# Run the application
echo "Running with Java version:"
java -version

echo ""
echo "Starting Ktor application..."
./gradlew run
