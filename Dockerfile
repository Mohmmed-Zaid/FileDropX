# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-17 as build

WORKDIR /app
COPY . .

# Build the application
RUN mvnw clean package -DskipTests

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:17-jdk-alpine

# Copy the jar from build stage
COPY --from=build /app/target/FlieSharing-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=$PORT -jar /app.jar"]
