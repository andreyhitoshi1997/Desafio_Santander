#!/bin/bash
# Script to build Spring Boot project and start Docker Compose for Redis and Spring Boot app

# Build the project with Gradle
./gradlew clean build

# Start Docker Compose
docker-compose up --build
