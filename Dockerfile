# Stage 1: Build
# CHANGED: Using 'maven:3.9-openjdk-17' because '3.9.4-openjdk-17' is not available.
# This ensures you get Maven 3.9.x with OpenJDK 17.
FROM maven:3.9-openjdk-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Using a slim OpenJDK 17 for a smaller final image size
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory (ensure it exists for file storage)
RUN mkdir -p /app/uploads

# Expose port 8080, which is the default for Spring Boot applications
EXPOSE 8080

# Command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
