# Use Eclipse Temurin OpenJDK 17 as base image (compatible with all platforms)
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built jar file into the container
COPY build/libs/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar file with optimized JVM settings for containers
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
