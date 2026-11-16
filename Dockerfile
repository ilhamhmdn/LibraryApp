# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Lean runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd -r appgroup \
    && useradd -r -g appgroup -u 1001 appuser \
    && mkdir -p /app/data \
    && chown -R appuser:appgroup /app

COPY --from=builder --chown=appuser:appgroup /workspace/target/*.jar app.jar

USER appuser:appgroup

ENV PORT=8080 \
    DB_URL="jdbc:h2:file:/app/data/librarydb;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1" \
    H2_CONSOLE_ENABLED="false"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD curl -f http://localhost:8080/v3/api-docs || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
