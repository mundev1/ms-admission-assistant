# Multi-stage build for Spring Boot application
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy pom.xml and source
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src

# Package jar without running tests during image build
RUN mvn -f backend/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /build/backend/target/*.jar app.jar

# Railway injects the PORT env var (defaults to 8080)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
