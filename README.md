# Card Subscription Service

A production-grade Spring Boot microservice built with **DDD** and **Clean Architecture** principles.  
Consumes card events from Kafka, calls an external subscription API, and exposes a REST API to query results.

---

## Architecture

The project is a **Maven multi-module** application. Each module has a single, well-defined responsibility and a strict dependency direction — outer layers depend on inner layers, never the reverse.

```
┌─────────────────────────────────────────────────────────┐
│                        exposition                        │  ← Spring Boot entry point
│         REST controllers · DTOs · Converters            │
└──────────────┬──────────────────────────┬───────────────┘
               │                          │
┌──────────────▼──────────┐  ┌────────────▼──────────────┐
│     kafka-consumer      │  │       infrastructure       │
│  Listener · DLT · Config│  │  JPA Persistence · HTTP    │
└──────────────┬──────────┘  └────────────┬──────────────┘
               │                          │
        ┌──────▼──────────────────────────▼──────┐
        │               application               │
        │  Use cases (ports/in) · Services        │
        │  Outbound ports (ports/out)             │
        └──────────────────┬─────────────────────┘
                           │
                  ┌────────▼────────┐
                  │     domain      │  ← Zero dependencies
                  │  Model · Enums  │
                  │  Exceptions     │
                  └─────────────────┘
```

### Module breakdown

| Module           | Depends on                              | Responsibility                                              |
|------------------|-----------------------------------------|-------------------------------------------------------------|
| `domain`         | —                                       | `CardSubscription` aggregate, `SubscriptionStatus`, domain exceptions. Pure Java, no frameworks. |
| `application`    | `domain`                                | Use-case interfaces (`SubscribeCardUseCase`, `GetCardSubscriptionUseCase`), outbound port interfaces, `CardSubscriptionService`. |
| `infrastructure` | `application`                           | JPA entity, Spring Data repository, persistence adapter, external HTTP API adapter (RestTemplate). |
| `kafka-consumer` | `application`                           | `CardEventKafkaConsumer`, `KafkaConsumerConfig` (DLT + exponential back-off), `CardEventMessage` DTO. |
| `exposition`     | `application` · `infrastructure` · `kafka-consumer` | `CardSubscriptionController`, request/response DTOs, converters, `GlobalExceptionHandler`, Spring Boot main class. |

---

## Key Design Decisions

**Domain is framework-free.**  
`domain/pom.xml` has zero dependencies. `CardSubscription` uses a hand-written builder and plain getters/setters — it compiles with bare `javac`. No Lombok, no Spring, no JPA annotations leak into the domain.

**Kafka is isolated in its own module.**  
All messaging concerns (consumer config, DLT strategy, retry, Kafka DTOs) live exclusively in `kafka-consumer`. The rest of the application has no Kafka dependency. Swapping the messaging technology only touches this module.

**Converters are first-class citizens in exposition.**  
`CardSubscriptionResponseConverter` and `SubscribeCardRequestConverter` are dedicated `@Component` classes under `exposition/converter/`. The controller stays thin and delegates all mapping — both are independently unit-testable.

**Idempotent subscription.**  
`CardSubscriptionService.subscribe()` checks for an existing active subscription before calling the external API. Safe to call multiple times with the same `cardId`.

**DLT with exponential back-off.**  
After 3 retry attempts (1 s → 2 s → 4 s), the message is published to `<topic>.DLT` via `DeadLetterPublishingRecoverer`. Each retry is logged with topic, partition, and offset.

---

## Project Structure

```
card-subscription/
├── Dockerfile
├── docker/
│   └── docker-compose.yml          # Kafka + Zookeeper + Kafka UI
├── domain/
│   └── src/main/java/.../domain/
│       ├── model/CardSubscription.java
│       ├── model/SubscriptionStatus.java
│       └── exception/CardSubscriptionNotFoundException.java
├── application/
│   └── src/main/java/.../application/
│       ├── command/SubscribeCardCommand.java
│       ├── port/in/SubscribeCardUseCase.java
│       ├── port/in/GetCardSubscriptionUseCase.java
│       ├── port/out/SaveCardSubscriptionPort.java
│       ├── port/out/LoadCardSubscriptionPort.java
│       ├── port/out/CardApiPort.java
│       └── service/CardSubscriptionService.java
├── infrastructure/
│   └── src/main/java/.../infrastructure/
│       ├── persistence/entity/CardSubscriptionEntity.java
│       ├── persistence/repository/CardSubscriptionJpaRepository.java
│       ├── persistence/adapter/CardSubscriptionPersistenceAdapter.java
│       ├── persistence/adapter/CardSubscriptionEntityMapper.java  (MapStruct)
│       ├── api/dto/SubscribeCardApiRequest.java
│       ├── api/dto/SubscribeCardApiResponse.java
│       ├── api/adapter/ExternalCardApiAdapter.java
│       └── config/RestTemplateConfig.java
├── kafka-consumer/
│   └── src/main/java/.../kafka/
│       ├── config/KafkaConsumerConfig.java
│       ├── dto/CardEventMessage.java
│       └── consumer/CardEventKafkaConsumer.java
└── exposition/
    └── src/main/java/.../
        ├── CardSubscriptionApplication.java
        └── exposition/
            ├── controller/CardSubscriptionController.java
            ├── dto/request/SubscribeCardRequest.java
            ├── dto/response/CardSubscriptionResponse.java
            ├── converter/CardSubscriptionResponseConverter.java
            ├── converter/SubscribeCardRequestConverter.java
            ├── exception/GlobalExceptionHandler.java
            └── exception/ApiError.java
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose

### 1. Start local infrastructure

```bash
docker compose -f docker/docker-compose.yml up -d
```

This starts:
- **Kafka** on `localhost:9092`
- **Zookeeper** on `localhost:2181`
- **Kafka UI** on [http://localhost:8090](http://localhost:8090)

### 2. Build the project

```bash
mvn clean package -DskipTests
```

### 3. Run the application

```bash
java -jar exposition/target/exposition-*.jar
```

Or with Maven:

```bash
mvn spring-boot:run -pl exposition
```

The application starts on **http://localhost:8080**.

### 4. Run with Docker

The `Dockerfile` uses a **3-stage build**:

| Stage       | Base image                        | Purpose                                               |
|-------------|-----------------------------------|-------------------------------------------------------|
| `builder`   | `maven:3.9.6-eclipse-temurin-21`  | Compiles all modules, produces the fat jar            |
| `extractor` | `eclipse-temurin:21-jre`          | Splits the fat jar into 4 Spring Boot layers          |
| _(runtime)_ | `eclipse-temurin:21-jre`          | Copies layers in order; runs as non-root `appuser`    |

The layered JAR means a code-only change rebuilds only the thin **application** layer (~a few KB), leaving the 100 MB+ of dependencies untouched in the Docker cache.

```bash
# Build the image
docker build -t card-subscription-service:latest .

