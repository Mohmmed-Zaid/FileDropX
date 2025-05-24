# Stage 1: Build
# CHANGED: Using 'maven:3.9.6-openjdk-17' - this specific patch version with OpenJDK 17 is generally available.
# Stage 1: Build
# Alternative: Using 'maven:3-openjdk-17' - this is the most general Maven 3.x with OpenJDK 17 tag.
FROM maven:3-openjdk-17 AS build # <--- CHANGE THIS LINE IF OPTION 1 FAILS



# ... (rest of your Dockerfile remains the same)
WORKDIR /app

# Copy pom.xml and download dependencies (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
