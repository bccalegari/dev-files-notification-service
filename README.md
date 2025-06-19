# Notification Service

> **Study project developed for learning purposes.**

This is a Spring Boot microservice responsible for handling email notifications in the **DevFiles** platform.  
It listens to a RabbitMQ queue and sends transactional emails using the SendGrid API.

The service is containerized with Docker and integrated into the full application via Docker Compose.

> This repository/module is part of the [DevFiles Monorepo](https://github.com/bccalegari/dev-files-monorepo).

## Features

- Consumes messages from RabbitMQ
- Sends emails using the SendGrid API
- Event-driven architecture
- Configurable email templates

## Technologies Used

- Java 21
- Spring Boot 3.4.3
- Spring AMQP (RabbitMQ)
- Spring Mail (SendGrid)
- Thymeleaf (for email templates)
- Jackson

## Testing
- Unit tests with JUnit 5
- Integration tests with Testcontainers for RabbitMQ and SendGrid
- Mocking with Mockito
- Spring Boot Test

## Prerequisites
- Docker installed
- dev-files-api compose file running (for RabbitMQ)
- devfiles-network and devfiles-rabbitmq networks created

```bash
docker network create devfiles-network
docker network create devfiles-rabbitmq
```

## Running locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/bccalegari/dev-files-notification-service.git
   ````

2. **Navigate to the project directory:**
   ```bash
    cd dev-files-notification-service
    ```

3. **Create a `.env` file:**
    ```bash
    cp .env.example .env
    ```
    Copy the `.env.example` to `.env` and fill in the required environment variables.

4. **Run dev compose:**
    ```bash
    docker compose -f docker-compose.dev.yml up
    ```

5. **Run the application:**
    The application will start automatically with the Docker Compose setup. You can access the logs to see if it's running correctly.

## Integration with DevFiles API
This service is integrated with the main dev-files-api service through RabbitMQ. The API publishes events to a RabbitMQ queue that this notification service listens to. When an event is received, the service processes it and sends the appropriate email notification.

## Logging
Logs are managed using Logback, and the configuration is set in `src/main/resources/logback-spring.xml`. The logs are output to the console by default, but you can configure it to write to a file or other appenders as needed.

Trace ID are propagated through RabbitMQ messages to maintain context across services. The trace ID is included in message headers and can be used for tracking requests across the system.

---

> **Study project developed for learning purposes.**

Built with ❤️ by Bruno Calegari