# Run (requires Kafka accessible from the container)
docker run -p 8080:8080 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  -e EXTERNAL_CARD_API_BASE_URL=http://host.docker.internal:8081 \
  card-subscription-service:latest
```

---

## REST API

Base URL: `http://localhost:8080/api/v1/card-subscriptions`

### Subscribe a card

```http
POST /api/v1/card-subscriptions
Content-Type: application/json

{
  "cardId": "CARD-001",
  "userId": "USER-123"
}
```

**Response `201 Created`:**
```json
{
  "id": "a3f1c2d4-...",
  "cardId": "CARD-001",
  "userId": "USER-123",
  "status": "SUBSCRIBED",
  "requestedAt": "2024-05-10T10:00:00",
  "processedAt": "2024-05-10T10:00:01",
  "errorMessage": null
}
```

### Get subscription by ID

```http
GET /api/v1/card-subscriptions/{id}
```

### Get subscription by card ID

```http
GET /api/v1/card-subscriptions/card/{cardId}
```

### Error responses

All errors return a structured `ApiError` body:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No subscription found for cardId: CARD-999",
  "timestamp": "2024-05-10T10:05:00",
  "details": []
}
```

---

## Kafka

### Topic consumed

| Topic         | Consumer group                        | DLT                  |
|---------------|---------------------------------------|----------------------|
| `card.events` | `card-subscription-consumer-group`    | `card.events.DLT`    |

### Message format (`CardEventMessage`)

```json
{
  "eventId": "evt-001",
  "eventType": "CARD_CREATED",
  "cardId": "CARD-001",
  "userId": "USER-123",
  "eventTimestamp": "2024-05-10T10:00:00"
}
```

### Produce a test event

```bash
docker exec -it kafka kafka-console-producer \
  --broker-list localhost:9092 \
  --topic card.events
```

Then paste:
```json
{"eventId":"evt-001","eventType":"CARD_CREATED","cardId":"CARD-001","userId":"USER-123","eventTimestamp":"2024-05-10T10:00:00"}
```

---

## Configuration

Key properties in `exposition/src/main/resources/application.yml`:

| Property                            | Default                        | Description                          |
|-------------------------------------|--------------------------------|--------------------------------------|
| `server.port`                       | `8080`                         | HTTP port                            |
| `spring.kafka.bootstrap-servers`    | `localhost:9092`               | Kafka broker address                 |
| `spring.kafka.consumer.group-id`    | `card-subscription-consumer-group` | Consumer group ID                |
| `kafka.topics.card-events`          | `card.events`                  | Source topic                         |
| `external.card-api.base-url`        | `http://localhost:8081`        | External subscription API base URL   |
| `external.card-api.subscribe-path`  | `/api/v1/subscribe-new-card`   | Subscription API endpoint path       |

Override any property via environment variable using Spring's relaxed binding, e.g.:

```bash
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 java -jar app.jar
```

---

## Running Tests

```bash
# All tests
mvn test

# Unit tests only (no Kafka / DB required)
mvn test -pl exposition -Dtest="**/unit/**"
```

---

## Tech Stack

| Layer       | Technology                                  |
|-------------|---------------------------------------------|
| Language    | Java 21                                     |
| Framework   | Spring Boot 3.2.5                           |
| Messaging   | Spring Kafka 3.x, `DefaultErrorHandler`, DLT |
| Persistence | Spring Data JPA, H2 (dev), PostgreSQL (prod) |
| Mapping     | MapStruct 1.5.5 (infrastructure only)       |
| Build       | Maven 3.9, multi-module                     |
| Container   | Docker (multi-stage build, non-root user)   |
