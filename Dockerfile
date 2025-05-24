# Stage 1: Build
FROM maven:3.8.6-openjdk-17 AS build

WORKDIR /app

# Copy your Maven wrapper scripts and pom.xml first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of your source code
COPY src ./src

# Build the jar without tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the jar
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
