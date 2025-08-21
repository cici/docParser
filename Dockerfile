FROM openjdk:17-jdk-slim as builder

# Install basic build dependencies
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy gradle files and wrapper
COPY build.gradle settings.gradle gradlew ./
COPY gradle/ gradle/

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build application
RUN ./gradlew build --no-daemon -x test

# Production image
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy built jar
COPY --from=builder /app/build/libs/doc-parser-1.0.0.jar doc-parser-1.0.0.jar

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    chown appuser:appuser /app
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "doc-parser-1.0.0.jar"]
