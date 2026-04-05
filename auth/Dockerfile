# ================================
# Build stage
# ================================
FROM gradle:8.6-jdk21 AS builder

WORKDIR /app

# Copy gradle files first (better cache usage)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Download dependencies
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source
COPY src src

# Build
RUN ./gradlew clean bootJar --no-daemon

# ================================
# Runtime stage
# ================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
