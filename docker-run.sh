#!/bin/bash

echo "🔨 Building the application..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "🐳 Starting Docker Compose..."
docker-compose up --build

