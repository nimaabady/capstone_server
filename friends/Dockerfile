# ================================
# Build stage
# ================================
FROM gradle:8.6-jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper first (for caching dependencies)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./


# Fix permissions and line endings for gradlew
RUN chmod +x gradlew && \
    sed -i 's/\r$//' gradlew

# Download dependencies (cached if unchanged)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the jar
RUN ./gradlew clean bootJar --no-daemon

# ================================
# Runtime stage
# ================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (change to match your service)
EXPOSE 8092

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]