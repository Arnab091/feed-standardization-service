# Feed Standardization Service

## Overview

This service accepts betting feed payloads from multiple external providers, validates them, normalizes them into a common internal message format, and publishes the result through a queue abstraction.

At the moment, the service supports:

- `provider-alpha`
- `provider-beta`

Supported feed categories:

- odds updates
- settlement updates

The current queue implementation is a mock sender: instead of pushing to a real broker, it logs the normalized message.

## Tech stack

- Java (the project currently declares `java.version=25` in `pom.xml`)
- Spring Boot `4.0.6`
- Maven Wrapper (`./mvnw`)
- Spring MVC + Bean Validation

## Project structure

- `src/main/java/com/sporty/feedstandardizationservice/rest` - HTTP controllers and error handling
- `src/main/java/com/sporty/feedstandardizationservice/message/external` - provider-specific request DTOs
- `src/main/java/com/sporty/feedstandardizationservice/message/internal` - normalized internal message types
- `src/main/java/com/sporty/feedstandardizationservice/queue` - queue publishing abstraction and mock implementation
- `src/test/java/.../rest/ProviderFeedControllerTest.java` - controller-level examples and regression tests

## Prerequisites

Before running the service, make sure you have:

- a compatible JDK installed
- `JAVA_HOME` pointing to that JDK if your shell requires it

The repository includes the Maven Wrapper, so you do **not** need to install Maven separately.
If you are running from an already packaged JAR instead of building from source, Maven is not required at all.

## How to run the application

From the project root:

```bash
./mvnw spring-boot:run
```

The service will start with Spring Boot's default settings.
Unless you override it, the HTTP server will be available at:

```text
http://localhost:8080
```

### Build a jar

```bash
./mvnw clean package
```

Run the packaged application:

```bash
java -jar target/feed-standardization-service-0.0.1-SNAPSHOT.jar
```

### Run from an already packaged JAR

If someone has already provided the built JAR file, you can run the service without the source build step.
You only need a compatible JDK on the machine.

For example, if the JAR is already present in the project's `target/` directory:

```bash
java -jar target/feed-standardization-service-0.0.1-SNAPSHOT.jar
```

If the JAR is stored somewhere else on your machine, use its full path:

```bash
java -jar /absolute/path/to/feed-standardization-service-0.0.1-SNAPSHOT.jar
```

You can launch that command from any directory. Once started, the service is available on the default port unless overridden:

```text
http://localhost:8080
```

## How to test the application

Run the automated test suite:

```bash
./mvnw test
```

## API summary

### Base behavior

Both provider endpoints:

- accept `POST` requests
- consume `application/json`
- return `202 Accepted` for valid payloads
- return Problem Details JSON for malformed or invalid requests

### Endpoint: Provider Alpha

**URL**

```text
POST /provider-alpha/feed
```

Provider Alpha uses `msg_type` as the payload discriminator.

#### Alpha odds update example

```bash
curl -i \
  -X POST 'http://localhost:8080/provider-alpha/feed' \
  -H 'Content-Type: application/json' \
  -d '{
    "msg_type": "odds_update",
    "event_id": "match-1",
    "values": {
      "1": 1.1,
      "X": 2.2,
      "2": 3.3
    }
  }'
```

#### Alpha settlement example

```bash
curl -i \
  -X POST 'http://localhost:8080/provider-alpha/feed' \
  -H 'Content-Type: application/json' \
  -d '{
    "msg_type": "settlement",
    "event_id": "match-3",
    "outcome": "X"
  }'
```

#### Alpha payload rules

- `msg_type` must be one of:
  - `odds_update`
  - `settlement`
- `event_id` is required
- for odds updates, `values.1`, `values.X`, and `values.2` are required
- for settlement updates, `outcome` must be one of `1`, `X`, or `2`

### Endpoint: Provider Beta

**URL**

```text
POST /provider-beta/feed
```

Provider Beta uses `type` as the payload discriminator.

#### Beta odds update example

```bash
curl -i \
  -X POST 'http://localhost:8080/provider-beta/feed' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "ODDS",
    "event_id": "match-4",
    "odds": {
      "home": 1.9,
      "draw": 2.8,
      "away": 4.7
    }
  }'
```

#### Beta settlement example

```bash
curl -i \
  -X POST 'http://localhost:8080/provider-beta/feed' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "SETTLEMENT",
    "event_id": "match-2",
    "result": "away"
  }'
```

#### Beta payload rules

- `type` must be one of:
  - `ODDS`
  - `SETTLEMENT`
- `event_id` is required
- for odds updates, `odds.home`, `odds.draw`, and `odds.away` are required
- for settlement updates, `result` must be one of `home`, `draw`, or `away`

## Successful response format

When a payload is accepted, the API returns a response like this:

```json
{
  "status": "accepted",
  "provider": "provider-alpha",
  "eventId": "match-1",
  "feedType": "odds_update"
}
```

## Internal normalization

The controllers convert external payloads into these internal messages before publishing:

### Odds update

```text
OddsUpdateMessage(eventId, home, draw, away)
```

### Settlement

```text
SettlementMessage(eventId, outCome)
```

Mapping examples:

- Alpha `values.1` -> internal `home`
- Alpha `values.X` -> internal `draw`
- Alpha `values.2` -> internal `away`
- Alpha outcome `1|X|2` -> internal `HOME|DRAW|AWAY`
- Beta outcome `home|draw|away` -> internal `HOME|DRAW|AWAY`

## Error handling

Invalid requests are returned as Problem Details responses.
The service currently distinguishes cases such as:

- malformed JSON -> `400 Bad Request`
- unsupported feed type discriminator -> `422 Unprocessable Content`
- invalid enum/value format -> `422 Unprocessable Content`
- validation failures for missing required fields -> `422 Unprocessable Content`

### Example validation error

```json
{
  "status": 422,
  "title": "Request validation failed",
  "errorCategory": "VALIDATION_FAILED",
  "violations": [
    {
      "field": "result",
      "message": "result is required",
      "rejectedValue": "null"
    }
  ]
}
```

### Example malformed JSON error

```json
{
  "status": 400,
  "title": "Malformed JSON request",
  "errorCategory": "MALFORMED_JSON"
}
```

## Logs and mock queue behavior

The default `MockQueueSender` is registered as a Spring service.
When a request is accepted, the normalized message is written to the application logs instead of being sent to Kafka, RabbitMQ, or another real message broker.

## Useful commands

```bash
./mvnw test
./mvnw spotless:check
./mvnw spotless:apply
./mvnw spring-boot:run
./mvnw clean package
java -jar target/feed-standardization-service-0.0.1-SNAPSHOT.jar
java -jar /absolute/path/to/feed-standardization-service-0.0.1-SNAPSHOT.jar
```

## Notes

- The API behavior shown above is based on the current controllers and automated tests in the repository.
- If you later replace `MockQueueSender` with a real broker integration, the HTTP contract can remain the same while the publishing implementation changes.


