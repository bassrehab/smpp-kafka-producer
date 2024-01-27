# Multi-stage Dockerfile for SMPP-Kafka Producer
# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first (better layer caching)
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY settings ./settings
COPY run.sh .

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Create non-root user for security
RUN addgroup -g 1000 smpp && \
    adduser -u 1000 -G smpp -s /bin/sh -D smpp

WORKDIR /app

# Copy artifacts from builder
COPY --from=builder /build/out/smpp-producer/*.jar ./
COPY --from=builder /build/out/smpp-producer/settings ./settings/

# Set ownership
RUN chown -R smpp:smpp /app

# Switch to non-root user
USER smpp

# Default JVM options (can be overridden via JAVA_OPTS)
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# Expose ports
# SMPP server port (configurable via -p flag)
EXPOSE 2775
# Metrics/health endpoint
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:9090/health/live || exit 1

# Entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
    -Dconfig.properties=settings/config.properties \
    -Dconfig.smpp=settings/context.xml \
    -Dlog4j2.configurationFile=file:settings/log4j2.xml \
    -jar smpp-kafka-producer-*-spring-boot.jar $@"]

# Default command (SMPP port)
CMD ["-p", "2775"]
