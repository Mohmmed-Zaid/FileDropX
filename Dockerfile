# Use a lightweight OpenJDK 17 Alpine image
FROM eclipse-temurin:17-jdk-alpine

# Copy the jar file into the container
COPY target/FlieSharing-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the jar, using the port provided by Render via $PORT environment variable
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar /app.jar"]
