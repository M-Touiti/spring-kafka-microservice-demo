# =============================================================================
# Stage 1 – Build
# Full Maven + JDK image. Compiles and packages all modules.
# POMs are copied before sources to cache the dependency download layer.
# =============================================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY domain/pom.xml          domain/pom.xml
COPY application/pom.xml     application/pom.xml
COPY infrastructure/pom.xml  infrastructure/pom.xml
COPY kafka-consumer/pom.xml  kafka-consumer/pom.xml
COPY exposition/pom.xml      exposition/pom.xml

# Download all dependencies – layer is reused unless a pom.xml changes
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY domain/src          domain/src
COPY application/src     application/src
COPY infrastructure/src  infrastructure/src
COPY kafka-consumer/src  kafka-consumer/src
COPY exposition/src      exposition/src

# Tests should be run in CI before building the image (-DskipTests here)
RUN mvn clean package -DskipTests -B --no-transfer-progress

# =============================================================================
# Stage 2 – Extract Spring Boot layered JAR
# Spring Boot splits the fat-jar into four cache-friendly layers so that a
# code-only change only invalidates the thin "application" layer (~a few KB),
# not the 100 MB+ of dependencies.
# =============================================================================
FROM eclipse-temurin:21-jre AS extractor

WORKDIR /extract
COPY --from=builder /build/exposition/target/exposition-*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# =============================================================================
# Stage 3 – Runtime image
# Minimal JRE. Each COPY is a distinct Docker layer ordered least → most
# volatile, so only the "application" layer is pushed on normal code changes.
# =============================================================================
FROM eclipse-temurin:21-jre

LABEL maintainer="card-subscription-team"
LABEL org.opencontainers.image.title="card-subscription-service"
LABEL org.opencontainers.image.description="Card Subscription Microservice – DDD / Clean Architecture"
LABEL org.opencontainers.image.source="https://github.com/your-org/card-subscription"

# Non-root user – security best practice
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

# Layer order: libraries change rarely, application code changes often
COPY --from=extractor /extract/dependencies           ./
COPY --from=extractor /extract/spring-boot-loader     ./
COPY --from=extractor /extract/snapshot-dependencies  ./
COPY --from=extractor /extract/application            ./

RUN chown -R appuser:appgroup /app
USER appuser

# Container-aware JVM flags
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
